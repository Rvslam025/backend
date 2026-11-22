CREATE TABLE roles (
    id          SMALLSERIAL  PRIMARY KEY,
    nombre      VARCHAR(20)  NOT NULL,
    descripcion VARCHAR(100) NOT NULL,
    CONSTRAINT uk_roles_nombre UNIQUE (nombre)
);

INSERT INTO roles (nombre, descripcion) VALUES
    ('ADMIN',         'Administrador'),
    ('RECEPCIONISTA', 'Recepcionista'),
    ('CLIENTE',       'Dueño de mascota');

ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol) REFERENCES roles (nombre);

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_email_minusculas CHECK (email = LOWER(email));