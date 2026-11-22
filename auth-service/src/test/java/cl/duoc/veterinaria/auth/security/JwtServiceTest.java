package cl.duoc.veterinaria.auth.security;

import cl.duoc.veterinaria.auth.model.Rol;
import cl.duoc.veterinaria.auth.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRETO = "clave-de-prueba-con-mas-de-32-caracteres!!";
    private static final String EMISOR = "veterinaria-san-marcos";
    private static final String DESTINATARIO = "veterinaria-frontend";

    private final JwtService jwt = new JwtService(SECRETO, 60, EMISOR, DESTINATARIO);

    private Usuario ana() {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setNombre("Ana");
        u.setEmail("ana@correo.cl");
        u.setRol(Rol.CLIENTE);
        return u;
    }

    @Test
    void generaYLeeUnTokenValido() {
        Claims claims = jwt.leer(jwt.generarToken(ana()));

        assertEquals("7", claims.getSubject());
        assertEquals("CLIENTE", claims.get("rol", String.class));
        assertEquals(EMISOR, claims.getIssuer());
    }

    @Test
    void rechazaUnTokenAlteradoParaHacerseAdmin() {
        String[] partes = jwt.generarToken(ana()).split("\\.");
        String payloadFalso = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"7\",\"rol\":\"ADMIN\"}".getBytes(StandardCharsets.UTF_8));
        String alterado = partes[0] + "." + payloadFalso + "." + partes[2];

        assertThrows(JwtException.class, () -> jwt.leer(alterado));
    }

    @Test
    void rechazaUnTokenVencido() {
        JwtService vencido = new JwtService(SECRETO, -1, EMISOR, DESTINATARIO); // expiró hace un minuto
        String token = vencido.generarToken(ana());

        assertThrows(ExpiredJwtException.class, () -> vencido.leer(token));
    }

    @Test
    void rechazaUnTokenEmitidoParaOtroDestinatario() {
        JwtService otraApp = new JwtService(SECRETO, 60, EMISOR, "otra-aplicacion");
        String token = otraApp.generarToken(ana());

        assertThrows(JwtException.class, () -> jwt.leer(token));
    }
}
