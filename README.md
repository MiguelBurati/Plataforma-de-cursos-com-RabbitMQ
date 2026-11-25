# Plataforma de Cursos — Mensageria com RabbitMQ

Trabalho final de mensageria. Uma plataforma de ensino publica eventos de domínio
(conclusão, abandono, envio e revisão de avaliações) e cinco responsabilidades de
consumo reagem de forma independente via RabbitMQ.

**Integrantes:**
- Edson C. Vicentim J. - RA:269005
- Miguel Angelo Burati Moralez - RA: 269341
- Pedro Henrique Silvério da Fonseca - RA: 269424
- Victor Augusto Moreira Barbosa - RA: 269391

**Stack:** Java 17 • Spring Boot 3.2 • Spring AMQP • RabbitMQ 3 • PostgreSQL 16 • Docker Compose

---

## Sumário
1. [Arquitetura e Decisões Técnicas](#1-arquitetura-e-decisões-técnicas)
2. [Contrato do Evento](#2-contrato-do-evento)
3. [Matriz de Roteamento](#3-matriz-de-roteamento)
4. [Como Executar a Aplicação](#4-como-executar-a-aplicação)
5. [Roteiro de Apresentação (Passo a Passo)](#5-roteiro-de-apresentação-passo-a-passo)
6. [Perguntas Frequentes da Banca (Defesa)](#6-perguntas-frequentes-da-banca-defesa)

---

## 1. Arquitetura e Decisões Técnicas

```text
                    ┌──────────────────────────────┐
                    │  PRODUCER (Spring Boot :8080)│
                    │  REST /eventos/*             │
                    └──────────────┬───────────────┘
                                   │ publica (topic)
                                   ▼
                    ┌──────────────────────────────┐
                    │ Exchange topic               │
                    │ plataforma.cursos            │
                    └───┬───┬───┬───┬───┬──────────┘
        curso.concluido │   │   │   │   │  # (todos)
        curso.abandonado│   │   │   │   │
        avaliacao.*     │   │   │   │   │
                        ▼   ▼   ▼   ▼   ▼
              ┌─────────┐ ┌─────────┐ ┌──────────────┐ ┌────────────┐ ┌──────────────┐
              │q.certif.│ │q.comunic│ │q.acompanham. │ │q.auditoria │ │q.indicadores │
              │ :8081   │ │ :8082   │ │ :8083        │ │ :8084      │ │ :8085/:8086  │
              └─────────┘ └─────────┘ └──────────────┘ └────────────┘ └──────────────┘
```

### Por que usamos *Topic Exchange*?
Escolhemos o **Topic Exchange** porque ele é o único que nos permite atender todos os requisitos com **uma única exchange**:
- **Seleção por evento exato:** A Certificação recebe só `curso.concluido`.
- **Acompanhamento de família (Wildcard):** O Acompanhamento usa `avaliacao.*` para pegar avaliações enviadas, revisadas e qualquer evento novo futuro (ex: `avaliacao.cancelada`).
- **Recebimento abrangente:** A Auditoria usa `#` para receber absolutamente tudo.

**Comparação com alternativas:**
- *Direct Exchange:* Não suporta curingas. Para acompanhar uma família, precisaríamos criar um binding manual para cada tipo de avaliação. Se surgisse um evento novo, teríamos que alterar a configuração do RabbitMQ na mão.
- *Fanout Exchange:* Ignora a routing key e envia para todos. A Certificação receberia eventos de abandono (o que não deveria) e a Auditoria não teria como filtrar nada.

### Como tratamos a Idempotência?
Todos os consumidores têm uma coluna `evento_id` com restrição `UNIQUE` no banco de dados.
- Ao receber uma mensagem, o consumidor verifica se aquele ID já existe. Se sim, ele apenas dá o `Ack` e ignora o processamento.
- No caso dos **Indicadores** (que tem duas instâncias concorrentes), usamos uma tabela `Processado` com restrição `UNIQUE` e um comando `UPSERT` (`ON CONFLICT DO UPDATE`) no PostgreSQL para garantir que, mesmo se as duas instâncias receberem o evento no mesmo milissegundo, o contador só suba uma vez.

### Confirmação de Consumo (Ack Manual)
Configuramos o `acknowledge-mode: manual` em todos os consumidores. O `basicAck` só é disparado **após** o dado ser salvo com sucesso no banco. Se der erro (ex: banco fora do ar), chamamos o `basicNack` com `requeue=true`, devolvendo a mensagem para a fila para ser reprocessada.

---

## 2. Contrato do Evento

O envelope do evento tem os seguintes campos:

```json
{
  "eventId": "UUID-único",
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
| `curso.concluido` | ✅ | ✅ | ❌ | ✅ | ✅ |
| `curso.abandonado` | ❌ | ✅ | ❌ | ✅ | ❌ |
| `avaliacao.enviada` | ❌ | ❌ | ✅ | ✅ | ❌ |
| `avaliacao.revisada` | ❌ | ✅ | ✅ | ✅ | ❌ |
| `avaliacao.<novo>` | ❌ | ❌ | ✅ | ✅ | ❌ |

---

## 4. Como Executar a Aplicação

1. Suba a infraestrutura:
```bash
docker compose up -d
```
2. No IntelliJ, rode as aplicações nesta ordem (espere uma terminar de subir para rodar a próxima):
   - `ProducerApplication` (Porta 8080)
   - `CertificacaoApplication` (Porta 8081)
   - `ComunicacaoApplication` (Porta 8082)
   - `AcompanhamentoApplication` (Porta 8083)
   - `AuditoriaApplication` (Porta 8084)
3. Para os Indicadores (duas instâncias):
   - Rode `IndicadoresApplication` normal (Porta 8085).
   - Vá em `Run -> Edit Configurations`, duplique a configuração, e adicione o argumento `--server.port=8086`. Rode essa segunda.

---

