package br.edu.plataforma.auditoria.config;

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
    public Queue filaAuditoria() {
        return new Queue(RabbitConstants.Q_AUDITORIA, true);
    }

    @Bean
    public Binding bindingAuditoria(Queue filaAuditoria, TopicExchange exchange) {
        // O "#" significa: receba TUDO
        return BindingBuilder.bind(filaAuditoria).to(exchange).with("#");
    }
}