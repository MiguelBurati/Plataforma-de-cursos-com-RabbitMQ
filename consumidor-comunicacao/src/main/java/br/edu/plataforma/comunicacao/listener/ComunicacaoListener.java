package br.edu.plataforma.comunicacao.listener;

import br.edu.plataforma.comunicacao.entity.Aviso;
import br.edu.plataforma.comunicacao.repository.AvisoRepository;
import br.edu.plataforma.common.Evento;
import br.edu.plataforma.common.RabbitConstants;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ComunicacaoListener {

    private final AvisoRepository repository;

    public ComunicacaoListener(AvisoRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitConstants.Q_COMUNICACAO)
    public void consumir(Evento evento, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            if (repository.existsByEventoId(evento.getEventId())) {
                System.out.println("Aviso já gerado para o evento " + evento.getEventId());
            } else {
                Aviso aviso = new Aviso();
                aviso.setEventoId(evento.getEventId());
                aviso.setTipoEvento(evento.getTipo());

                String aluno = (String) evento.getDados().get("alunoNome");
                String curso = (String) evento.getDados().get("cursoNome");

                // Regra de negócio para criar a mensagem
                String mensagem = switch (evento.getTipo()) {
                    case "CURSO_CONCLUIDO" -> "Parabéns, " + aluno + "! Você concluiu o curso " + curso + ".";
                    case "CURSO_ABANDONADO" -> "Sentiremos sua falta, " + aluno + ". Você abandonou o curso " + curso + ".";
                    case "AVALIACAO_REVISADA" -> "Olá " + aluno + ", sua avaliação (" + evento.getDados().get("referencia") + ") foi revisada. Nova nota: " + evento.getDados().get("nota");
                    default -> "Aviso sobre o evento: " + evento.getTipo();
                };

                aviso.setMensagem(mensagem);
                repository.save(aviso);
                System.out.println("Aviso registrado: " + mensagem);
            }

            channel.basicAck(tag, false);
        } catch (Exception e) {
            System.err.println("Erro no processamento: " + e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}