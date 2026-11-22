package cl.duoc.veterinaria.auth.exception;

public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException() {
        super("El correo ya está registrado");
    }
}