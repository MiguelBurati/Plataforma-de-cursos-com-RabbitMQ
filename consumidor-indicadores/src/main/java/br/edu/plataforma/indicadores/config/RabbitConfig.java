package br.edu.plataforma.indicadores.config;

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
    public Queue filaIndicadores() {
        return new Queue(RabbitConstants.Q_INDICADORES, true);
    }

    @Bean
    public Binding bindingIndicadores(Queue filaIndicadores, TopicExchange exchange) {
        return BindingBuilder.bind(filaIndicadores).to(exchange).with(RabbitConstants.RK_CURSO_CONCLUIDO);
    }
}