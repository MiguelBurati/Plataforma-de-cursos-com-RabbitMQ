package br.edu.plataforma.certificacao.config;

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
    public Queue filaCertificacao() {
        return new Queue(RabbitConstants.Q_CERTIFICACAO, true);
    }

    @Bean
    public Binding bindingCertificacao(Queue filaCertificacao, TopicExchange exchange) {
        return BindingBuilder.bind(filaCertificacao).to(exchange).with(RabbitConstants.RK_CURSO_CONCLUIDO);
    }
}