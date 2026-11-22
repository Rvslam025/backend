package cl.duoc.veterinaria.auth.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(LocalDateTime timestamp, int status, String error, String mensaje, Map<String, String> campos) {

    public static ApiErrorResponse of(int status, String error, String mensaje) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, mensaje, null);
    }
}