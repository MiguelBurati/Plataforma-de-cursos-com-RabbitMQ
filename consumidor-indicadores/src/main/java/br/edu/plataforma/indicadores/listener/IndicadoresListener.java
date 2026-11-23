package br.edu.plataforma.indicadores.listener;

import br.edu.plataforma.common.Evento;
import br.edu.plataforma.common.RabbitConstants;
import br.edu.plataforma.indicadores.service.IndicadorService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class IndicadoresListener {

    private final IndicadorService service;

    public IndicadoresListener(IndicadorService service) {
        this.service = service;
    }

    @RabbitListener(queues = RabbitConstants.Q_INDICADORES)
    public void consumir(Evento evento, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            String cursoId = (String) evento.getDados().get("cursoId");
            service.registrarConclusao(evento.getEventId(), cursoId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            System.err.println("Erro ao processar indicador: " + e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}