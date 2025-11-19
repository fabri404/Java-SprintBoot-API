# Product API — Documentación Técnica

**Tecnologías**: Java 17 · Spring Boot 3 · Spring Web · Spring Data JPA · Bean Validation · PostgreSQL · Flyway · Maven Wrapper

> API RESTful para la gestión de productos (CRUD completo) con persistencia en PostgreSQL, migraciones Flyway, validaciones y manejo uniforme de errores.

---

## 0. Inicio rápido — de 0 a API funcionando

1. **Clonar el repositorio**

```bash
git clone <URL_DEL_REPO>
cd <carpeta_del_repo>
```

2. **Configurar base de datos PostgreSQL**

Crea la base de datos y el usuario (puede ser `postgres` u otro usuario propio):

```sql
CREATE DATABASE productdb OWNER postgres;
```

3. **Configurar credenciales (variables de entorno o application.yml)**

Ejemplo con variables de entorno:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/productdb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=TU_PASSWORD
export SERVER_PORT=8080
```

4. **Construir y ejecutar la aplicación**

```bash
./mvnw clean package
./mvnw spring-boot:run
# o bien
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

La API quedará disponible en:

```text
http://localhost:8080
```

Endpoints principales:

- `GET /products`
- `GET /products/{id}`
- `POST /products`
- `PUT /products/{id}`
- `DELETE /products/{id}`

---

## 1. API — Endpoints principales y ejemplos rápidos (curl)

En todos los ejemplos se asume que la API corre en `http://localhost:8080`.  
Cada comando `curl` es independiente (no se usan variables tipo `BASE_URL`).

### 1.1 Crear producto — `POST /products`

```bash
curl -i -X POST http://localhost:8080/products   -H "Content-Type: application/json"   -d '{"name":"Mate","description":"Acero inoxidable","price":190.43}'
```

Respuesta esperada (201 Created):

```json
{
  "id": 1,
  "name": "Mate",
  "description": "Acero inoxidable",
  "price": 190.43,
  "createdAt": "2025-11-18T18:30:00Z",
  "updatedAt": "2025-11-18T18:30:00Z"
}
```

### 1.2 Listar productos — `GET /products`

```bash
curl -i http://localhost:8080/products
```

- Devuelve una lista de productos (`List<ProductResponse>`).
- Por defecto se ordenan **cronológicamente**, del más nuevo al más viejo (`createdAt` descendente).

### 1.3 Obtener producto por id — `GET /products/{id}`

```bash
curl -i http://localhost:8080/products/1
```

- Si existe: `200 OK` con el producto.
- Si no existe: `404 Not Found` con un cuerpo JSON de error, por ejemplo:

```json
{
  "error": "PRODUCT_NOT_FOUND",
  "message": "Product with id 999 not found",
  "status": 404,
  "path": "/products/999",
  "timestamp": "2025-11-18T18:35:00Z"
}
```

### 1.4 Actualizar producto — `PUT /products/{id}`

```bash
curl -i -X PUT http://localhost:8080/products/1   -H "Content-Type: application/json"   -d '{"name":"Mate XL","description":"Acero doble pared","price":219.99}'
```

- Si el producto existe: `200 OK` (o `204 No Content`, según implementación) con el producto actualizado.
- El campo `updatedAt` se actualiza a la fecha/hora de la última modificación.
- `createdAt` se mantiene sin cambios.

### 1.5 Eliminar producto — `DELETE /products/{id}`

```bash
curl -i -X DELETE http://localhost:8080/products/1
```

- Si existe y se elimina correctamente: `204 No Content`.
- Si no existe: `404 Not Found` con JSON de error.

---

## 2. Modelo de datos y contrato JSON

### 2.1 Entidad de dominio `Product`

A nivel de negocio, un producto tiene:

- `id`: identificador numérico autogenerado.
- `name`: nombre del producto.
- `description`: descripción del producto.
- `price`: precio con **2 decimales** (NUMERIC(15,2)).
- `createdAt`: fecha/hora de creación.
- `updatedAt`: fecha/hora de última actualización.

### 2.2 DTO de entrada — `CreateProductRequest` / `UpdateProductRequest`

Ejemplo de JSON válido para crear/actualizar:

```json
{
  "name": "Cafetera Express",
  "description": "Cafetera de 20 bares",
  "price": 129999.99
}
```

Reglas clave:

- `name`
  - Obligatorio, no vacío.
  - Longitud mínima 3 caracteres.
  - Debe contener al menos **una letra** (no puede ser solo números).
- `description`
  - Obligatoria, no vacía.
  - Longitud mínima 3 caracteres.
  - Debe contener al menos **una letra**.
- `price`
  - Obligatorio.
  - Mayor o igual que 0.
  - Máximo 2 decimales.
  - Se fuerza internamente a escala 2 con redondeo HALF_UP, por lo que
    valores como `190.43097853056348075` se guardan y devuelven como `190.43`.

### 2.3 DTO de salida — `ProductResponse`

El contrato de salida incluye:

```json
{
  "id": 1,
  "name": "Cafetera Express",
  "description": "Cafetera de 20 bares",
  "price": 129999.99,
  "createdAt": "2025-11-18T18:30:00Z",
  "updatedAt": "2025-11-18T18:45:12Z"
}
```

---

## 3. Requisitos del sistema

- **Sistema operativo**: Linux, macOS o Windows.
- **Java**: JDK 17 (OpenJDK recomendado).
- **Base de datos**: PostgreSQL 14+.
- **Herramientas recomendadas**:
  - DBeaver / pgAdmin (cliente gráfico).
  - curl o HTTPie para pruebas manuales.
  - Docker (opcional) si se desea levantar PostgreSQL en contenedor.

---

## 4. Configuración de la aplicación (Spring Boot)

Archivo principal: `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/productdb}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: ${SERVER_PORT:8080}
```

Se recomienda configurar las credenciales mediante variables de entorno en lugar de hardcodearlas.

---

## 5. Esquema de base de datos y migraciones (Flyway)

### 5.1 Migración inicial

Ejemplo de `V1__create_products.sql`:

```sql
CREATE TABLE IF NOT EXISTS products (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  price NUMERIC(15,2) NOT NULL CHECK (price >= 0),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

- El tipo `NUMERIC(15,2)` limita el precio a 2 decimales.
- `created_at` y `updated_at` mantienen el tracking temporal.
- Cualquier valor de `price` fuera de rango dispara un error coherente con las validaciones del modelo.

### 5.2 Verificación rápida en PostgreSQL

```bash
psql -h localhost -U postgres -d productdb -c "\d+ products"
psql -h localhost -U postgres -d productdb -c "SELECT * FROM products ORDER BY created_at DESC LIMIT 5;"
```

---

## 6. Endpoints — detalle y ejemplos (curl)

### 6.1 POST /products — Crear producto

```bash
curl -i -X POST http://localhost:8080/products   -H "Content-Type: application/json"   -d '{"name":"Mouse","description":"Inalámbrico","price":999.90}'
```

Errores posibles:

- `400 Bad Request` con detalles si:
  - Falta algún campo obligatorio.
  - `name` o `description` no cumplen con las reglas (solo números, muy corto, etc.).
  - `price` tiene más de 2 decimales o no es numérico.

### 6.2 GET /products — Listar productos

```bash
curl -s http://localhost:8080/products | jq .
```

- Devuelve lista ordenada por `createdAt` descendente.
- Si se implementa paginación con `Pageable`, se exponen campos como `content`, `totalElements`, `totalPages`, etc.

Ejemplo con parámetros de paginación:

```bash
curl -s "http://localhost:8080/products?page=0&size=5" | jq .
```

### 6.3 GET /products/{id} — Detalle

```bash
curl -i http://localhost:8080/products/10
```

- `200 OK` si existe.
- `404 Not Found` con body JSON si no existe.

### 6.4 PUT /products/{id} — Actualizar producto

```bash
curl -i -X PUT http://localhost:8080/products/10   -H "Content-Type: application/json"   -d '{"name":"Mouse Pro","description":"Bluetooth 5.0","price":1299.00}'
```

- `200 OK` con el producto actualizado.
- `404 Not Found` si el id no existe.
- `400 Bad Request` si la validación falla.

### 6.5 DELETE /products/{id} — Eliminar producto

```bash
curl -i -X DELETE http://localhost:8080/products/10
```

- `204 No Content` si se elimina correctamente.
- `404 Not Found` si el id no existe.

---

## 7. Validación y manejo de errores

La API utiliza **Bean Validation** (`jakarta.validation`) en los DTOs de entrada y un `@ControllerAdvice` para transformar las excepciones en respuestas JSON consistentes.

Ejemplo de error de validación:

```json
{
  "error": "VALIDATION_ERROR",
  "status": 400,
  "message": "Invalid request body",
  "path": "/products",
  "timestamp": "2025-11-18T18:40:00Z",
  "details": [
    {
      "field": "name",
      "message": "name must contain at least one letter"
    },
    {
      "field": "price",
      "message": "price must have up to 2 decimals"
    }
  ]
}
```

Ejemplo de error de recurso no encontrado:

```json
{
  "error": "PRODUCT_NOT_FOUND",
  "status": 404,
  "message": "Product with id 123 not found",
  "path": "/products/123",
  "timestamp": "2025-11-18T18:42:00Z"
}
```

Este diseño evita mensajes genéricos tipo “malformación” y ayuda a que quien use la API entienda exactamente qué debe corregir.

---

## 8. Razones técnicas: SQL vs NoSQL y elección de PostgreSQL

- El dominio es relacional y sencillo (lista de productos), por lo que SQL encaja bien.
- PostgreSQL:
  - Es open source, robusto y estándar en entornos productivos.
  - Soporta tipos numéricos exactos (`NUMERIC`) para precios.
  - Se integra muy bien con Spring Data JPA.

Para este tipo de API CRUD simple, una base NoSQL sería una sobreingeniería innecesaria.

---

## 9. Estructura de paquetes (sugerida)

```text
ar.edu.challenge01.productapi
├── ProductApiApplication.java
├── entity
│   └── Product.java
├── dto
│   ├── CreateProductRequest.java
│   ├── UpdateProductRequest.java
│   └── ProductResponse.java
├── repository
│   └── ProductRepository.java
├── mapper
│   └── ProductMapper.java
├── web
│   └── ProductController.java
└── exception
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java   // @ControllerAdvice
```

---

## 10. Estructura del proyecto

- `src/main/java`: código fuente de la aplicación.
- `src/main/resources`:
  - `application.yml`: configuración de Spring.
  - `db/migration`: scripts Flyway.
- `src/test/java`: pruebas unitarias e integración.
- `pom.xml`: dependencias, plugins y configuración de build.

---

## 11. Testing — resumen

| Tipo de test       | Capa         | Herramienta            | Objetivo principal                                  |
|--------------------|-------------|------------------------|----------------------------------------------------|
| Unit tests         | Servicio    | JUnit + Mockito        | Regla de negocio y validaciones                    |
| Web slice tests    | Controller  | @WebMvcTest + MockMvc  | Códigos HTTP, payloads, errores                    |
| Integration tests  | Full stack | @SpringBootTest        | Flujo completo con BD (idealmente PostgreSQL real) |

Con esta base, la API queda lista para ser probada por cualquier cliente HTTP (curl, Postman, Insomnia, Swagger UI, etc.) y para ser extendida con nuevas funcionalidades en el futuro.
