package cl.duoc.veterinaria.clinica.exception;


public class DatosInvalidosException extends RuntimeException {
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
