package cl.duoc.veterinaria.auth.service;

import cl.duoc.veterinaria.auth.dto.LoginRequest;
import cl.duoc.veterinaria.auth.dto.LoginResponse;
import cl.duoc.veterinaria.auth.exception.CredencialesInvalidasException;
import cl.duoc.veterinaria.auth.exception.DemasiadosIntentosException;
import cl.duoc.veterinaria.auth.model.Rol;
import cl.duoc.veterinaria.auth.model.Usuario;
import cl.duoc.veterinaria.auth.repository.UsuarioRepository;
import cl.duoc.veterinaria.auth.security.JwtService;
import cl.duoc.veterinaria.auth.security.LimiteIntentosLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository repository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4); // costo bajo solo para que la prueba sea rápida
    private final JwtService jwt = new JwtService("clave-de-prueba-con-mas-de-32-caracteres!!", 60,
            "veterinaria-san-marcos", "veterinaria-frontend");
    private AuthService service;

    @BeforeEach
    void crearServicio() {
        service = new AuthService(repository, encoder, jwt, new LimiteIntentosLogin());
    }

    private Usuario ana(boolean activo) {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNombre("Ana");
        u.setEmail("ana@correo.cl");
        u.setPasswordHash(encoder.encode("clave-segura"));
        u.setRol(Rol.CLIENTE);
        u.setActivo(activo);
        return u;
    }

    @Test
    void loginCorrectoEntregaUnTokenBearer() {
        when(repository.findByEmail("ana@correo.cl")).thenReturn(Optional.of(ana(true)));

        LoginResponse respuesta = service.login(new LoginRequest("  ANA@correo.cl ", "clave-segura"));

        assertEquals("Bearer", respuesta.tipo());
        assertNotNull(respuesta.token());
        assertEquals(Rol.CLIENTE, respuesta.usuario().rol());
    }

    @Test
    void claveIncorrectaYCorreoInexistenteDanElMismoMensaje() {
        when(repository.findByEmail("ana@correo.cl")).thenReturn(Optional.of(ana(true)));
        when(repository.findByEmail("nadie@correo.cl")).thenReturn(Optional.empty());

        var claveMala = assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("ana@correo.cl", "otra-clave")));
        var noExiste = assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("nadie@correo.cl", "clave-segura")));

        assertEquals(claveMala.getMessage(), noExiste.getMessage());
    }

    @Test
    void usuarioDesactivadoNoPuedeIniciarSesion() {
        when(repository.findByEmail("ana@correo.cl")).thenReturn(Optional.of(ana(false)));

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("ana@correo.cl", "clave-segura")));
    }

    @Test
    void bloqueaElCorreoDespuesDeCincoIntentosFallidos() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        LoginRequest intento = new LoginRequest("ana@correo.cl", "adivinando");

        for (int i = 0; i < 5; i++) {
            assertThrows(CredencialesInvalidasException.class, () -> service.login(intento));
        }
        assertThrows(DemasiadosIntentosException.class, () -> service.login(intento));
    }
}
