package br.edu.plataforma.common;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitCommonConfig {

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(RabbitConstants.EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper om) {
        return new Jackson2JsonMessageConverter(om);
    }
}