package cl.duoc.veterinaria.auth.service;

import cl.duoc.veterinaria.auth.dto.UsuarioUpdateRequest;
import cl.duoc.veterinaria.auth.exception.AccesoDenegadoException;
import cl.duoc.veterinaria.auth.exception.ConflictoException;
import cl.duoc.veterinaria.auth.exception.CredencialesInvalidasException;
import cl.duoc.veterinaria.auth.exception.EmailYaRegistradoException;
import cl.duoc.veterinaria.auth.exception.RecursoNoEncontradoException;
import cl.duoc.veterinaria.auth.model.Rol;
import cl.duoc.veterinaria.auth.model.Usuario;
import cl.duoc.veterinaria.auth.repository.UsuarioRepository;
import cl.duoc.veterinaria.auth.security.UsuarioAutenticado;
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

    //El rol lo decide siempre el servidor (nunca viene del registro público). 
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
        return guardar(u);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll();
    }

    /** Datos de quien tiene el token. Si la cuenta se desactivó, el token ya no sirve. */
    @Transactional(readOnly = true)
    public Usuario perfil(Long id) {
        Usuario u = repository.findById(id).orElseThrow(CredencialesInvalidasException::new);
        if (!u.isActivo()) {
            throw new CredencialesInvalidasException();
        }
        return u;
    }

    @Transactional(readOnly = true)
    public Usuario ver(Long id, UsuarioAutenticado yo) {
        verificarDuenoOAdmin(id, yo);
        return buscar(id);
    }

    @Transactional
    public Usuario actualizar(Long id, UsuarioUpdateRequest req, UsuarioAutenticado yo) {
        verificarDuenoOAdmin(id, yo);
        if (!yo.esAdmin() && (req.rol() != null || req.activo() != null)) {
            throw new AccesoDenegadoException("Solo un administrador puede cambiar el rol o el estado de una cuenta");
        }
        boolean quitaSuRol = req.rol() != null && req.rol() != Rol.ADMIN;
        if (yo.esAdmin() && id.equals(yo.id()) && (quitaSuRol || Boolean.FALSE.equals(req.activo()))) {
            // Si el último ADMIN se quitara el rol, nadie podría volver a administrar
            throw new ConflictoException("Un administrador no puede quitarse su propio rol ni desactivarse");
        }

        Usuario u = buscar(id);
        String correo = req.email().trim().toLowerCase();
        if (!correo.equals(u.getEmail()) && repository.existsByEmail(correo)) {
            throw new EmailYaRegistradoException();
        }
        u.setNombre(req.nombre().trim());
        u.setEmail(correo);
        if (req.clave() != null) {
            u.setPasswordHash(passwordEncoder.encode(req.clave()));
        }
        if (req.rol() != null) {
            u.setRol(req.rol());
        }
        if (req.activo() != null) {
            u.setActivo(req.activo());
        }
        return guardar(u);
    }

    /** Borrado lógico: la cuenta queda guardada, pero ya no puede iniciar sesión. */
    @Transactional
    public void desactivar(Long id, UsuarioAutenticado yo) {
        if (id.equals(yo.id())) {
            throw new ConflictoException("No puedes desactivar tu propia cuenta");
        }
        Usuario u = buscar(id);
        u.setActivo(false);
        repository.save(u);
    }

    private Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    /** Regla de la Guía 19: nadie ve ni cambia datos ajenos cambiando el id en la URL. */
    private void verificarDuenoOAdmin(Long id, UsuarioAutenticado yo) {
        if (!yo.esAdmin() && !id.equals(yo.id())) {
            throw new AccesoDenegadoException("No puedes ver ni modificar la cuenta de otra persona");
        }
    }

    private Usuario guardar(Usuario u) {
        try {
            return repository.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            // Dos registros simultáneos con el mismo correo: lo detiene el UNIQUE de la tabla
            throw new EmailYaRegistradoException();
        }
    }
}