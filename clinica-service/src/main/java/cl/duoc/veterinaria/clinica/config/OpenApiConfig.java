package cl.duoc.veterinaria.clinica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI openAPI(@Value("${app.openapi.server-url:}") String serverUrl) {
        OpenAPI api = new OpenAPI()
                .info(new Info()
                        .title("Veterinaria San Marcos - clinica-service")
                        .version("1.0.0")
                        .description("""
                                Servicios y citas de la veterinaria.
                                Para las rutas con candado primero hay que hacer login en auth-service (puerto 8081) \
                                y pegar el token en Authorize."""))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
        if (!serverUrl.isBlank()) {
            api.addServersItem(new Server().url(serverUrl).description("Servidor del equipo"));
        }
        return api;
    }
}
