package com.bierchitekt.concerts;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI defineOpenApi() {
        Server server = new Server();

        Info information = new Info()
                .title("Munich Concerts API")
                .version("1.0")
                .description("This API exposes endpoints to manage concerts.");
        return new OpenAPI().info(information).servers(List.of(server));
    }
}