package cl.duoc.veterinaria.clinica.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;


@Service
public class JwtService {

    private final SecretKey key;
    private final String emisor;
    private final String destinatario;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.issuer}") String emisor,
                      @Value("${app.jwt.audience}") String destinatario) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // mínimo 32 bytes
        this.emisor = emisor;
        this.destinatario = destinatario;
    }

    
    public Claims leer(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(emisor)
                .requireAudience(destinatario)
                .build()
                .parseSignedClaims(token);
        if (!Jwts.SIG.HS256.getId().equals(jws.getHeader().getAlgorithm())) {
            throw new UnsupportedJwtException("Algoritmo no permitido");
        }
        return jws.getPayload();
    }
}
