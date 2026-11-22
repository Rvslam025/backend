package cl.duoc.veterinaria.auth.security;

import cl.duoc.veterinaria.auth.exception.DemasiadosIntentosException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LimiteIntentosLogin {

    private static final int MAXIMO_FALLIDOS = 5;
    private static final Duration BLOQUEO = Duration.ofMinutes(15);

    private record Intentos(int fallidos, Instant desde) {
        boolean vencido() {
            return Instant.now().isAfter(desde.plus(BLOQUEO));
        }
    }

    private final Map<String, Intentos> porCorreo = new ConcurrentHashMap<>();

    public void verificar(String correo) {
        Intentos intentos = porCorreo.get(correo);
        if (intentos == null) {
            return;
        }
        if (intentos.vencido()) {
            porCorreo.remove(correo);
        } else if (intentos.fallidos() >= MAXIMO_FALLIDOS) {
            throw new DemasiadosIntentosException();
        }
    }

    public void registrarFallo(String correo) {
        porCorreo.merge(correo, new Intentos(1, Instant.now()),
                (anterior, nuevo) -> anterior.vencido() ? nuevo : new Intentos(anterior.fallidos() + 1, anterior.desde()));
    }

    public void limpiar(String correo) {
        porCorreo.remove(correo);
    }
}

