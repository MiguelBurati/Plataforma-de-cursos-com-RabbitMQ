package br.edu.plataforma.acompanhamento.listener;

import br.edu.plataforma.acompanhamento.entity.RegistroAcompanhamento;
import br.edu.plataforma.acompanhamento.repository.RegistroAcompanhamentoRepository;
import br.edu.plataforma.common.Evento;
import br.edu.plataforma.common.RabbitConstants;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AcompanhamentoListener {

    private final RegistroAcompanhamentoRepository repository;

    public AcompanhamentoListener(RegistroAcompanhamentoRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitConstants.Q_ACOMPANHAMENTO)
    public void consumir(Evento evento, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            if (repository.existsByEventoId(evento.getEventId())) {
                System.out.println("Registro de acompanhamento já existe para o evento " + evento.getEventId());
            } else {
                RegistroAcompanhamento registro = new RegistroAcompanhamento();
                registro.setEventoId(evento.getEventId());
                registro.setTipoEvento(evento.getTipo());
                registro.setAluno((String) evento.getDados().get("alunoNome"));
                registro.setReferencia((String) evento.getDados().get("referencia"));

                // A nota pode ou não existir dependendo do evento
                Object notaObj = evento.getDados().get("nota");
                if (notaObj != null) {
                    registro.setNota(((Number) notaObj).doubleValue());
                }

                repository.save(registro);
                System.out.println("Acompanhamento registrado: " + registro.getTipoEvento() + " - Aluno: " + registro.getAluno() + " - Nota: " + registro.getNota());
            }

            channel.basicAck(tag, false);
        } catch (Exception e) {
            System.err.println("Erro no processamento: " + e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}