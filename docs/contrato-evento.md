# Contrato de Eventos — Plataforma de Cursos

Este documento define o contrato JSON trocado entre o produtor e os 5 consumidores
via RabbitMQ. Os nomes de campo aqui definidos são exatamente os mesmos utilizados
nas classes Java (conversão automática via Jackson).

## Regra de uso dos endpoints do Produtor

- **`POST /eventos/*`** (publicação normal): O corpo da requisição deve conter **apenas o objeto `dados`**. O produtor gera automaticamente o `eventId` (UUID), o `tipo` e o `ocorridoEm`.
- **`POST /eventos/republicar`**: O corpo da requisição deve conter o **envelope completo**. Usado exclusivamente para o teste de duplicidade (enviando o mesmo `eventId` duas vezes) e para demonstrar a chegada de um evento novo da família.

---

## 1. Envelope do Evento (Campos Externos)

Todos os eventos têm exatamente os mesmos 4 campos externos. O que muda é o conteúdo interno de `dados`.

| Campo | Tipo | Descrição |
|---|---|---|
| `eventId` | string (UUID) | Identificador único do evento. **Base da idempotência:** o mesmo `eventId` significa a mesma ocorrência e deve ser processado uma única vez pelos consumidores. |
| `tipo` | string | Nome do fato ocorrido. Valores: `CURSO_CONCLUIDO`, `CURSO_ABANDONADO`, `AVALIACAO_ENVIADA`, `AVALIACAO_REVISADA` (e novos fatos da família, ex.: `AVALIACAO_CANCELADA`). |
| `ocorridoEm` | string (ISO-8601 UTC) | Momento exato em que o fato aconteceu no domínio. |
| `dados` | objeto | Campos específicos do tipo do evento (ver tabela abaixo). |

---

## 2. Campos Obrigatórios dentro de `dados`, por tipo

| `tipo` | Campos obrigatórios em `dados` |
|---|---|
| `CURSO_CONCLUIDO` | `alunoId`, `alunoNome`, `cursoId`, `cursoNome`, `cargaHoraria` |
| `CURSO_ABANDONADO` | `alunoId`, `alunoNome`, `cursoId`, `cursoNome` |
| `AVALIACAO_ENVIADA` | `alunoId`, `alunoNome`, `avaliacaoId`, `referencia`, `nota` |
| `AVALIACAO_REVISADA` | `alunoId`, `alunoNome`, `avaliacaoId`, `referencia`, `nota` |
| `AVALIACAO_CANCELADA` (novo fato) | `alunoId`, `alunoNome`, `avaliacaoId`, `referencia` |

---

## 3. Envelopes Completos (Exemplos Canônicos)

### 3.1 `CURSO_CONCLUIDO`
*Efeitos: Certificação (gera certificado), Comunicação (aviso), Auditoria (histórico), Indicadores (incrementa contador).*

```json
{
  "eventId": "11111111-1111-1111-1111-111111111111",
  "tipo": "CURSO_CONCLUIDO",
  "ocorridoEm": "2026-06-10T14:32:00Z",
  "dados": {
    "alunoId": "A-001",
    "alunoNome": "Maria Silva",
    "cursoId": "C-10",
    "cursoNome": "Java Avançado",
    "cargaHoraria": 40
  }
}
```

### 3.2 `CURSO_ABANDONADO`
*Efeitos: Comunicação (aviso de abandono), Auditoria (histórico).*

```json
{
  "eventId": "22222222-2222-2222-2222-222222222222",
  "tipo": "CURSO_ABANDONADO",
  "ocorridoEm": "2026-06-10T15:10:00Z",
  "dados": {
    "alunoId": "A-002",
    "alunoNome": "João Souza",
    "cursoId": "C-10",
    "cursoNome": "Java Avançado"
  }
}
```

### 3.3 `AVALIACAO_ENVIADA`
*Efeitos: Acompanhamento (registra envio), Auditoria (histórico).*

```json
{
  "eventId": "33333333-3333-3333-3333-333333333333",
  "tipo": "AVALIACAO_ENVIADA",
  "ocorridoEm": "2026-06-11T09:00:00Z",
  "dados": {
    "alunoId": "A-001",
    "alunoNome": "Maria Silva",
    "avaliacaoId": "AV-55",
    "referencia": "Módulo 3 - Exercício 2",
    "nota": 8.5
  }
}
```

### 3.4 `AVALIACAO_REVISADA`
*Efeitos: Comunicação (aviso de revisão), Acompanhamento (registra revisão), Auditoria (histórico).*

```json
{
  "eventId": "44444444-4444-4444-4444-444444444444",
  "tipo": "AVALIACAO_REVISADA",
  "ocorridoEm": "2026-06-11T11:20:00Z",
  "dados": {
    "alunoId": "A-001",
    "alunoNome": "Maria Silva",
    "avaliacaoId": "AV-55",
    "referencia": "Módulo 3 - Exercício 2",
    "nota": 9.5
  }
}
```

### 3.5 `AVALIACAO_CANCELADA` (Novo Fato da Família)
*Efeitos: Acompanhamento (recebe via wildcard `avaliacao.*`), Auditoria (recebe via `#`). Prova que o sistema suporta novos eventos da família sem alteração de bindings.*

```json
{
  "eventId": "55555555-5555-5555-5555-555555555555",
  "tipo": "AVALIACAO_CANCELADA",
  "ocorridoEm": "2026-06-12T08:00:00Z",
  "dados": {
    "alunoId": "A-001",
    "alunoNome": "Maria Silva",
    "avaliacaoId": "AV-55",
    "referencia": "Módulo 3 - Exercício 2"
  }
}
```

---

## 4. Regras de `eventId` e Duplicidade

- **Publicação normal (`POST /eventos/*`)**: o produtor gera um UUID novo a cada chamada. A chance de colisão é matematicamente desprezível.
- **Republicação (`POST /eventos/republicar`)**: o `eventId` vem no corpo e é preservado.
- **Comportamento em Duplicidade**: Se um evento com o mesmo `eventId` for publicado novamente (seja intencionalmente ou por falha de rede), todos os consumidores (Certificação, Comunicação, Acompanhamento, Auditoria e Indicadores) identificarão a duplicidade através da constraint `UNIQUE (evento_id)` no banco de dados. Eles devolverão o `Ack` ao RabbitMQ (para remover a mensagem da fila) mas **não duplicarão os efeitos no banco de dados**.