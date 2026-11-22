package cl.duoc.veterinaria.auth.config;

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
                        .title("Veterinaria San Marcos - API REST")
                        .version("1.0.0")
                        .description("""
                                API del backend: usuarios, autenticación con JWT y roles.
                                Para probar rutas protegidas: haz POST /api/auth/login, copia el campo `token` \
                                y pégalo en el botón **Authorize** (sin escribir la palabra Bearer).
                                Códigos: 400 datos inválidos, 401 sin token/vencido/credenciales malas, \
                                403 rol insuficiente, 409 conflicto (ej. correo duplicado)."""))
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
