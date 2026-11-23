package br.edu.plataforma.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import br.edu.plataforma.common.RabbitCommonConfig;

@SpringBootApplication
@Import(RabbitCommonConfig.class)
public class ProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }
}