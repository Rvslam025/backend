package cl.duoc.veterinaria.auth.exception;

public class DemasiadosIntentosException extends RuntimeException {
    public DemasiadosIntentosException() {
        super("Demasiados intentos fallidos. Espera 15 minutos e intenta de nuevo");
    }
}
