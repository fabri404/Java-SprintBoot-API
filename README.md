# Product API — Documentación Técnica

**Tecnologías**: Java 17 · Spring Boot 3 · Spring Web · Spring Data JPA · Bean Validation · PostgreSQL · Flyway · Maven Wrapper

> Objetivo: exponer una API REST para la gestión de productos con CRUD completo, validaciones, paginación opcional, migraciones de esquema y manejo de errores uniforme. Esta guía permite reproducir el entorno y ejecutar la aplicación de punta a punta.
> 

---

## 0. Inicio rápido — paso a paso (de 0 a API funcionando)

1. **Clonar el repositorio**

```bash
git clone <URL_DEL_REPO>
cd <carpeta_del_repo>

```

1. **Instalar dependencias del sistema** (si no las tienes)
- Java: `sudo apt install -y openjdk-17-jdk && java -version`
- PostgreSQL: `sudo apt install -y postgresql postgresql-contrib && psql --version`
- (Opcional) HTTPie y jq: `sudo apt install -y httpie jq`
1. **Crear base de datos y usuario** (ajusta credenciales propias)

```bash
sudo -u postgres psql -c "CREATE USER YOUR_DB_USER WITH PASSWORD 'YOUR_DB_PASSWORD';"
sudo -u postgres psql -c "CREATE DATABASE productdb OWNER YOUR_DB_USER;"

```

1. **Exportar variables de entorno** (recomendado)

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/productdb
export SPRING_DATASOURCE_USERNAME=YOUR_DB_USER
export SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD
export SERVER_PORT=8080

```

1. **Compilar y verificar**

```bash
./mvnw -U clean package

```

> El Maven Wrapper (./mvnw) baja las dependencias a ~/.m2/repository y usa la versión de Maven declarada en el wrapper para garantizar reproducibilidad.
> 
1. **Aplicar migraciones y arrancar**

```bash
./mvnw spring-boot:run
# o bien
java -jar target/product-api-0.0.1-SNAPSHOT.jar

```

La app queda en `http://localhost:${SERVER_PORT:-8080}` y expone `/products`.

1. **Probar la API**

```bash
http POST :8080/products name='Mate' description='Acero' price:=21999.00
http :8080/products

```

> Si algo falla, revisa la sección 11. Solución de problemas.
> 

---

## 1. Introducción conceptual

### 1.1 ¿Qué es una API?

Una **API** (Application Programming Interface) es una interfaz de comunicación entre programas. En la web suele ser una **API HTTP**: el cliente envía una petición a una **URL** y recibe una **respuesta** (usualmente **JSON**), siguiendo un contrato definido.

### 1.2 ¿Qué es una API RESTful?

**REST** es un estilo de arquitectura para diseñar APIs que:

- Modela el dominio como **recursos** (p. ej., `products`).
- Usa **URLs** para identificar recursos y **métodos HTTP** para operar.
- Es **stateless**: cada petición es autosuficiente.
- Emplea **códigos HTTP** coherentes y formatos estándar como **JSON**.

Recursos y endpoints solicitados (CRUD):

- `POST /products`
- `GET /products`
- `GET /products/{id}`
- `PUT /products/{id}`
- `DELETE /products/{id}`

### 1.3 Recursos, endpoints y representación

- **Recurso**: entidad del negocio (Producto).
- **Endpoint**: **método + ruta** (ej.: `GET /products/{id}`).
- **Representación**: JSON enviado/recibido (contrato).

Ejemplo de **Product**:

```json
{
  "id": 1,
  "name": "Cafetera",
  "description": "Express 20 bares",
  "price": 129999.99,
  "created_at": "2025-10-31T20:00:00Z"
}

```

### 1.4 Métodos HTTP

- **GET**: lectura (seguro e idempotente).
- **POST**: creación (no idempotente).
- **PUT**: sustitución/actualización **total** (idempotente).
- **DELETE**: eliminación (idempotente semántico).

### 1.5 Códigos de estado HTTP

- **200 OK**: lectura/actualización exitosa (GET/PUT).
- **201 Created**: creación exitosa (POST) + header `Location`.
- **204 No Content**: borrado exitoso (DELETE) o actualización sin cuerpo.
- **400 Bad Request**: JSON inválido/validaciones fallidas.
- **404 Not Found**: `id` inexistente.
- **409 Conflict** *(opcional)*: conflicto de negocio (duplicados, etc.).
- **422 Unprocessable Entity** *(opcional)*: semántica inválida.
- **500 Internal Server Error**: error no controlado.

### 1.6 JSON, serialización y contrato

- **JSON** es el formato de intercambio (obligatorio).
- Spring usa **Jackson** para convertir objetos Java ⇄ JSON (**serialización**).
- La forma del JSON es el **API contract**; debe documentarse y mantenerse estable.

### 1.7 Arquitectura lógica en Spring Boot

- **Controller**: maneja HTTP (rutas, status codes, DTOs).
- **Service**: reglas y validaciones de negocio.
- **Repository**: acceso a datos (JPA/JDBC).
- **Entity / DTO**: modelo de persistencia vs. modelo expuesto por la API.

### 1.8 Persistencia

El proyecto utiliza **PostgreSQL** con **Spring Data JPA**. Alternativamente podría usarse JDBC puro, pero JPA acelera el CRUD.

### 1.9 Migraciones (Flyway)

Las migraciones versionadas (`db/migration/V1__...sql`, `V2__...sql`, ...) permiten **recrear** la base de forma determinista en cualquier entorno. Se aplican automáticamente al iniciar la app.

### 1.10 Validación de datos

Se aplican anotaciones de Bean Validation: `@NotBlank`, `@PositiveOrZero`, etc. Los errores se devuelven como `400 Bad Request` con cuerpo JSON consistente.

### 1.11 Paginación/ordenamiento

`GET /products?page=0&size=10&sort=createdAt,desc` — se soporta `Pageable` de Spring (opcional, recomendado).

### 1.12 Manejo uniforme de errores

`@ControllerAdvice` traduce excepciones a respuestas JSON con campos: `timestamp`, `status`, `error`, `message`, `path`.

---

## 2. Requisitos del sistema

### 2.1 Sistema operativo

- Linux (Ubuntu/Zorin), macOS o Windows 10/11.

### 2.2 Software necesario

- **JDK 17** (OpenJDK recomendado)
- **Maven Wrapper** (incluido en el repo: `./mvnw`)
- **PostgreSQL 14+** (recomendado 15/16/18)
- **psql** (cliente de línea de comandos de PostgreSQL)
- Herramientas opcionales:
    - **HTTPie** o **curl** (pruebas manuales de la API)
    - **jq** (formateo de JSON en CLI)
    - **Docker / Docker Compose** (alternativa para DB)
    - **DBeaver** o **pgAdmin** (cliente gráfico SQL)

---

## 3. Instalación (paso a paso)

### 3.1 Instalar Java 17 (Ubuntu/Zorin)

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
java -version

```

### 3.2 Instalar PostgreSQL (Ubuntu/Zorin)

```bash
sudo apt install -y postgresql postgresql-contrib
sudo systemctl enable --now postgresql
psql --version

```

### 3.3 Crear base de datos y usuario

> Reemplaza YOUR_DB_USER / YOUR_DB_PASSWORD por credenciales propias (no uses DNI ni datos sensibles).
> 

```bash
sudo -u postgres psql -c "CREATE USER YOUR_DB_USER WITH PASSWORD 'YOUR_DB_PASSWORD';"
sudo -u postgres psql -c "CREATE DATABASE productdb OWNER YOUR_DB_USER;"

```

### 3.4 Variables de entorno (recomendado)

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/productdb
export SPRING_DATASOURCE_USERNAME=YOUR_DB_USER
export SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD
export SERVER_PORT=8080

```

### 3.5 Clonado y build

```bash
# dentro del directorio del proyecto
./mvnw -U clean package

```

### 3.6 Arranque de la aplicación

```bash
./mvnw spring-boot:run
# o
java -jar target/product-api-0.0.1-SNAPSHOT.jar

```

La API queda disponible en `http://localhost:${SERVER_PORT:-8080}`.

---

## 4. Configuración de la aplicación

Archivo `src/main/resources/application.yml` (perfil por defecto **dev**):

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

> Perfiles: puedes definir application-dev.yml y application-prod.yml y activar con --spring.profiles.active=dev.
> 

---

## 5. Esquema de base y migraciones (Flyway)

### 5.1 Migración inicial `V1__create_products.sql`

```sql
CREATE TABLE IF NOT EXISTS products (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  price NUMERIC(15,2) NOT NULL CHECK (price >= 0),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

```

> Al iniciar la app, Flyway validará/aplicará las migraciones en orden.
> 

### 5.2 Comandos útiles (psql)

```bash
# probar conexión
PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h localhost -p 5432 -U $SPRING_DATASOURCE_USERNAME -d productdb -c "SELECT now();"

# describir tabla
PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h localhost -U $SPRING_DATASOURCE_USERNAME -d productdb -c "\\d+ public.products"

# ver datos
PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h localhost -U $SPRING_DATASOURCE_USERNAME -d productdb -c "SELECT * FROM products ORDER BY id DESC LIMIT 10;"

# historial de Flyway
PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h localhost -U $SPRING_DATASOURCE_USERNAME -d productdb -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"

```

---

## 6. Endpoints y ejemplos

### 6.1 Rutas

- `POST /products`
- `GET /products`
- `GET /products/{id}`
- `PUT /products/{id}`
- `DELETE /products/{id}`

### 6.2 Ejemplos con **curl**

```bash
# Base (ajustar si usas prefijo /api/v1)
export BASE="http://localhost:8080"

# Crear
curl -i -X POST "$BASE/products" -H 'Content-Type: application/json' \
  -d '{"name":"Mate","description":"Acero","price":21999.00}'

# Listar
curl -s "$BASE/products" | jq .

# Ver uno
curl -s "$BASE/products/1" | jq .

# Actualizar total
curl -i -X PUT "$BASE/products/1" -H 'Content-Type: application/json' \
  -d '{"name":"Mate XL","description":"Doble pared","price":25999.00}'

# Borrar
curl -i -X DELETE "$BASE/products/1"

```

### 6.3 Ejemplos con **HTTPie**

```bash
http POST $BASE/products name='Mouse' description='Inalámbrico' price:=999.90
http $BASE/products
http $BASE/products/1
http PUT $BASE/products/1 name='Mouse Pro' description='BT 5.0' price:=1299.00
http DELETE $BASE/products/1

```

### 6.4 Paginación (opcional)

```
GET /products?page=0&size=10&sort=createdAt,desc

```

Respuesta típica `Page<>`: `content`, `totalElements`, `totalPages`, `number`, etc.

---

## 7. Validación y manejo de errores

### 7.1 Validaciones

En DTO/Entity:

```java
@NotBlank private String name;
@NotBlank private String description;
@PositiveOrZero private BigDecimal price;

```

### 7.2 Respuesta de error unificada

Ejemplo de cuerpo de error:

```json
{
  "timestamp":"2025-10-31T23:00:00Z",
  "status":404,
  "error":"Not Found",
  "message":"Product 999 not found",
  "path":"/products/999"
}

```

### 7.3 Plantilla de `@ControllerAdvice`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    var errors = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage, (a,b)->a));
    return Map.of(
      "timestamp", Instant.now().toString(),
      "status", 400,
      "error", "Bad Request",
      "message", errors,
      "path", req.getRequestURI()
    );
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, Object> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    return Map.of(
      "timestamp", Instant.now().toString(),
      "status", 404,
      "error", "Not Found",
      "message", ex.getMessage(),
      "path", req.getRequestURI()
    );
  }

  @ExceptionHandler(ConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, Object> handleConflict(ConflictException ex, HttpServletRequest req) {
    return Map.of(
      "timestamp", Instant.now().toString(),
      "status", 409,
      "error", "Conflict",
      "message", ex.getMessage(),
      "path", req.getRequestURI()
    );
  }
}

```

---

## 8. Pruebas automatizadas

### 8.1 Unit tests

- Prueban la lógica de **Service** con dependencias mockeadas.
- Herramientas: JUnit 5, Mockito.

### 8.2 Web layer tests

- `@WebMvcTest(ProductController.class)` + `MockMvc` para verificar status y contrato JSON.

### 8.3 Integration tests

- `@SpringBootTest` con Testcontainers (PostgreSQL) para pruebas end-to-end.

---

## 9. Docker (opcional)

`docker-compose.yml` mínimo para PostgreSQL:

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: productdb
      POSTGRES_USER: YOUR_DB_USER
      POSTGRES_PASSWORD: YOUR_DB_PASSWORD
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:

```

> Con la DB en marcha, ejecuta la app con tus variables SPRING_DATASOURCE_* apuntando a localhost:5432.
> 

---

## 10. Operación y catálogo de comandos

### 10.1 Build/Run

```yaml
iniciar_con_mvnw:
  cmd: "./mvnw spring-boot:run"
  desc: "Ejecuta la app con el Maven Wrapper del proyecto."
iniciar_con_mvn:
  cmd: "mvn spring-boot:run"
  desc: "Ejecuta la app usando Maven instalado en el sistema."
compilar_sin_tests:
  cmd: "./mvnw -DskipTests clean package"
  desc: "Compila y empaqueta el JAR omitiendo tests."
compilar_con_tests:
  cmd: "./mvnw clean package"
  desc: "Compila, corre tests y empaqueta el JAR."
ejecutar_jar:
  cmd: "java -jar target/product-api-0.0.1-SNAPSHOT.jar"
  desc: "Corre el JAR ya construido."
forzar_update_dependencias:
  cmd: "./mvnw -U clean package"
  desc: "Fuerza actualización de metadatos/SNAPSHOTs al compilar."

```

### 10.2 Variables útiles

```yaml
base_api:
  cmd: 'export BASE="http://localhost:8080"'
  desc: "Variable de entorno con la base de la API."

```

### 10.3 CRUD con curl

*(ver sección 6.2)*

### 10.4 PostgreSQL

*(ver sección 5.2)*

### 10.5 Migraciones (Flyway)

```yaml
crear_migracion_Vn:
  cmd: 'touch src/main/resources/db/migration/V2__create_categories.sql'
  desc: "Crea una nueva migración."
aplicar_migraciones:
  cmd: "./mvnw spring-boot:run"
  desc: "Levanta la app; Flyway valida y migra."
rehacer_ultima_migracion_dev:
  cmd: |
    # Desarrollo: revertir última entrada (hacer rollback manual del esquema)
    PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h localhost -U $SPRING_DATASOURCE_USERNAME -d productdb \
    -c "DELETE FROM flyway_schema_history WHERE installed_rank = (SELECT max(installed_rank) FROM flyway_schema_history);"
  desc: "Desmarca la última migración (uso con cuidado en dev)."

```

### 10.6 Diagnóstico rápido

```yaml
ver_logs_app:
  cmd: "./mvnw spring-boot:run -Dspring-boot.run.arguments=\"--logging.level.root=INFO --logging.level.org.springframework=INFO --logging.level.org.hibernate.SQL=DEBUG --logging.level.org.hibernate.type.descriptor.sql=TRACE\""
  desc: "Arranca con logs detallados."
puerto_ocupado:
  cmd: "ss -ltnp | grep :8080  # o: lsof -i :8080"
  desc: "Chequea si el 8080 está ocupado."
matar_proceso_en_8080:
  cmd: "kill -9 <PID>"
  desc: "Libera el puerto 8080 (cuidado)."

```

### 10.7 Swagger/OpenAPI (opcional)

- UI: `http://localhost:8080/swagger-ui/index.html`
- Agregar dependencia `springdoc-openapi-starter-webmvc-ui` en `pom.xml`.

---

## 11. Solución de problemas (troubleshooting)

### 11.1 Compilación/Build (Maven)

- **`error: release version 17 not supported`** → Selecciona JDK 17: configura `JAVA_HOME`, `update-alternatives` y verifica `java -version`/`javac -version`.
- **`maven-wrapper.properties: No such file`** → Usa `mvn` del sistema o genera wrapper con `mvn -N wrapper`.
- **Dependencias duplicadas** → Elimina duplicados en `pom.xml` y ejecuta `./mvnw -U clean package`.
- **Símbolos/miembros duplicados** → Quita implementaciones repetidas en código.

### 11.2 Arranque/Runtime

- **`Port 8080 was already in use`** → Identifica y mata proceso (`ss`/`lsof`) o usa `-server.port=8081`.
- **`ConflictingBeanDefinitionException`** → Hay dos beans con el mismo nombre (p. ej., dos `ProductController`). Deja uno.
- **`Unsupported Database: PostgreSQL 18.0` (Flyway)** → Actualiza a versión reciente (ej. `11.15.0`) e incluye `flyway-database-postgresql`.
- **`Schema-validation: missing column [X]`** → Entidad no coincide con esquema. Crea migración `Vn__...` con `ALTER TABLE` o ajusta la entidad.

### 11.3 Conexión a BD

- **`Peer authentication failed for user "postgres"`** → Usa `PGPASSWORD=... psql -h localhost -U postgres ...` o configura un usuario/password propios.
- Verifica `spring.datasource.url` apunta a `jdbc:postgresql://localhost:5432/productdb`.

### 11.4 Flyway

- Para **nuevas columnas/tablas**, agrega `V2__...sql`, `V3__...sql`, etc. No edites migraciones ya aplicadas.
- En **desarrollo**, para “volver atrás”: dropea el esquema público y reinicia (no en producción).

### 11.5 API (cURL)

- **500** → Revisa mappers/DTOs/Service; activa logs DEBUG; mapea excepciones a 400/404/409 en lugar de 500.
- **404** → ID inexistente; lanza `NotFoundException` y manéjala en `@ControllerAdvice`.
- **400** → JSON inválido o violación de Bean Validation.
- **409** → Conflictos de negocio (duplicados únicos); valida en Service y responde 409.

### 11.6 Datos “que desaparecen”

- Asegura estar consultando **la misma** instancia de DB que usa la app (host, puerto, base). No mezcles contenedores con local.

---

## 12. Razones técnicas: SQL vs NoSQL y elección de PostgreSQL

### 12.1 ¿Por qué **SQL** para este proyecto?

- **Relacionalidad y consistencia**: Productos con constraints (NOT NULL, CHECK `price >= 0`) y potenciales relaciones futuras (categorías, stocks, órdenes) se benefician de **ACID** y tipos fuertes.
- **Consultas ad hoc** ricas: agregaciones, joins y paginación con costo predecible.
- **Evolución controlada**: cambios de esquema versionados con **Flyway**.

**Ventajas de SQL**

- Integridad referencial, transacciones y normalización.
- Ecosistema maduro (JDBC/JPA, migraciones, clientes, monitoreo).

**Desventajas**

- Menor flexibilidad de esquema que NoSQL.
- Escalado horizontal suele requerir estrategias adicionales (sharding/replicación).

**Cuándo elegir NoSQL**

- Esquemas altamente dinámicos, documentos heterogéneos.
- Altas tasas de escritura y lectura distribuidas globalmente con tolerancia a particiones.
- Modelos de acceso simples (clave-valor/documento) y requerimientos de baja latencia.

### 12.2 ¿Por qué **PostgreSQL** sobre otras opciones?

- **Standards SQL y extensiones**: tipos avanzados (JSONB), índices potentes (GIN/GIST), funciones y vistas materializadas.
- **Integridad y performance**: óptimo balance OLTP; excelente para CRUD con validaciones.
- **Herramientas**: amplia disponibilidad de clientes (DBeaver/pgAdmin), soporte en Testcontainers, imágenes oficiales Docker.
- **Comparativa breve**:
    - vs **MySQL/MariaDB**: Postgres ofrece semántica SQL más estricta, `CHECK` robusto, tipos/índices avanzados y JSONB nativo con operadores potentes.
    - vs **H2**: H2 es en memoria/embebida, útil para tests; no es una base de producción.
    - vs **MongoDB** (NoSQL): schema-less y documentos flexibles, pero carece de joins/ACID completos por defecto para muchos casos; para este dominio relacional, Postgres se ajusta mejor.

---

## 13. Seguridad y buenas prácticas

- **Nunca** hardcodees credenciales en el repo. Usa **variables de entorno** o **Secret Managers**.
- Separa **perfiles** (`dev`, `prod`) y configura CORS si habrá frontends en dominios distintos.
- Deshabilita `open-in-view` en JPA (ya configurado) para evitar fugas del contexto de persistencia.
- Loguea a niveles apropiados y evita volcar datos sensibles.

---

## 14. Estructura de paquetes (sugerida)

```
com.example.productapi
├─ config/           # CORS, Swagger, etc.
├─ controller/       # ProductController
├─ dto/              # ProductRequest, ProductResponse
├─ entity/           # Product
├─ exception/        # NotFoundException, ConflictException
├─ repository/       # ProductRepository (JpaRepository)
├─ service/          # ProductService
└─ util/             # mappers, helpers

```

---

## 15. Estructura de proyecto — qué hay en cada carpeta y por qué

> Objetivo: alta cohesión y bajo acoplamiento. Cada capa tiene una responsabilidad única, lo que facilita pruebas, mantenimiento y escalabilidad.
> 

### 15.1 Paquetes Java

```
com.example.productapi
├─ config/           # Configuración transversal: CORS, Swagger/OpenAPI, mapeos Jackson, etc.
├─ controller/       # Adaptadores HTTP: @RestController, rutas y status codes; solo orquestan.
├─ dto/              # Contratos de entrada/salida (requests/responses). Evitan exponer entidades JPA.
├─ entity/           # Modelo de persistencia (JPA @Entity): mapea 1:1 a tablas.
├─ exception/        # Excepciones de dominio (NotFoundException, ConflictException, etc.).
├─ repository/       # Interfaces JPA (JpaRepository<Product, Long>), queries derivadas, paginación.
├─ service/          # Reglas de negocio, validaciones, transacciones; coordina repositorios.
└─ util/             # Mappers/convertidores, helpers (evitar lógica de negocio aquí).

```

**Rationale**

- `controller` expone **contratos HTTP**; no contiene lógica de negocio ni acceso a datos.
- `service` centraliza **reglas** y valida **invariantes** (p. ej., precio ≥ 0 más allá de Bean Validation).
- `repository` encapsula la **persistencia** y facilitan testear `service` con dobles/mocks.
- `dto` define el **contrato público** (API contract) y desacopla la capa web del modelo de BD.

### 15.2 Archivos raíz imprescindibles

```
.
├─ pom.xml                         # Coordinadas Maven, dependencias y plugins (incluye BOM de Spring).
├─ mvnw / mvnw.cmd                 # Maven Wrapper para Linux/macOS y Windows.
├─ .mvn/wrapper/                   # Metadata del wrapper (versiones/URL de Maven).
├─ src/
│  ├─ main/
│  │  ├─ java/com/example/productapi/
│  │  │  ├─ ProductApiApplication.java   # Clase @SpringBootApplication (entry point).
│  │  │  └─ (paquetes listados en 14.1)
│  │  └─ resources/
│  │     ├─ application.yml              # Config por perfil; NO subir credenciales reales.
│  │     └─ db/migration/
│  │        └─ V1__create_products.sql   # Migraciones Flyway versionadas (V2__, V3__, ...).
│  └─ test/
│     └─ java/com/example/productapi/    # Tests unitarios, web y de integración.
└─ .gitignore                      # Ignora /target, /logs, .idea, .vscode, etc.

```

**Archivos clave por paquete**

- `controller/ProductController.java`: endpoints (`/products`), mapea DTOs ⇄ dominio, devuelve códigos HTTP adecuados.
- `dto/ProductRequest.java` y `dto/ProductResponse.java`: validaciones `@NotBlank`, `@PositiveOrZero` y proyección de salida.
- `entity/Product.java`: `@Entity(name = "products")`, columnas, constraints (además de las de BD).
- `repository/ProductRepository.java`: `interface` que extiende `JpaRepository` y habilita paginación/orden.
- `service/ProductService.java`: `create, findAll(Pageable), findById, update, delete` con manejo de `NotFoundException`/`ConflictException`.
- `exception/GlobalExceptionHandler.java`: `@ControllerAdvice` que unifica respuestas de error.

## 16. Testing

Este proyecto incorpora una batería de pruebas orientada a verificar **contrato de API**, **reglas de validación** y **comportamiento end-to-end**. A continuación se describe **qué se testea, cómo y por qué es importante**, junto con comandos y rutas de archivos para integrarlo a tu documentación.

## 16.1 Tipos de tests incluidos

### 1. Tests de Controller (slice web)

- **Anotaciones clave:** `@WebMvcTest(ProductController.class)`, `MockMvc`
    - **Qué validan:**
        - Mapeos HTTP y rutas: `GET /products`, `GET /products/{id}`, `POST`, `PUT`, `DELETE`.
        - **Códigos de estado** esperados (200/201/204/404/400).
        - **Estructura del JSON** de respuesta (campos presentes).
    - **Por qué importa:** detecta errores de contrato de API (paths, status, serialización) **sin** depender de la base de datos → **rápidos y precisos**.
    
    **Archivo de referencia:**
    
    `src/test/java/ar/edu/challenge01/productapi/web/ProductControllerTest.java`
    
    ---
    
    ### 2. Tests de Validación (Bean Validation)
    
    - **Anotaciones clave:** `@WebMvcTest`, `@Valid` en el `@RequestBody`, `ApiExceptionHandler`
    - **Qué validan:**
        - Que payloads inválidos (por ejemplo, `name` vacío o `price` nulo) disparen **400 Bad Request**.
        - Que el **cuerpo de error** siga un formato consistente (clave `error`, `message`, `details` con campos y mensajes).
    - **Por qué importa:** evita que **datos inválidos** entren a la capa de persistencia y estandariza la UX de la API ante errores.
    
    **Archivo de referencia:**
    
    `src/test/java/ar/edu/challenge01/productapi/web/ProductValidationTest.java`
    
    > Nota: El handler devuelve error: "Bad Request" (reason phrase estándar). Los tests ya contemplan ese valor para no generar falsos negativos.
    > 
    
    ---
    
    ### 3. Tests de Integración (end-to-end) – *opcional/si se activan*
    
    - **Anotaciones clave:** `@SpringBootTest`, `MockMvc` (o `TestRestTemplate`)
    - **Qué validan:**
        - Flujo **real** CRUD: crear → leer → actualizar → borrar **atravesando todas las capas** (web → JPA → DB → Flyway).
        - Alineación entre **entidades JPA** y **migraciones** (columnas, tipos, constraints).
    - **Por qué importa:** da **confianza de extremo a extremo**; si pasa aquí, es altamente probable que funcione igual en producción.
    
    **Archivo de referencia (si se incluye):**
    
    `src/test/java/ar/edu/challenge01/productapi/web/ProductE2ETest.java`
    

| Tipo | Criterio | Ejemplo de aserción |
| --- | --- | --- |
| Controller (Web) | La ruta responde con **status** correcto | `andExpect(status().isOk())`, `isCreated()`, `isNoContent()`, `isNotFound()`, `isBadRequest()` |
| Controller (Web) | JSON con **campos esperados** | `jsonPath("$.id").exists()`, `jsonPath("$.name").value("Teclado")` |
| Validación | **400** cuando el payload viola `@Valid` | `andExpect(status().isBadRequest())` |
| Validación | **Formato de error** consistente | `jsonPath("$.error").value("Bad Request")` y `jsonPath("$.details.name").exists()` |
| Integración | CRUD completo **end-to-end** | POST→GET→PUT→DELETE con verificación de side-effects en DB |