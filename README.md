# Barbearia MK — Backend

API REST desenvolvida para a operação de uma barbearia, com foco em agendamentos, gerenciamento de serviços, clientes e pagamentos.

Este foi um projeto real, desenvolvido sob demanda e comercializado para uso em um negócio. A aplicação foi pensada para resolver necessidades práticas do dia a dia: reduzir conflitos de agenda, facilitar o agendamento para clientes e centralizar a gestão administrativa.

## Funcionalidades

### Cliente

- Cadastro e autenticação de usuários;
- Criação de agendamentos;
- Consulta de horários agendados;
- Reagendamento e cancelamento de atendimentos;
- Consulta de dados do próprio perfil;
- Pagamento presencial ou online via PIX.

### Administração

- Visualização de todos os agendamentos;
- Consulta de clientes cadastrados;
- Criação, edição e exclusão de serviços;
- Controle de preços e duração dos atendimentos;
- Gestão centralizada da agenda.

### Pagamentos

- Geração de cobrança PIX integrada ao Mercado Pago;
- Criação de QR Code para pagamento;
- Consulta do status de pagamento;
- Confirmação automática por webhook;
- Uso de chave de idempotência para evitar cobranças duplicadas;
- Validação dos dados recebidos antes da confirmação do pagamento.

## Segurança

A aplicação possui recursos voltados para um ambiente real de produção:

- Autenticação baseada em JWT;
- Senhas criptografadas com BCrypt;
- Controle de acesso por perfil de usuário;
- Separação entre permissões de cliente e administrador;
- Limite de requisições por IP e global;
- Proteção contra excesso de tentativas de agendamento;
- Integração com Cloudflare Turnstile;
- Configuração de CORS;
- Validação de assinatura em webhooks do Mercado Pago;
- Proteção de dados sensíveis por variáveis de ambiente.

## Stack utilizada

- Java 25
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- JWT
- Maven
- Mercado Pago SDK
- Cloudflare Turnstile
- Guava RateLimiter
- Lombok

## Organização do projeto

```text
src/main/java/com/barbshop/system
├── controllers     # Endpoints da API
├── services        # Regras de negócio
├── repository      # Acesso aos dados
├── entity          # Entidades do banco de dados
├── security        # JWT, filtros e configurações de segurança
├── payment         # Fluxo de pagamento PIX
├── ratelimiter     # Limitação de requisições
├── cloudflare      # Validação do Turnstile
├── requests        # DTOs de entrada
├── responses       # DTOs de resposta
└── handlers        # Tratamento global de exceções
```

## Requisitos

- Java 25 ou superior;
- MySQL;
- Maven, ou o Maven Wrapper incluído no projeto;
- Conta configurada no Mercado Pago para utilização do PIX.

## Configuração

As configurações sensíveis devem ser definidas em um arquivo `.env`, sem enviar credenciais reais ao repositório.

Exemplo:

```properties
DB_URL=jdbc:mysql://localhost:3306/barbearia_mk
DB_USERNAME=seu_usuario
DB_PASS=sua_senha

JWT_SECRET=uma_chave_segura
JWT_TIME_EXPIRATION=86400000

ADMIN_EMAIL=admin@exemplo.com
ADMIN_PASSWORD=uma_senha_segura
ADMIN_NAME=Administrador MK
ADMIN_PHONE=11999999999

SITE_CORS=http://localhost:5500

MERCADO_PAGO_ACESS_KEY=sua_chave_mercado_pago
MERCADO_PAGO_WEBHOOK_SECRET_KEY=seu_segredo_webhook

CLOUDFLARE_TURNSTILE_ENABLED=false
CLOUDFLARE_PUBLIC_SITE_KEY=
CLOUDFLARE_SECRET_KEY=
```

Para produção, recomenda-se habilitar o Cloudflare Turnstile, definir as origens permitidas no CORS e configurar corretamente o webhook do Mercado Pago.

## Execução

No Windows:

```bash
.\mvnw.cmd spring-boot:run
```

A API será iniciada, por padrão, em:

```text
http://localhost:8080
```

## Principais fluxos

1. O cliente realiza cadastro ou login.
2. Seleciona os serviços e o horário desejado.
3. A API valida conflitos de agenda e disponibilidade.
4. O agendamento é criado.
5. Caso o pagamento seja online, a API gera uma cobrança PIX.
6. O Mercado Pago notifica a aplicação via webhook após a confirmação.
7. O sistema atualiza o status do pagamento de forma segura.

## Objetivo do projeto

O objetivo foi construir uma solução de backend segura e organizada para uma necessidade comercial real, priorizando regras de negócio, proteção de dados, controle de agenda e integração de pagamentos.
