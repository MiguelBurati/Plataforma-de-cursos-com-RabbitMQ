package br.edu.plataforma.certificacao.listener;

import br.edu.plataforma.certificacao.entity.Certificado;
import br.edu.plataforma.certificacao.repository.CertificadoRepository;
import br.edu.plataforma.common.Evento;
import br.edu.plataforma.common.RabbitConstants;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CertificacaoListener {

    private final CertificadoRepository repository;

    public CertificacaoListener(CertificadoRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitConstants.Q_CERTIFICACAO)
    public void consumir(Evento evento, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            if (repository.existsByEventoId(evento.getEventId())) {
                System.out.println("Evento " + evento.getEventId() + " já processado. Ignorando (Idempotência).");
            } else {
                Certificado cert = new Certificado();
                cert.setEventoId(evento.getEventId());
                cert.setAluno((String) evento.getDados().get("alunoNome"));
                cert.setCurso((String) evento.getDados().get("cursoNome"));

                Number carga = (Number) evento.getDados().get("cargaHoraria");
                cert.setCargaHoraria(carga != null ? carga.intValue() : 0);

                repository.save(cert);
                System.out.println("Certificado gerado para: " + cert.getAluno());
            }

            channel.basicAck(tag, false); // Confirma sucesso

        } catch (Exception e) {
            System.err.println("Erro ao processar: " + e.getMessage());
            channel.basicNack(tag, false, true); // Devolve para a fila em caso de erro
        }
    }
}