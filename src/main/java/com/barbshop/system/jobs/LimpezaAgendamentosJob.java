package com.barbshop.system.jobs;

import com.barbshop.system.services.AgendamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LimpezaAgendamentosJob {

    private final AgendamentoService service;

    //Roda todos os dias á 1h da manhã
    @Scheduled(cron = "0 0 1 * * ?")
    public void executarJobs() {
        service.deletarAgendamentosAntigos();
    }

}
