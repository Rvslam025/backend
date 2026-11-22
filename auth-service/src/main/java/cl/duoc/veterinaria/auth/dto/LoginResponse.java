package cl.duoc.veterinaria.auth.dto;

public record LoginResponse(String token, String tipo, long expiraEnSegundos, UsuarioResponse usuario) {}