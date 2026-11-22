package cl.duoc.veterinaria.auth.service;

import cl.duoc.veterinaria.auth.dto.UsuarioUpdateRequest;
import cl.duoc.veterinaria.auth.exception.AccesoDenegadoException;
import cl.duoc.veterinaria.auth.exception.ConflictoException;
import cl.duoc.veterinaria.auth.exception.EmailYaRegistradoException;
import cl.duoc.veterinaria.auth.model.Rol;
import cl.duoc.veterinaria.auth.repository.UsuarioRepository;
import cl.duoc.veterinaria.auth.security.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    private UsuarioService service;

    private final UsuarioAutenticado cliente = new UsuarioAutenticado(1L, "Ana", "CLIENTE");
    private final UsuarioAutenticado admin = new UsuarioAutenticado(9L, "Administrador", "ADMIN");

    @BeforeEach
    void crearServicio() {
        service = new UsuarioService(repository, new BCryptPasswordEncoder(4));
    }

    @Test
    void registroConCorreoRepetidoDa409() {
        when(repository.existsByEmail("ana@correo.cl")).thenReturn(true);

        assertThrows(EmailYaRegistradoException.class,
                () -> service.crear("Ana", "ANA@correo.cl", "clave-segura", Rol.CLIENTE));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void clienteNoPuedeVerLaCuentaDeOtroCambiandoElId() {
        assertThrows(AccesoDenegadoException.class, () -> service.ver(2L, cliente));
        verifyNoInteractions(repository);
    }

    @Test
    void clienteNoPuedeDarseElRolAdmin() {
        var req = new UsuarioUpdateRequest("Ana", "ana@correo.cl", null, Rol.ADMIN, null);

        assertThrows(AccesoDenegadoException.class, () -> service.actualizar(1L, req, cliente));
    }

    @Test
    void adminNoPuedeQuitarseSuPropioRol() {
        var req = new UsuarioUpdateRequest("Administrador", "admin@correo.cl", null, Rol.CLIENTE, null);

        assertThrows(ConflictoException.class, () -> service.actualizar(9L, req, admin));
    }
}
