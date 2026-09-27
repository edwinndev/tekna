# Guía de inicio rápido

Instrucciones sencillas para ejecutar y probar la aplicación localmente.

---

### Requisitos previos

- Java 21 instalado.
- Servidor MySQL activo en el puerto `3306`.

---

### Pasos para arrancar

1. Abre una terminal en la raíz del proyecto.
2. Ejecuta el siguiente comando para iniciar con el perfil `dev`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

En Windows (PowerShell o CMD):

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

La aplicación iniciará en el puerto **8081**.

---

### Cómo probar la aplicación

Una vez iniciada, puedes acceder a:

- **Swagger UI (Documentación interactiva)**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI Docs (JSON)**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

---

### Probar endpoints principales

Usa cualquier cliente HTTP (Postman, Bruno, Thunder Client o cURL):

- **Crear producto**: `POST http://localhost:8081/v1/products`
- **Actualizar producto**: `PUT http://localhost:8081/v1/products`
- **Obtener por ID**: `GET http://localhost:8081/v1/products/{id}`
- **Búsqueda y paginación**: `POST http://localhost:8081/v1/products/filter`
- **Eliminar producto (lógico)**: `DELETE http://localhost:8081/v1/products/{id}`
