
# Plataforma de Cursos - Mensageria com RabbitMQ

Trabalho final de mensageria. Uma plataforma de ensino publica eventos de domínio (conclusão, abandono, envio e revisão de avaliações) e cinco responsabilidades de consumo reagem de forma independente via RabbitMQ.

**Integrantes:**
- Edson C. Vicentim J. - RA: 269005
- Miguel Angelo Burati Moralez - RA: 269341
- Pedro Henrique Silvério da Fonseca - RA: 269424
- Victor Augusto Moreira Barbosa - RA: 269391

**Stack:** Java 17 | Spring Boot 3.2 | Spring AMQP | RabbitMQ 3 | PostgreSQL 16 | Docker Compose

---

## Sumário

1. [Arquitetura e Decisões Técnicas](#1-arquitetura-e-decisoes-tecnicas)
2. [Contrato do Evento](#2-contrato-do-evento)
3. [Matriz de Roteamento](#3-matriz-de-roteamento)
4. [Como Executar a Aplicação](#4-como-executar-a-aplicacao)
5. [Roteiro de Testes](#5-roteiro-de-testes)

---

## 1. Arquitetura e Decisões Técnicas

A arquitetura segue o modelo de publicação e assinatura (publish/subscribe) com as seguintes divisões:

- **Produtor (Porta 8080):** API REST que recebe os eventos e publica no RabbitMQ.
- **Exchange:** `plataforma.cursos` (Tipo: Topic)
- **Consumidores Independentes:**
   - Certificação (Fila: `q.certificacao` | Porta 8081)
   - Comunicação (Fila: `q.comunicacao` | Porta 8082)
   - Acompanhamento (Fila: `q.acompanhamento` | Porta 8083)
   - Auditoria (Fila: `q.auditoria` | Porta 8084)
   - Indicadores (Fila: `q.indicadores` | Portas 8085 e 8086)
- **Banco de Dados:** PostgreSQL para persistência e controle de idempotência.

### Por que usamos Topic Exchange?

Escolhemos o Topic Exchange porque ele permite atender todos os requisitos com uma única exchange, utilizando caracteres curingas (wildcards):

- **Seleção exata:** Certificação recebe apenas `curso.concluido`.
- **Acompanhamento de família:** O Acompanhamento usa `avaliacao.*` para pegar qualquer evento da família de avaliações.
- **Recebimento abrangente:** A Auditoria usa `#` para receber todos os eventos.

**Comparação com alternativas:**
- *Direct Exchange:* Não suporta curingas. Para acompanhar uma família, seria necessário criar um binding manual para cada tipo de avaliação. Novos eventos exigiriam alteração na configuração do broker.
- *Fanout Exchange:* Ignora a routing key e envia para todas as filas, impedindo o filtro seletivo (a Certificação receberia eventos de abandono, por exemplo).

### Idempotência

Todos os consumidores possuem uma coluna `evento_id` com restrição `UNIQUE` no banco. Ao receber uma mensagem duplicada, o banco barra a inserção e o consumidor apenas confirma a mensagem (Ack) sem gerar um novo efeito. Nos Indicadores (duas instâncias), usamos o comando `UPSERT` (ON CONFLICT DO UPDATE) no PostgreSQL para garantir a soma correta mesmo com concorrência.

### Confirmação de Consumo (Ack Manual)

O modo de confirmação está configurado como `manual`. A mensagem só é confirmada para o RabbitMQ (via `basicAck`) **após** o dado ser persistido com sucesso no banco de dados. Se ocorrer um erro (ex: banco indisponível), o consumidor executa um `basicNack` com `requeue=true`, devolvendo a mensagem à fila para nova tentativa.

---

## 2. Contrato do Evento

O envelope JSON padrão enviado pelo produtor possui a seguinte estrutura:

```json
{
  "eventId": "UUID-unico",
  "tipo": "CURSO_CONCLUIDO",
  "ocorridoEm": "2025-06-10T14:32:00Z",
  "dados": {
    "alunoId": "A-1",
    "alunoNome": "Maria",
    "cursoId": "C-10",
    "cursoNome": "Java",
    "cargaHoraria": 40
  }
}
```

---

## 3. Matriz de Roteamento

| Evento (routing key) | Certificação (:8081) | Comunicação (:8082) | Acompanhamento (:8083) | Auditoria (:8084) | Indicadores (:8085/86) |
|---|:-:|:-:|:-:|:-:|:-:|
| `curso.concluido` | Sim | Sim | Não | Sim | Sim |
| `curso.abandonado` | Não | Sim | Não | Sim | Não |
| `avaliacao.enviada` | Não | Não | Sim | Sim | Não |
| `avaliacao.revisada` | Não | Sim | Sim | Sim | Não |
| `avaliacao.<novo>` | Não | Não | Sim | Sim | Não |

---

## 4. Como Executar a Aplicação

### 4.1. Subir a Infraestrutura

Inicie o Docker Desktop e, na raiz do projeto, execute:

```bash
docker compose up -d
```

### 4.2. Importar as Configurações do RabbitMQ (definitions.json)

Para importar a topologia exata do projeto em uma instância limpa:

1. Acesse o painel do RabbitMQ em `http://localhost:15672` (usuário: `guest`, senha: `guest`).
2. Vá em **Overview** e role até o final, na seção **Import / export definitions**.
3. Clique em **Choose File** e selecione o arquivo `definitions.json` localizado na raiz deste repositório.
4. Clique em **Import**.

### 4.3. Iniciar as Aplicações

No IntelliJ, inicie os serviços nesta ordem:

- `ProducerApplication` (Porta 8080)
- `CertificacaoApplication` (Porta 8081)
- `ComunicacaoApplication` (Porta 8082)
- `AcompanhamentoApplication` (Porta 8083)
- `AuditoriaApplication` (Porta 8084)

**Para os Indicadores (duas instâncias):**

1. Rode `IndicadoresApplication` normalmente (Porta 8085).
2. Vá em `Run -> Edit Configurations`, duplique a configuração de execução.
3. Na nova configuração, adicione o argumento `--server.port=8086` no campo "Program arguments" e execute.

---

## 5. Roteiro de Testes

1. Abra o arquivo `testes.http` na raiz do projeto (pelo IntelliJ) para disparar os eventos.
2. **Teste de Roteamento:** Dispare o evento `curso-concluido`. Verifique nos consoles que Certificação, Comunicação e Auditoria reagiram, enquanto Acompanhamento não.
3. **Teste de Concorrência:** Com as duas instâncias de Indicadores rodando, dispare múltiplos eventos de `curso-concluido`. Verifique nos logs que o RabbitMQ dividiu as mensagens entre as portas 8085 e 8086, e que o total no banco está correto.
4. **Teste de Interrupção:** Pare o `ComunicacaoApplication`, dispare um evento e verifique que ele fica retido na fila (`q.comunicacao`) no painel do RabbitMQ. Ligue a aplicação novamente e confirme o processamento da mensagem pendente.
5. **Teste de Duplicidade:** No arquivo `testes.http`, utilize a requisição `POST /eventos/republicar` (que envia o mesmo `eventId`). Execute duas vezes. Verifique nos logs a mensagem "já processado" e consulte o endpoint `/certificados` para atestar que apenas um registro foi gerado.
