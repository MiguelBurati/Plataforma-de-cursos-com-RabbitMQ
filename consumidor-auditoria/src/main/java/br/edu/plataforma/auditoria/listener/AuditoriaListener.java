package br.edu.plataforma.auditoria.listener;

import br.edu.plataforma.auditoria.entity.Auditoria;
import br.edu.plataforma.auditoria.repository.AuditoriaRepository;
import br.edu.plataforma.common.Evento;
import br.edu.plataforma.common.RabbitConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class AuditoriaListener {

    private final AuditoriaRepository repository;
    private final ObjectMapper objectMapper;

    public AuditoriaListener(AuditoriaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitConstants.Q_AUDITORIA)
    public void consumir(Evento evento, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            if (repository.existsByEventoId(evento.getEventId())) {
                System.out.println("Auditoria já registrada para o evento " + evento.getEventId());
            } else {
                Auditoria aud = new Auditoria();
                aud.setEventoId(evento.getEventId());
                aud.setTipo(evento.getTipo());
                aud.setOcorridoEm(evento.getOcorridoEm());
                aud.setRecebidoEm(Instant.now());

                // Converte o objeto Evento de volta para JSON String
                aud.setPayloadJson(objectMapper.writeValueAsString(evento));

                repository.save(aud);
                System.out.println("Auditoria registrada: " + aud.getTipo() + " | ID: " + aud.getEventoId());
            }

            channel.basicAck(tag, false);
        } catch (Exception e) {
            System.err.println("Erro na auditoria: " + e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}