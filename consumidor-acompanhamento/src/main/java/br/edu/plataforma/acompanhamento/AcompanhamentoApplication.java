package br.edu.plataforma.acompanhamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import br.edu.plataforma.common.RabbitCommonConfig;

@SpringBootApplication
@EnableRabbit
@Import(RabbitCommonConfig.class)
public class AcompanhamentoApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcompanhamentoApplication.class, args);
    }
}