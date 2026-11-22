CREATE TABLE citas (
    id              BIGSERIAL    PRIMARY KEY,
    
    cliente_id      BIGINT       NOT NULL,
    cliente_nombre  VARCHAR(100) NOT NULL,
    mascota         VARCHAR(60)  NOT NULL,
    codigo_servicio VARCHAR(10)  NOT NULL,
    fecha           DATE         NOT NULL,
    hora            TIME         NOT NULL,
    precio          INTEGER      NOT NULL,
    estado          VARCHAR(15)  NOT NULL DEFAULT 'PENDIENTE',
    creada_en       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_citas_servicio FOREIGN KEY (codigo_servicio) REFERENCES servicios (codigo) ON DELETE RESTRICT,
    CONSTRAINT ck_citas_precio CHECK (precio > 0),
    CONSTRAINT ck_citas_estado CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'REPROGRAMADA', 'RECHAZADA', 'CANCELADA'))
);


CREATE UNIQUE INDEX uk_citas_horario_activo ON citas (fecha, hora)
    WHERE estado IN ('PENDIENTE', 'CONFIRMADA', 'REPROGRAMADA');

CREATE INDEX ix_citas_cliente ON citas (cliente_id);
