package cl.duoc.veterinaria.auth.security;

import cl.duoc.veterinaria.auth.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiracionMinutos;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expiracionMinutos) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // mínimo 32 bytes
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generarToken(Usuario u) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(u.getEmail())
                .claim("rol", u.getRol().name())
                .claim("nombre", u.getNombre())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMinutos * 60_000))
                .signWith(key)
                .compact();
    }

    public long getExpiracionSegundos() {
        return expiracionMinutos * 60;
    }

    public Claims leer(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}