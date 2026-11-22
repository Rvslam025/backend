CREATE TABLE roles (
    id          TINYINT      NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(20)  NOT NULL,
    descripcion VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO roles (nombre, descripcion) VALUES
    ('ADMIN',         'Administrador'),
    ('RECEPCIONISTA', 'Recepcionista'),
    ('CLIENTE',       'Dueño de mascota');

ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol) REFERENCES roles (nombre);
