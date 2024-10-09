package server.poptato;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
public class PoptatoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoptatoApplication.class, args);
        System.out.println("Hello Poptato Server Team !!");
    }

}
