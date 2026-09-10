package com.javaesencial.biblioteca.configuracion;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la especificación OpenAPI que springdoc publica en
 * {@code /v3/api-docs} y muestra en Swagger UI ({@code /docs}).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiBiblioteca() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Biblioteca -- Java Esencial")
                        .version("1.0.0")
                        .description("API REST del proyecto Biblioteca del libro Java Esencial, Volumen 2.")
                        .contact(new Contact()
                                .name("Basilio Fajardo Gálvez -- Java Esencial")));
    }
}
