(() => {
  const $ = (selector, root = document) => root.querySelector(selector);
  const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
  const API_PATH = "/mk_barb";
  const DEFAULT_API_URL = "http://localhost:8080";
  const serviceNames = {CORTE_DE_CABELO:"Corte de cabelo",SOBRANCELHA:"Sobrancelha",BARBA:"Barba",BARBA_E_CABELO:"Barba e cabelo",CORTE_PROMOCIONAL:"Corte promocional"};
  const state = {session: readSession(), services: [], profile: null, currentModal: null, pendingPix: null, portalView: "appointments", pages: {appointments: 0, adminAppointments: 0, users: 0, services: 0}};
  const modalLayer = $("#modalLayer"), bookingModal = $("#bookingModal"), authModal = $("#authModal"), portalModal = $("#portalModal"), settingsModal = $("#settingsModal"), paymentModal = $("#paymentModal");
  let pixPolling = null, pixCountdown = null;

  function readSession() { try { return JSON.parse(localStorage.getItem("mk-session")) || null; } catch { return null; } }
  function saveSession(session) { state.session = session; localStorage.setItem("mk-session", JSON.stringify(session)); }
  function clearSession() { state.session = null; state.profile = null; localStorage.removeItem("mk-session"); }
  function apiBase() { return (localStorage.getItem("mk-api-url") || DEFAULT_API_URL).trim().replace(/\/$/, ""); }
  function url(path) { return `${apiBase()}${API_PATH}${path}`; }
  function escape(value = "") { return String(value).replace(/[&<>'"]/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;","'":"&#39;","\"":"&quot;"}[c])); }
  function prettyName(value) { return serviceNames[value] || String(value || "Serviço"); }
  function money(value) { return new Intl.NumberFormat("pt-BR", {style:"currency", currency:"BRL"}).format(Number(value || 0)); }
  function timeText(value) {
    if (value === null || value === undefined) return "duração a confirmar";
    const raw = String(value);
    if (/^PT\d+M$/i.test(raw)) return `${raw.match(/\d+/)[0]} min`;
    if (/^\d{2}:\d{2}/.test(raw)) return `${raw.slice(0, 5)} h`;
    if (!Number.isNaN(Number(raw))) return `${raw} min`;
    return raw;
  }
  function minutes(value) {
    const raw = String(value || "");
    if (/^PT\d+M$/i.test(raw)) return Number(raw.match(/\d+/)[0]);
    if (/^\d{2}:\d{2}/.test(raw)) { const [h,m] = raw.split(":").map(Number); return h * 60 + m; }
    const amount = Number(raw); return Number.isFinite(amount) ? amount : 30;
  }
  function shortDate(value) { if (!value) return "Data a definir"; const date = new Date(value); return Number.isNaN(date) ? value : new Intl.DateTimeFormat("pt-BR", {dateStyle:"medium", timeStyle:"short"}).format(date); }
  function initialLetters(name) { return String(name || "MK").split(/\s+/).filter(Boolean).slice(0,2).map(p => p[0]).join("").toUpperCase(); }
  function todayISO() { const now = new Date(); now.setMinutes(now.getMinutes() - now.getTimezoneOffset()); return now.toISOString().slice(0,10); }
  function feedback(node, message, kind = "") { if (!node) return; node.textContent = message; node.className = `form-feedback ${kind}`; }
  function toast(message, type = "") { const node = document.createElement("div"); node.className = `toast ${type}`; node.textContent = message; $("#toastRegion").append(node); setTimeout(() => node.remove(), 4200); }
  function messageFrom(error) { return error?.message || error?.body?.message || error?.body?.erro || "Não foi possível concluir a operação. Tente novamente."; }

  async function api(path, options = {}) {
    const headers = {Accept:"application/json", ...(options.body ? {"Content-Type":"application/json"} : {}), ...(options.headers || {})};
    if (state.session?.token && options.auth !== false) headers.Authorization = `Bearer ${state.session.token}`;
    let response;
    try { response = await fetch(url(path), {...options, headers}); }
    catch { throw {message:"Não foi possível falar com a API. Verifique se o servidor está em execução."}; }
    const text = await response.text(); let body;
    try { body = text ? JSON.parse(text) : null; } catch { body = text; }
    if (!response.ok) {
      if ((response.status === 401 || response.status === 403) && options.auth !== false) { clearSession(); closeModal(); toast("Sua sessão expirou. Entre novamente.", "error"); }
      throw {status: response.status, body, message: body?.message || body?.erro};
    }
    return body;
  }
  function contentOf(page) { return Array.isArray(page) ? page : (page?.content || []); }

  function openModal(modal) {
    if (!modalLayer || !modal) return;
    $$(".modal", modalLayer).forEach(item => { item.hidden = item !== modal; });
    modalLayer.classList.add("open"); modalLayer.setAttribute("aria-hidden", "false"); state.currentModal = modal;
    const focus = modal.querySelector("input, button, select"); if (focus) setTimeout(() => focus.focus(), 30);
  }
  function stopPixMonitoring() { if (pixPolling) clearInterval(pixPolling); if (pixCountdown) clearInterval(pixCountdown); pixPolling = null; pixCountdown = null; }
  function closeModal() { if (!modalLayer) return; stopPixMonitoring(); modalLayer.classList.remove("open"); modalLayer.setAttribute("aria-hidden", "true"); $$(".modal", modalLayer).forEach(item => item.hidden = true); state.currentModal = null; }
  function openAuth(tab = "login") {
    if (document.body.dataset.page !== "auth") { window.location.href = state.session ? (state.session.role === "USER_ADMIN" ? "admin.html" : "minha-conta.html") : "entrar.html"; return; }
    setAuthTab(tab); openModal(authModal);
  }
  function setAuthTab(tab) { $$('[data-auth-tab]').forEach(b => b.classList.toggle("active", b.dataset.authTab === tab)); $("#loginForm").hidden = tab !== "login"; $("#registerForm").hidden = tab !== "register"; }

  async function loadServices(force = false) {
    if (state.services.length && !force) return state.services;
    const page = await api("/public/cortes/todos_tipos_de_cortes?page=0&size=50", {auth:false});
    state.services = contentOf(page); return state.services;
  }
  async function renderPublicServices() {
    const host = $("#publicServices");
    if (!host) return;
    try {
      const services = await loadServices();
      if (!services.length) { host.innerHTML = '<div class="service-loading">Os serviços serão disponibilizados em breve.</div>'; return; }
      host.innerHTML = services.map((s, i) => `<article class="service-card"><span class="service-index">0${i + 1}</span><h3>${escape(prettyName(s.nome))}</h3><p>${escape(timeText(s.tempo))} de atendimento</p><div class="price"><b>${money(s.preco)}</b><span>selecionar ↗</span></div></article>`).join("");
    } catch (error) { host.innerHTML = `<div class="service-loading">Não foi possível carregar os serviços agora.</div>`; }
  }
  async function openBooking() {
    if (document.body.dataset.page !== "booking") { window.location.href = "agendar.html"; return; }
    const publicFields = $("#publicIdentityFields"), notice = $("#bookingIdentityNotice");
    const isCustomer = state.session?.role === "USER_COMMOM";
    publicFields.hidden = isCustomer;
    $$("input", publicFields).forEach(input => input.disabled = isCustomer);
    notice.innerHTML = isCustomer ? '<span class="form-mode-dot"></span><span>Agendamento na sua conta</span>' : '<span class="form-mode-dot"></span><span>Agendamento público</span><button type="button" class="inline-action session-trigger">Entrar na conta</button>';
    if (isCustomer) { try { state.profile ||= await api("/auth/login/usuario/view"); } catch {} }
    const data = $("#appointmentForm");
    // Alterado pelo Codex: corrige a mensagem antiga que sugeria pagamento já confirmado.
    const onlineHelp = $("input[name=pagamento][value=ONLINE] + span small");
    if (onlineHelp) onlineHelp.textContent = "Escaneie o PIX e confirme em até 8 minutos";
    if (isCustomer && state.profile) { data.elements.nome.value = state.profile.nome || ""; data.elements.email.value = state.profile.email || ""; data.elements.telefone.value = state.profile.numTel || ""; }
    data.elements.data.min = todayISO(); $("#bookingFeedback").textContent = "";
    try { await loadServices(); renderBookingServices(); } catch (error) { $("#bookingServices").innerHTML = '<p class="field-message">Não foi possível carregar os serviços. Verifique a conexão e tente de novo.</p>'; }
    updateBookingSummary(); openModal(bookingModal);
  }
  function renderBookingServices() {
    const host = $("#bookingServices");
    host.innerHTML = state.services.length ? state.services.map(s => `<label class="service-choice"><input type="checkbox" value="${Number(s.idCortes)}" data-price="${Number(s.preco)}" data-minutes="${minutes(s.tempo)}" /><span><b>${escape(prettyName(s.nome))}</b><small>${money(s.preco)} · ${escape(timeText(s.tempo))}</small></span></label>`).join("") : '<p class="field-message">Nenhum serviço disponível.</p>';
  }
  function selectedServices() { return $$("#bookingServices input:checked").map(input => ({id:Number(input.value), price:Number(input.dataset.price), minutes:Number(input.dataset.minutes)})); }
  function updateBookingSummary() {
    const services = selectedServices(), total = services.reduce((sum, item) => sum + item.price, 0), duration = services.reduce((sum, item) => sum + item.minutes, 0);
    $("#bookingSummary").innerHTML = services.length ? `<span>${services.length} serviço${services.length > 1 ? "s" : ""} · ${duration} min</span><b>${money(total)}</b>` : '<span>Nenhum serviço selecionado</span><b>R$ 0,00</b>';
    if ($("#appointmentForm").elements.data.value) populateTimeOptions();
  }
  function slotToMinutes(slot) { const [h,m] = slot.split(":").map(Number); return h * 60 + m; }
  function asSlot(value) { const match = String(value || "").match(/(\d{2}:\d{2})/); return match?.[1] || ""; }
  async function populateTimeOptions() {
    const form = $("#appointmentForm"), date = form.elements.data.value, select = form.elements.horario, message = $("#availabilityMessage");
    if (!date) return;
    select.disabled = true; select.innerHTML = '<option>Consultando disponibilidade...</option>'; message.className = "availability-message";
    const duration = selectedServices().reduce((sum, item) => sum + item.minutes, 0) || 30;
    let occupied = new Set();
    try { const page = await api(`/public/horarios_ocupados/pordia/${date}?page=0&size=50`, {auth:false}); occupied = new Set(contentOf(page).map(item => asSlot(item.horario)).filter(Boolean)); message.textContent = "Horários ocupados foram removidos da lista."; }
    catch (error) { if (error.status === 404) { message.textContent = "Todos os horários sugeridos estão livres."; } else { message.textContent = "Não foi possível consultar os ocupados. A confirmação final será feita ao reservar."; message.className = "availability-message error"; } }
    const slots = [];
    for (let min = 9 * 60; min <= 19 * 60 + 30; min += 30) {
      if (min + duration > 20 * 60) continue;
      const candidate = `${String(Math.floor(min / 60)).padStart(2,"0")}:${String(min % 60).padStart(2,"0")}`;
      const blocks = Math.ceil(duration / 30); let free = true;
      for (let block = 0; block < blocks; block++) { const pos = min + block * 30; const key = `${String(Math.floor(pos / 60)).padStart(2,"0")}:${String(pos % 60).padStart(2,"0")}`; if (occupied.has(key)) free = false; }
      if (free) slots.push(candidate);
    }
    select.disabled = false; select.innerHTML = `<option value="">${slots.length ? "Selecione" : "Sem horário sugerido"}</option>${slots.map(slot => `<option value="${slot}">${slot}</option>`).join("")}`;
    if (!slots.length) { select.disabled = true; message.textContent = "Não há horários sugeridos para esta data. Tente outro dia."; message.className = "availability-message error"; }
  }
  async function submitAppointment(event) {
    event.preventDefault(); const form = event.currentTarget, services = selectedServices();
    if (!services.length) { feedback($("#bookingFeedback"), "Escolha pelo menos um serviço.", "error"); return; }
    const date = form.elements.data.value, time = form.elements.horario.value;
    if (!date || !time) { feedback($("#bookingFeedback"), "Escolha uma data e um horário.", "error"); return; }
    const isCustomer = state.session?.role === "USER_COMMOM";
    const identity = isCustomer && state.profile ? {nome:state.profile.nome, email:state.profile.email, telefone:state.profile.numTel} : {nome:form.elements.nome.value.trim(), email:form.elements.email.value.trim(), telefone:form.elements.telefone.value.trim()};
    if (!identity.nome || !identity.email || !identity.telefone) { feedback($("#bookingFeedback"), "Preencha seus dados para continuar.", "error"); return; }
    const online = form.elements.pagamento.value === "ONLINE";
    // Alterado pelo Codex: o frontend informa somente a modalidade; o backend inicia PIX como PENDENTE.
    const body = {nome_cliente:identity.nome,email_cliente:identity.email,data_horario_corte:`${date}T${time}:00`,telefone_cliente:identity.telefone,tipoPagamento:online ? "ONLINE" : "NO_LOCAL",listaCortes:services.map(item => item.id)};
    const submit = form.querySelector("[type=submit]"); submit.disabled = true; feedback($("#bookingFeedback"), "Reservando seu horário...");
    try {
      const reserva = await api(isCustomer ? "/auth/login/usuario/agendamento/create" : "/public/marcar", {method:"POST", body:JSON.stringify(body), auth:isCustomer});
      form.reset(); renderBookingServices(); updateBookingSummary();
      if (online) {
        await gerarEAbrirPix(reserva, isCustomer);
        if (portalModal.hidden === false) setTimeout(() => renderPortal(state.portalView), 50);
        return;
      }
      feedback($("#bookingFeedback"), "Agendamento confirmado. Até breve!", "success"); toast("Horário reservado com sucesso.", "success"); if (portalModal.hidden === false) setTimeout(() => renderPortal(state.portalView), 50); setTimeout(closeModal, 1150);
    }
    catch (error) { feedback($("#bookingFeedback"), messageFrom(error), "error"); }
    finally { submit.disabled = false; }
  }

  // Alterado pelo Codex: exibe QR Code, código copia-e-cola e confirmação real do webhook.
  async function gerarEAbrirPix(reserva, isCustomer) {
    state.pendingPix = {reserva, isCustomer};
    try {
      const pix = isCustomer
        ? await api(`/auth/login/usuario/pagamento/${reserva.idAgendamento}/pix`, {method:"POST"})
        : await api("/public/pagamento/pix", {method:"POST", headers:{"X-Booking-Token":reserva.tokenPagamentoPublico}, auth:false});
      mostrarPix(pix);
    } catch (error) {
      mostrarErroPix(messageFrom(error));
    }
  }
  function mostrarPix(pix) {
    if (!paymentModal) return;
    const code = pix?.qrCode || "", base64 = pix?.qrCodeBase64 || "";
    $("#pixQrImage").src = base64 ? `data:image/png;base64,${base64}` : "";
    $("#pixQrImage").hidden = !base64;
    $("#pixCopyCode").value = code;
    $("#pixTicketLink").href = pix?.ticketUrl || "#";
    $("#pixTicketLink").hidden = !pix?.ticketUrl;
    $("#pixRetryButton").hidden = true;
    feedback($("#pixFeedback"), "Aguardando a confirmação segura do pagamento.");
    openModal(paymentModal);
    acompanharPix(pix?.pagamentoExpiraEm);
  }
  function mostrarErroPix(message) {
    if (!paymentModal) return;
    $("#pixQrImage").hidden = true; $("#pixCopyCode").value = ""; $("#pixTicketLink").hidden = true;
    $("#pixRetryButton").hidden = false;
    feedback($("#pixFeedback"), `Horário reservado. ${message}`, "error");
    openModal(paymentModal);
  }
  function acompanharPix(expiraEm) {
    stopPixMonitoring();
    const atualizarTempo = () => {
      const restante = new Date(expiraEm).getTime() - Date.now();
      const host = $("#pixExpiry"); if (!host) return;
      if (restante <= 0) { host.textContent = "Este PIX expirou."; return; }
      const minutos = Math.floor(restante / 60000), segundos = Math.floor((restante % 60000) / 1000);
      host.textContent = `Reserva válida por ${minutos}:${String(segundos).padStart(2,"0")}.`;
    };
    atualizarTempo(); pixCountdown = setInterval(atualizarTempo, 1000);
    pixPolling = setInterval(async () => {
      const pending = state.pendingPix; if (!pending) return;
      try {
        const status = pending.isCustomer
          ? await api(`/auth/login/usuario/pagamento/${pending.reserva.idAgendamento}/status`)
          : await api("/public/pagamento/status", {headers:{"X-Booking-Token":pending.reserva.tokenPagamentoPublico}, auth:false});
        if (status.statusPagamento === "PAGO") { stopPixMonitoring(); feedback($("#pixFeedback"), "Pagamento confirmado. Seu horário está garantido!", "success"); toast("PIX confirmado com sucesso.", "success"); }
        if (status.statusPagamento === "EXPIRADO") { stopPixMonitoring(); feedback($("#pixFeedback"), "O prazo expirou e o horário foi liberado.", "error"); }
      } catch (_) { /* A próxima tentativa de consulta continua automaticamente. */ }
    }, 5000);
  }

  async function submitLogin(event) { event.preventDefault(); const form = event.currentTarget, button = form.querySelector("[type=submit]"), note = $(".form-feedback", form); button.disabled = true; feedback(note, "Entrando..."); try { const result = await api("/auth/login", {method:"POST", body:JSON.stringify({email:form.elements.email.value.trim(), senha:form.elements.senha.value}), auth:false}); saveSession(result); state.profile = null; feedback(note, "Entrada confirmada.", "success"); toast(`Bem-vindo${result.nome ? `, ${result.nome}` : ""}.`, "success"); setTimeout(() => { window.location.href = result.role === "USER_ADMIN" ? "admin.html" : "minha-conta.html"; }, 350); } catch (error) { feedback(note, messageFrom(error), "error"); } finally { button.disabled = false; } }
  async function submitRegister(event) { event.preventDefault(); const form = event.currentTarget, button = form.querySelector("[type=submit]"), note = $(".form-feedback", form); button.disabled = true; feedback(note, "Criando sua conta..."); try { await api("/public/novo_usuario", {method:"POST", body:JSON.stringify({nome:form.elements.nome.value.trim(),numTel:form.elements.numTel.value.trim(),email:form.elements.email.value.trim(),senha:form.elements.senha.value}), auth:false}); feedback(note, "Conta criada. Agora entre com seu e-mail e senha.", "success"); form.reset(); setTimeout(() => setAuthTab("login"), 950); } catch (error) { feedback(note, messageFrom(error), "error"); } finally { button.disabled = false; } }

  function navItems() { return state.session?.role === "USER_ADMIN" ? [{id:"adminOverview",icon:"01",name:"Visão geral"},{id:"adminAppointments",icon:"02",name:"Agenda"},{id:"users",icon:"03",name:"Clientes"},{id:"services",icon:"04",name:"Serviços"}] : [{id:"appointments",icon:"01",name:"Meus agendamentos"},{id:"profile",icon:"02",name:"Meu perfil"}]; }
  function openPortal(view) { if (!state.session) { openAuth(); return; } state.portalView = view || (state.session.role === "USER_ADMIN" ? "adminOverview" : "appointments"); fillAccount(); renderPortalNav(); openModal(portalModal); renderPortal(state.portalView); }
  function fillAccount() { $("#accountName").textContent = state.session?.nome || "Minha conta"; $("#accountInitials").textContent = initialLetters(state.session?.nome); const admin = state.session?.role === "USER_ADMIN"; $("#accountRole").textContent = admin ? "Administrador" : "Cliente"; $("#portalKicker").textContent = admin ? "GESTÃO MK" : "MINHA ÁREA"; }
  function renderPortalNav() { $("#portalNav").innerHTML = navItems().map(item => `<button class="portal-nav-button ${item.id === state.portalView ? "active" : ""}" type="button" data-portal-view="${item.id}"><b>${item.icon}</b>${item.name}</button>`).join(""); }
  function title(title, description, action = "") { return `<div class="portal-view-title"><div><h2>${title}</h2><p>${description}</p></div>${action}</div>`; }
  function loading() { $("#portalView").innerHTML = '<div class="loading-state">Carregando informações...</div>'; }
  async function renderPortal(view) {
    state.portalView = view; renderPortalNav(); loading();
    try { if (view === "appointments") await renderMyAppointments(); else if (view === "profile") await renderProfile(); else if (view === "adminOverview") await renderAdminOverview(); else if (view === "adminAppointments") await renderAdminAppointments(); else if (view === "users") await renderUsers(); else if (view === "services") await renderServiceManagement(); }
    catch (error) { $("#portalView").innerHTML = `<div class="empty-state"><b>Não foi possível carregar.</b>${escape(messageFrom(error))}<br /><button class="mini-button" type="button" data-reload-portal>Tentar de novo</button></div>`; }
  }
  function appointmentItem(item, admin = false) {
    const services = Array.isArray(item.listacortes) ? item.listacortes.map(prettyName).join(" · ") : "Serviços a confirmar";
    return `<li class="appointment-item"><div class="appointment-main"><b>${escape(item.nome || "Cliente")}</b><small>${escape(item.email || "")}</small></div><div class="appointment-services">${escape(services)}</div><div class="item-muted">${escape(shortDate(item.dataHoraInicio))}</div><div class="item-actions">${!admin ? `<button class="icon-button" type="button" data-reschedule="${Number(item.id)}" title="Reagendar">↻</button>` : ""}<button class="icon-button danger" type="button" data-delete-appointment="${Number(item.id)}" data-admin="${admin}" title="Cancelar">×</button></div></li>`;
  }
  function panel(head, subtitle, body, pagination = "") { return `<section class="data-panel"><div class="data-panel-head"><h3>${head}</h3><span>${subtitle}</span></div>${body}${pagination}</section>`; }
  function pagination(page, view) { if (page.totalPages <= 1) return ""; return `<div class="pagination"><button type="button" data-page-view="${view}" data-page="${page.number - 1}" ${page.first ? "disabled" : ""}>←</button><span>${page.number + 1} de ${page.totalPages}</span><button type="button" data-page-view="${view}" data-page="${page.number + 1}" ${page.last ? "disabled" : ""}>→</button></div>`; }
  async function renderMyAppointments() { const page = await api(`/auth/login/usuario/agendamento/view?page=${state.pages.appointments}&size=10`); const items = contentOf(page); $("#portalView").innerHTML = title("Meus horários", "Acompanhe e ajuste seus próximos atendimentos.", '<button class="mini-button booking-trigger" type="button">+ novo horário</button>') + panel("Próximos agendamentos", items.length ? `${page.totalElements || items.length} registrado(s)` : "", items.length ? `<ul class="appointment-list">${items.map(item => appointmentItem(item)).join("")}</ul>` : '<div class="empty-state"><b>Nenhum horário por aqui.</b>Quando estiver pronto, reserve seu próximo atendimento.</div>', pagination(page, "appointments")); }
  async function renderProfile() { state.profile ||= await api("/auth/login/usuario/view"); const p = state.profile; $("#portalView").innerHTML = title("Meu perfil", "Seus dados usados nos próximos agendamentos.") + `<section class="profile-card"><span class="account-avatar">${escape(initialLetters(p.nome))}</span><div><h3>${escape(p.nome)}</h3><p>${escape(p.email)} · ${escape(p.numTel)}</p></div><button class="mini-button booking-trigger" type="button">Agendar</button></section><section class="danger-zone"><h3>Encerrar conta</h3><p>Esta ação remove sua conta de cliente. Ela não pode ser desfeita.</p><button class="danger-button" type="button" data-delete-account>Excluir minha conta</button></section>`; }
  async function renderAdminOverview() { const [appointments, users, services] = await Promise.all([api("/auth/login/admin/todos_agendamentos?page=0&size=5"), api("/auth/login/admin/todos_usuarios?page=0&size=5"), api("/auth/login/admin/cortes/view/todos_tipos_de_cortes?page=0&size=50")]); const a = contentOf(appointments); $("#portalView").innerHTML = title("Visão geral", "Acompanhe a operação da barbearia.") + `<div class="dashboard-stats"><article class="stat-card"><small>AGENDAMENTOS</small><b>${appointments.totalElements ?? a.length}</b></article><article class="stat-card"><small>CLIENTES</small><b>${users.totalElements ?? contentOf(users).length}</b></article><article class="stat-card"><small>SERVIÇOS ATIVOS</small><b>${services.totalElements ?? contentOf(services).length}</b></article></div>` + panel("Agenda recente", "últimos registros", a.length ? `<ul class="appointment-list">${a.map(item => appointmentItem(item, true)).join("")}</ul>` : '<div class="empty-state"><b>A agenda está vazia.</b>Os próximos horários aparecerão aqui.</div>'); }
  async function renderAdminAppointments() { const page = await api(`/auth/login/admin/todos_agendamentos?page=${state.pages.adminAppointments}&size=10`); const items = contentOf(page); $("#portalView").innerHTML = title("Agenda", "Todos os atendimentos marcados na barbearia.") + panel("Agendamentos", `${page.totalElements ?? items.length} no total`, items.length ? `<ul class="appointment-list">${items.map(item => appointmentItem(item, true)).join("")}</ul>` : '<div class="empty-state"><b>Agenda vazia.</b>Nenhum agendamento foi encontrado.</div>', pagination(page, "adminAppointments")); }
  async function renderUsers() { const page = await api(`/auth/login/admin/todos_usuarios?page=${state.pages.users}&size=10`); const items = contentOf(page); $("#portalView").innerHTML = title("Clientes", "Pessoas cadastradas na plataforma.") + panel("Cadastros", `${page.totalElements ?? items.length} no total`, items.length ? `<ul class="user-list">${items.map(user => `<li class="user-item"><div class="appointment-main"><b>${escape(user.nome)}</b><small>${escape(user.email)}</small></div><div class="item-muted">${escape(user.numTel || "")}</div></li>`).join("")}</ul>` : '<div class="empty-state"><b>Nenhum cliente encontrado.</b>Os novos cadastros aparecerão aqui.</div>', pagination(page, "users")); }
  function serviceEditor() { return `<form class="service-editor" id="newServiceForm"><input name="nome" type="text" maxlength="100" placeholder="Nome do serviço" required /><input name="preco" type="number" min="0.01" step="0.01" placeholder="Preço" required /><input name="tempo" type="time" step="60" value="00:30" required /><button class="mini-button" type="submit">Criar serviço</button></form>`; }
  async function renderServiceManagement() { const page = await api(`/auth/login/admin/cortes/view/todos_tipos_de_cortes?page=${state.pages.services}&size=50`); const items = contentOf(page); $("#portalView").innerHTML = title("Serviços", "Crie, altere o preço ou remova os serviços disponíveis.") + panel("Catálogo de serviços", `${items.length} exibido(s)`, serviceEditor() + (items.length ? `<ul class="service-admin-list">${items.map(item => `<li class="admin-service-item"><div class="appointment-main"><b>${escape(prettyName(item.nome))}</b><small>${escape(timeText(item.tempo))}</small></div><div>${money(item.preco)}</div><div class="item-muted">#${Number(item.idCortes)}</div><div class="item-actions"><button class="icon-button" type="button" data-edit-service="${Number(item.idCortes)}" title="Editar">✎</button><button class="icon-button danger" type="button" data-delete-service="${Number(item.idCortes)}" title="Excluir">×</button></div></li><li class="service-edit-row" id="serviceEdit-${Number(item.idCortes)}"><input data-service-name type="text" maxlength="100" value="${escape(item.nome)}" /><input data-service-price type="number" min="0.01" step="0.01" value="${Number(item.preco)}" /><button class="mini-button" type="button" data-save-service="${Number(item.idCortes)}">Salvar</button></li>`).join("")}</ul>` : '<div class="empty-state"><b>Sem serviços cadastrados.</b>Use o formulário acima para criar o primeiro.</div>')); }
  async function deleteAppointment(id, admin) { if (!confirm("Cancelar este agendamento?")) return; try { await api(admin ? `/auth/login/admin/deletebyid/${id}` : `/auth/login/usuario/agendamento/delete/${id}`, {method:"DELETE"}); toast("Agendamento cancelado.", "success"); renderPortal(state.portalView); } catch (error) { toast(messageFrom(error), "error"); } }
  async function reschedule(id) { const value = prompt("Novo horário (AAAA-MM-DDTHH:MM:SS)", ""); if (!value) return; try { await api(`/auth/login/usuario/agendamento/change/byid/${id}`, {method:"PATCH", body:JSON.stringify({novoHorario:value})}); toast("Pedido de reagendamento concluído.", "success"); renderPortal("appointments"); } catch (error) { toast(messageFrom(error), "error"); } }
  async function deleteAccount() { if (!confirm("Tem certeza? Sua conta será excluída.")) return; try { await api("/auth/login/usuario/deletar", {method:"DELETE"}); clearSession(); closeModal(); toast("Sua conta foi excluída.", "success"); } catch (error) { toast(messageFrom(error), "error"); } }
  async function createService(event) { event.preventDefault(); const form = event.target, submit = form.querySelector("button"); submit.disabled = true; try { const time = form.elements.tempo.value.length === 5 ? `${form.elements.tempo.value}:00` : form.elements.tempo.value; await api("/auth/login/admin/cortes/criar", {method:"POST", body:JSON.stringify({nome:form.elements.nome.value.trim(),preco:Number(form.elements.preco.value),tempo:time})}); toast("Serviço criado.", "success"); state.services = []; renderPortal("services"); renderPublicServices(); } catch (error) { toast(messageFrom(error), "error"); } finally { submit.disabled = false; } }
  async function saveService(id) { const row = $(`#serviceEdit-${id}`); if (!row) return; try { await api(`/auth/login/admin/cortes/mudar/byid/${id}`, {method:"PATCH", body:JSON.stringify({nome:$("[data-service-name]", row).value,preco:Number($("[data-service-price]", row).value)})}); toast("Serviço atualizado.", "success"); state.services = []; renderPortal("services"); renderPublicServices(); } catch (error) { toast(messageFrom(error), "error"); } }
  async function deleteService(id) { if (!confirm("Excluir este serviço do catálogo?")) return; try { await api(`/auth/login/admin/cortes/deletar/byid/${id}`, {method:"DELETE"}); state.services = []; toast("Serviço excluído.", "success"); renderPortal("services"); renderPublicServices(); } catch (error) { toast(messageFrom(error), "error"); } }

  document.addEventListener("click", event => {
    const sessionTrigger = event.target.closest(".session-trigger"); if (sessionTrigger) { state.session ? openPortal() : openAuth(); return; }
    if (event.target.closest(".booking-trigger")) { openBooking(); return; }
    const close = event.target.closest("[data-close-modal]"); if (close || event.target === modalLayer) { closeModal(); return; }
    const authTab = event.target.closest("[data-auth-tab]"); if (authTab) { setAuthTab(authTab.dataset.authTab); return; }
    const view = event.target.closest("[data-portal-view]"); if (view) { renderPortal(view.dataset.portalView); return; }
    const deleteApp = event.target.closest("[data-delete-appointment]"); if (deleteApp) { deleteAppointment(deleteApp.dataset.deleteAppointment, deleteApp.dataset.admin === "true"); return; }
    const change = event.target.closest("[data-reschedule]"); if (change) { reschedule(change.dataset.reschedule); return; }
    // Alterado pelo Codex: ações do modal PIX não manipulam status de pagamento no navegador.
    if (event.target.closest("[data-copy-pix]")) { const code = $("#pixCopyCode")?.value; if (code) navigator.clipboard?.writeText(code).then(() => toast("Código PIX copiado.", "success")); return; }
    if (event.target.closest("[data-retry-pix]") && state.pendingPix) { gerarEAbrirPix(state.pendingPix.reserva, state.pendingPix.isCustomer); return; }
    if (event.target.closest("[data-delete-account]")) { deleteAccount(); return; }
    const page = event.target.closest("[data-page-view]"); if (page) { state.pages[page.dataset.pageView] = Number(page.dataset.page); renderPortal(page.dataset.pageView); return; }
    const edit = event.target.closest("[data-edit-service]"); if (edit) { $(`#serviceEdit-${edit.dataset.editService}`)?.classList.toggle("open"); return; }
    const save = event.target.closest("[data-save-service]"); if (save) { saveService(save.dataset.saveService); return; }
    const delService = event.target.closest("[data-delete-service]"); if (delService) { deleteService(delService.dataset.deleteService); return; }
    if (event.target.closest("[data-reload-portal]")) { renderPortal(state.portalView); }
  });
  const appointmentForm = $("#appointmentForm"), loginForm = $("#loginForm"), registerForm = $("#registerForm"), portalView = $("#portalView"), logoutButton = $("#logoutButton"), apiSettingsButton = $("#apiSettingsButton"), apiSettingsForm = $("#apiSettingsForm");
  if (appointmentForm) { appointmentForm.addEventListener("submit", submitAppointment); appointmentForm.addEventListener("change", event => { if (event.target.matches("input[type=checkbox]")) updateBookingSummary(); if (event.target.name === "data") populateTimeOptions(); }); }
  if (loginForm) loginForm.addEventListener("submit", submitLogin); if (registerForm) registerForm.addEventListener("submit", submitRegister);
  if (portalView) portalView.addEventListener("submit", event => { if (event.target.id === "newServiceForm") createService(event); });
  if (logoutButton) logoutButton.addEventListener("click", () => { clearSession(); toast("Você saiu da sua conta."); setTimeout(() => { window.location.href = "index.html"; }, 180); });
  if (apiSettingsButton) apiSettingsButton.addEventListener("click", () => { $("#apiUrlInput").value = localStorage.getItem("mk-api-url") || ""; openModal(settingsModal); });
  if (apiSettingsForm) apiSettingsForm.addEventListener("submit", event => { event.preventDefault(); const raw = $("#apiUrlInput").value.trim().replace(/\/$/, ""); if (raw && !/^https?:\/\//i.test(raw)) { feedback($(".form-feedback", event.currentTarget), "Use uma URL começando com http:// ou https://", "error"); return; } if (raw) localStorage.setItem("mk-api-url", raw); else localStorage.removeItem("mk-api-url"); state.services = []; feedback($(".form-feedback", event.currentTarget), "Endereço salvo.", "success"); renderPublicServices(); setTimeout(closeModal, 700); });
  document.addEventListener("keydown", event => { if (!modalLayer || event.key !== "Escape" || !modalLayer.classList.contains("open")) return; closeModal(); });
  const year = $("#currentYear"); if (year) year.textContent = new Date().getFullYear(); renderPublicServices();
  const page = document.body.dataset.page;
  if (page === "booking") openBooking();
  if (page === "auth") openAuth();
  if (page === "account" || page === "admin") {
    const requiredRole = page === "admin" ? "USER_ADMIN" : "USER_COMMOM";
    if (!state.session) window.location.replace("entrar.html");
    else if (state.session.role !== requiredRole) window.location.replace(state.session.role === "USER_ADMIN" ? "admin.html" : "minha-conta.html");
    else openPortal(page === "admin" ? "adminOverview" : "appointments");
  }
})();
