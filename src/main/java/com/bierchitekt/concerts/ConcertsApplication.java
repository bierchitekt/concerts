package com.bierchitekt.concerts;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConcertsApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(ConcertsApplication.class)
                .web(WebApplicationType.NONE);

        try (ConfigurableApplicationContext context = appBuilder.run(args)) {
            ConcertService concertService = context.getBean(ConcertService.class);
            concertService.getNewConcerts();
        }
    }
}
