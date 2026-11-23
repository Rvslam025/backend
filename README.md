# veterinaria-san-marcos backend
Proyecto Full Stack 2 - Evaluación Parcial 3
Nombre del equipo: Team Rocket


Integrantes:
Matías Araya Venegas, mata.araya@duocuc.cl
Natanael Valenzuela, na.valenzuelap@duocuc.cl
Esteban Acuña, este.acuna@duocuc.cl


Repositorio del frontend: PENDIENTE-LINK-REPO-FRONTEND

Swagger:
- auth-service: http://PENDIENTE-IP:8081/swagger-ui.html
- clinica-service: http://PENDIENTE-IP:8082/swagger-ui.html


## Descripción

Este es el backend de la página de la Veterinaria San Marcos. Lo hicimos con Spring Boot y lo separamos en dos microservicios:

- **auth-service** (puerto 8081): registro, login con JWT y CRUD de usuarios.
- **clinica-service** (puerto 8082): catálogo de servicios y citas.

Los dos usan la misma base de datos PostgreSQL en AWS RDS, pero cada uno tiene su propio esquema (`auth` y `clinica`). Las tablas se crean solas con Flyway al levantar el proyecto.


## Tecnologías

Java 25, Spring Boot 4.1.2, Spring Security, JPA, Flyway, PostgreSQL (RDS), JJWT, Swagger (springdoc) y Docker.


## Roles

- **CLIENTE**: se crea al registrarse. Puede pedir citas, ver las suyas y cancelarlas.
- **RECEPCIONISTA**: ve todas las citas y las puede confirmar, rechazar o cambiar de horario.
- **ADMIN**: lo mismo que recepcionista, más el CRUD de servicios y de usuarios.

El usuario ADMIN inicial se crea al levantar auth-service con los datos del `.env`.


## Endpoints

auth-service (8081)

| Método | Ruta | Quién puede |
|---|---|---|
| POST | /api/auth/register | Cualquiera |
| POST | /api/auth/login | Cualquiera |
| GET | /api/auth/me | Con sesión |
| GET | /api/usuarios | ADMIN |
| POST | /api/usuarios | ADMIN |
| GET | /api/usuarios/{id} | El mismo usuario o ADMIN |
| PUT | /api/usuarios/{id} | El mismo usuario o ADMIN |
| DELETE | /api/usuarios/{id} | ADMIN (desactiva la cuenta) |

clinica-service (8082)

| Método | Ruta | Quién puede |
|---|---|---|
| GET | /api/servicios | Cualquiera |
| GET | /api/servicios/{codigo} | Cualquiera |
| POST, PUT, DELETE | /api/servicios | ADMIN |
| POST | /api/citas | Con sesión |
| GET | /api/citas/mias | Con sesión |
| GET | /api/citas | ADMIN y RECEPCIONISTA |
| GET | /api/citas/{id} | El dueño, ADMIN y RECEPCIONISTA |
| PATCH | /api/citas/{id}/confirmar, /rechazar, /reprogramar | ADMIN y RECEPCIONISTA |
| PATCH | /api/citas/{id}/cancelar | Solo el dueño |

En Swagger se pueden probar todos. Primero hay que hacer login en auth-service, copiar el token y pegarlo en el botón Authorize.


## Reglas que maneja el backend

- Las horas para citas son 9, 10, 11, 12, 15, 16, 17 y 18. Las urgencias también se pueden pedir a las 19, 20 y 21, con $10.000 extra.
- No se pueden tener dos citas en la misma fecha y hora (responde 409).
- El precio y el estado de la cita los pone el backend, no el frontend.
- Un cliente no puede ver ni cancelar citas de otra persona (responde 403).
- Las contraseñas se guardan encriptadas con BCrypt.
- Si alguien se equivoca 5 veces en el login, se bloquea ese correo por 15 minutos.


## Cómo ejecutar el proyecto

1. Copiar `.env.example` como `.env` y completar la contraseña de la base de datos, el `JWT_SECRET` y la clave del admin.
2. Correr `docker compose up -d --build`.
3. Entrar a Swagger en los puertos 8081 y 8082.

Pruebas unitarias: `./mvnw test`
