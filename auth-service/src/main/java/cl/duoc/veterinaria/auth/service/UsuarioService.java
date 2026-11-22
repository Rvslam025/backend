package cl.duoc.veterinaria.auth.service;

import cl.duoc.veterinaria.auth.exception.CredencialesInvalidasException;
import cl.duoc.veterinaria.auth.exception.EmailYaRegistradoException;
import cl.duoc.veterinaria.auth.model.Rol;
import cl.duoc.veterinaria.auth.model.Usuario;
import cl.duoc.veterinaria.auth.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /** El rol lo decide siempre el servidor (nunca viene del registro público). */
    @Transactional
    public Usuario crear(String nombre, String email, String clave, Rol rol) {
        String correo = email.trim().toLowerCase();
        if (repository.existsByEmail(correo)) {
            throw new EmailYaRegistradoException();
        }
        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setEmail(correo);
        u.setPasswordHash(passwordEncoder.encode(clave));
        u.setRol(rol);
        try {
            return repository.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            throw new EmailYaRegistradoException();
        }
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return repository.findByEmail(email).orElseThrow(CredencialesInvalidasException::new);
    }
}