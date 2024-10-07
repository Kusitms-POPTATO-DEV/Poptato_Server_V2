package server.poptato;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PoptatoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoptatoApplication.class, args);
        System.out.println("Hello Poptato Server Team !!");
    }

}
