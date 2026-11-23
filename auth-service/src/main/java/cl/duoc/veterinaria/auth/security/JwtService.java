package cl.duoc.veterinaria.auth.security;

import cl.duoc.veterinaria.auth.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
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
    private final String emisor;
    private final String destinatario;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expiracionMinutos,
                      @Value("${app.jwt.issuer}") String emisor,
                      @Value("${app.jwt.audience}") String destinatario) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); 
        this.expiracionMinutos = expiracionMinutos;
        this.emisor = emisor;
        this.destinatario = destinatario;
    }

    public String generarToken(Usuario u) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(u.getId())) 
                .issuer(emisor)
                .audience().add(destinatario).and()
                .claim("rol", u.getRol().name())
                .claim("nombre", u.getNombre())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMinutos * 60_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public long getExpiracionSegundos() {
        return expiracionMinutos * 60;
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