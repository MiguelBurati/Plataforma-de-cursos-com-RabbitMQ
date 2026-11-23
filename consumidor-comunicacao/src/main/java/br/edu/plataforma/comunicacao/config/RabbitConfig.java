package br.edu.plataforma.comunicacao.config;

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
    public Queue filaComunicacao() {
        return new Queue(RabbitConstants.Q_COMUNICACAO, true);
    }

    @Bean
    public Binding bindingConcluido(Queue filaComunicacao, TopicExchange exchange) {
        return BindingBuilder.bind(filaComunicacao).to(exchange).with(RabbitConstants.RK_CURSO_CONCLUIDO);
    }

    @Bean
    public Binding bindingAbandonado(Queue filaComunicacao, TopicExchange exchange) {
        return BindingBuilder.bind(filaComunicacao).to(exchange).with(RabbitConstants.RK_CURSO_ABANDONADO);
    }

    @Bean
    public Binding bindingRevisada(Queue filaComunicacao, TopicExchange exchange) {
        return BindingBuilder.bind(filaComunicacao).to(exchange).with(RabbitConstants.RK_AVALIACAO_REVISADA);
    }
}