package edu.market.notification.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI para la documentación de la API.
 * Esta clase configura SpringDoc OpenAPI para generar la documentación Swagger de la API.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura la información de la API para la documentación OpenAPI.
     *
     * @return Objeto OpenAPI configurado
     */
    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Servicio de Notificaciones")
                        .description("API para la gestión de notificaciones, plantillas y preferencias de usuario")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("dev@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
