package br.edu.plataforma.producer.controller;

import br.edu.plataforma.common.Evento;
import br.edu.plataforma.common.RabbitConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    private final RabbitTemplate rabbit;

    public EventoController(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    private Evento publicar(String tipo, String routingKey, Map<String, Object> dados) {
        Evento evento = new Evento(UUID.randomUUID().toString(), tipo, Instant.now(), dados);
        rabbit.convertAndSend(RabbitConstants.EXCHANGE, routingKey, evento);
        return evento;
    }

    @PostMapping("/curso-concluido")
    public Evento cursoConcluido(@RequestBody Map<String, Object> dados) {
        return publicar("CURSO_CONCLUIDO", RabbitConstants.RK_CURSO_CONCLUIDO, dados);
    }

    @PostMapping("/curso-abandonado")
    public Evento cursoAbandonado(@RequestBody Map<String, Object> dados) {
        return publicar("CURSO_ABANDONADO", RabbitConstants.RK_CURSO_ABANDONADO, dados);
    }

    @PostMapping("/avaliacao-revisada")
    public Evento avaliacaoRevisada(@RequestBody Map<String, Object> dados) {
        return publicar("AVALIACAO_REVISADA", RabbitConstants.RK_AVALIACAO_REVISADA, dados);
    }

    @PostMapping("/avaliacao-enviada")
    public Evento avaliacaoEnviada(@RequestBody Map<String, Object> dados) {
        return publicar("AVALIACAO_ENVIADA", RabbitConstants.RK_AVALIACAO_ENVIADA, dados);
    }

    @PostMapping("/republicar")
    public Evento republicar(@RequestBody Evento evento) {
        // Descobre a routing key baseada no tipo do evento
        String rk = switch (evento.getTipo()) {
            case "CURSO_CONCLUIDO" -> RabbitConstants.RK_CURSO_CONCLUIDO;
            case "CURSO_ABANDONADO" -> RabbitConstants.RK_CURSO_ABANDONADO;
            case "AVALIACAO_ENVIADA" -> RabbitConstants.RK_AVALIACAO_ENVIADA;
            case "AVALIACAO_REVISADA" -> RabbitConstants.RK_AVALIACAO_REVISADA;
            // Se for um evento novo (ex: AVALIACAO_CANCELADA), converte para avaliacao.cancelada
            default -> evento.getTipo().toLowerCase().replace('_', '.');
        };

        rabbit.convertAndSend(RabbitConstants.EXCHANGE, rk, evento);
        return evento;
    }

}