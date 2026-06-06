package com.pibank.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pibanklOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PiBank API")
                .description("""
                    API REST del Banco Virtual PiBank.

                    **Autenticación:** Usa JWT Bearer Token. Primero haz login en `/auth/login`
                    y copia el `accessToken` en el botón "Authorize" arriba.

                    **Roles disponibles:**
                    - `CUSTOMER`: Cliente regular del banco
                    - `ADMIN`: Administrador del sistema
                    - `ANALYST`: Analista de riesgos y fraude
                    - `SUPPORT`: Agente de soporte al cliente
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Equipo PiBank")
                    .email("dev@pibank.co"))
                .license(new License()
                    .name("Privado — PiBank S.A.S.")))
            .servers(List.of(
                new Server().url("http://localhost:8080/api/v1").description("Servidor de desarrollo"),
                new Server().url("https://api.pibank.co/api/v1").description("Servidor de producción")
            ))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Ingresa el JWT token obtenido en /auth/login")
                ));
    }
}
