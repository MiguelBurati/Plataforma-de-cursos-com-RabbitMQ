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

## 5. Roteiro de Apresentação (Passo a Passo)

> **Nota para o apresentador:** Abra este arquivo no IntelliJ na aba de Markdown, ou imprima. Os endpoints estão no arquivo `testes.http` na raiz do projeto.

### Bloco 1: Arquitetura e Contrato (3 min)
- Mostre o diagrama acima. Explique que o Produtor recebe a chamada REST, monta o JSON e publica no Topic Exchange.
- Mostre a matriz de roteamento. Diga que o Topic Exchange permite uma única exchange servir a todos os 5 consumidores.
- Mostre um exemplo de JSON do contrato.

### Bloco 2: Roteamento (5 min)
Vá no arquivo `testes.http` no IntelliJ. Antes de disparar, diga em voz alta quem **deve** receber.
1. **Dispare o `POST /eventos/curso-concluido`:**
   - *Fala:* "Isso deve ir para Certificação, Comunicação, Auditoria e Indicadores. O Acompanhamento não recebe."
   - *Ação:* Olhe os consoles. Mostre o log em 4 consoles e o silêncio no Acompanhamento.
2. **Dispare o `POST /eventos/curso-abandonado`:**
   - *Fala:* "Agora só Comunicação e Auditoria."
   - *Ação:* Mostre que Certificação e Indicadores ficaram em silêncio.
3. **Dispare o `POST /eventos/avaliacao-revisada`:**
   - *Fala:* "Comunicação, Acompanhamento e Auditoria."
4. **Verificação:** Abra os navegadores para provar o efeito:
   - `localhost:8081/certificados`
   - `localhost:8082/avisos`
   - `localhost:8084/auditoria`

### Bloco 3: Trabalho Concorrente - Indicadores (3 min)
- *Fala:* "Temos duas instâncias do consumidor de Indicadores rodando na mesma porta/fila (8085 e 8086). O RabbitMQ vai dividir o trabalho (round-robin)."
- *Ação:* Dispare 4 vezes o `POST /eventos/curso-concluido` (pode ser com dados diferentes) no `testes.http`.
- *Ação:* Mostre o console dos dois Indicadores. Um processou 2 e o outro processou 2 (ou divisão parecida).
- *Ação:* Acesse `localhost:8085/indicadores` e `localhost:8086/indicadores`. O total é o mesmo (soma 4), pois compartilham o mesmo PostgreSQL.

### Bloco 4: Interrupção e Recuperação (4 min)
- *Ação:* Vá no IntelliJ e clique no botão vermelho de **Stop** na aplicação `ComunicacaoApplication` (Porta 8082).
- *Fala:* "Vamos desligar a Comunicação."
- *Ação:* Dispare um `POST /eventos/curso-concluido`.
- *Fala:* "Vejam que a Certificação e a Auditoria processaram normalmente. O sistema não travou."
- *Ação:* Abra o painel do RabbitMQ (`localhost:15672`), vá em Queues -> `q.comunicacao`. Mostre que há **1 mensagem "Ready"** esperando.
- *Ação:* Volte no IntelliJ e dê **Run** na `ComunicacaoApplication`.
- *Fala:* "Quando o consumidor volta, ele puxa a mensagem que ficou guardada e processa. Nenhuma mensagem foi perdida."

### Bloco 5: Duplicidade e Defesa (5 min)
- *Ação:* No `testes.http`, vá no request "5. TESTE DE DUPLICIDADE" e clique no Play **duas vezes**.
- *Fala:* "Estamos enviando o mesmo evento (mesmo `eventId`) duas vezes."
- *Ação:* Mostre o console da Certificação. Na primeira vez, ele gera. Na segunda, mostra `já processado. Ignorando (Idempotência)`.
- *Ação:* Acesse `localhost:8081/certificados`. Mostre que existe apenas **1** certificado para aquele evento.
- *Fala:* "O Ack só é dado após salvar no banco. E o banco tem constraint UNIQUE pelo `eventId`."

---

## 6. Perguntas Frequentes da Banca (Defesa)

**1. O que acontece se surgir um evento novo, do tipo `AVALIACAO_CANCELADA`?**
R: O produtor mapeia automaticamente para a routing key `avaliacao.cancelada`. O Acompanhamento (que usa binding `avaliacao.*`) e a Auditoria (binding `#`) vão receber a mensagem automaticamente, sem precisar alterar código ou reiniciar o RabbitMQ.
*(Para provar, dispare o request "6. TESTE DE EVENTO NOVO" do `testes.http`)*.

**2. E se um evento não atender a nenhuma regra específica de um consumidor?**
R: A mensagem nem chega na fila dele. O roteamento é resolvido no Exchange. Por exemplo, o evento de Abandono nem entra na fila `q.certificacao`, pois não há binding para ele.

**3. Como vocês garantem que as duas instâncias de Indicadores não dupliquem a contagem?**
R: Usamos uma tabela no banco de dados (`Processado`) com uma constraint `UNIQUE` no `evento_id`. A primeira instância que tentar salvar aquele ID consegue; a segunda recebe uma exceção de banco (`DataIntegrityViolationException`) e aborta o processamento, sem somar no contador.

**4. O que acontece se o RabbitMQ cair?**
R: As filas são declaradas como `Durable` e as mensagens como persistentes (`MessageDeliveryMode.PERSISTENT`). Quando o RabbitMQ volta, as mensagens não confirmadas voltam para a fila.