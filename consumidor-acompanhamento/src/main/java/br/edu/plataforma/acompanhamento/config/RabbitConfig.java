package br.edu.plataforma.acompanhamento.config;

import br.edu.plataforma.common.RabbitConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue filaAcompanhamento() {
        return new Queue(RabbitConstants.Q_ACOMPANHAMENTO, true);
    }

    @Bean
    public Binding bindingAcompanhamento(Queue filaAcompanhamento, TopicExchange exchange) {
        // O "*" significa: qualquer coisa que comece com "avaliacao."
        return BindingBuilder.bind(filaAcompanhamento).to(exchange).with("avaliacao.*");
    }
}