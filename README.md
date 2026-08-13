

# 🚀 Spring Microservices

### Arquitectura de Microservicios con Spring Boot 3 & Spring Cloud

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2022.0.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

---

Sistema distribuido de gestión académica que implementa el patrón de **microservicios** para administrar **Cursos** y **Estudiantes**, con configuración centralizada, service discovery y un API Gateway como punto de entrada único.


---

## 📑 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Stack Tecnológico](#-stack-tecnológico)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Descripción de Microservicios](#-descripción-de-microservicios)
- [Modelo de Datos](#-modelo-de-datos)
- [Comunicación entre Servicios](#-comunicación-entre-servicios)
- [Configuración Centralizada](#-configuración-centralizada)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Configuración de Base de Datos](#-instalación-y-configuración-de-base-de-datos)
- [Orden de Ejecución](#-orden-de-ejecución)
- [API Endpoints](#-api-endpoints)
- [Ejemplos de Uso con Postman / cURL](#-ejemplos-de-uso-con-postman--curl)
- [Puertos y URLs](#-puertos-y-urls)
- [Troubleshooting](#-troubleshooting)

---

## 📋 Descripción General

Este proyecto implementa un **sistema de gestión académica** basado en microservicios donde:

- Los **estudiantes** se matriculan en **cursos**.
- Cada estudiante pertenece a un curso (relación `courseId`).
- El microservicio de **cursos** puede consultar todos los estudiantes matriculados en un curso específico a través de comunicación inter-servicio con **OpenFeign**.
- Toda la configuración está **centralizada** mediante un Config Server.
- Los servicios se **descubren dinámicamente** a través de Eureka.
- Un **API Gateway** actúa como punto de entrada único para los clientes.

---

## 🏗 Arquitectura del Sistema

```
                          ┌─────────────────────────────┐
                          │        CLIENTE / POSTMAN     │
                          └──────────────┬──────────────┘
                                         │
                                         ▼
                          ┌─────────────────────────────┐
                          │      🌐 API GATEWAY          │
                          │    (microservice-gateway)    │
                          │        Puerto: 8080          │
                          └──────┬──────────────┬───────┘
                                 │              │
                    ┌────────────┘              └────────────┐
                    ▼                                        ▼
     ┌──────────────────────────┐          ┌──────────────────────────┐
     │   📚 COURSE SERVICE      │  Feign   │   🎓 STUDENT SERVICE     │
     │  (microservice-course)   │─────────▶│  (microservice-student)  │
     │     Puerto: 9090         │          │     Puerto: 8090         │
     │     BD: PostgreSQL       │          │     BD: MySQL            │
     └────────────┬─────────────┘          └────────────┬─────────────┘
                  │                                     │
                  └──────────────┬──────────────────────┘
                                 │  Se registran
                                 ▼
                  ┌──────────────────────────────┐
                  │    🔍 EUREKA SERVER           │
                  │   (microservice-eureka)       │
                  │      Puerto: 8761             │
                  └──────────────┬───────────────┘
                                 │  Obtiene config
                                 ▼
                  ┌──────────────────────────────┐
                  │    ⚙️ CONFIG SERVER           │
                  │   (microservice-config)       │
                  │      Puerto: 8888             │
                  │   (Fuente: classpath nativo)   │
                  └──────────────────────────────┘
```

### Flujo de comunicación

```
1.  Config Server arranca → expone configuraciones centralizadas en puerto 8888
2.  Eureka Server arranca → obtiene su config del Config Server → levanta registro en 8761
3.  Gateway arranca → obtiene su config del Config Server → se registra en Eureka → escucha en 8080
4.  Student Service arranca → obtiene su config → se registra en Eureka → escucha en 8090
5.  Course Service arranca → obtiene su config → se registra en Eureka → escucha en 9090
6.  Cliente hace request al Gateway (8080) → Gateway enruta al servicio correcto
7.  Course Service usa OpenFeign → llama a Student Service a través del Gateway (8080)
```

---

## 🛠 Stack Tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| **Lenguaje** | Java (OpenJDK) | 21 |
| **Framework** | Spring Boot | 3.1.4 |
| **Cloud** | Spring Cloud | 2022.0.5 |
| **Build Tool** | Apache Maven | 3.x |
| **Service Discovery** | Netflix Eureka | — |
| **API Gateway** | Spring Cloud Gateway | — |
| **Config Server** | Spring Cloud Config | — |
| **Inter-service Comm.** | OpenFeign | — |
| **BD Estudiantes** | MySQL | 8.x |
| **BD Cursos** | PostgreSQL | 15/16 |
| **ORM** | Hibernate / Spring Data JPA | — |
| **Utilidades** | Lombok | — |
| **Monitoring** | Spring Boot Actuator | — |

---

## 📂 Estructura del Proyecto

```
SpringMicroservices/                      ← POM padre (multi-módulo)
├── pom.xml                               ← POM raíz con dependencyManagement
│
├── microservice-config/                  ← ⚙️ Config Server (Puerto 8888)
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../MicroserviceConfigApplication.java
│       └── resources/
│           ├── application.yml           ← Config del propio server
│           └── configurations/           ← Configs centralizadas
│               ├── msvc-eureka.yml
│               ├── msvc-gateway.yml
│               ├── msvc-student.yml
│               └── msvc-course.yml
│
├── microservice-eureka/                  ← 🔍 Discovery Server (Puerto 8761)
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../MicroserviceEurekaApplication.java
│       └── resources/
│           └── application.yml           ← Apunta al Config Server
│
├── microservice-gateway/                 ← 🌐 API Gateway (Puerto 8080)
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../MicroserviceGatewayApplication.java
│       └── resources/
│           └── application.yml           ← Apunta al Config Server
│
├── microservice-student/                 ← 🎓 Student Service (Puerto 8090)
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../
│       │   ├── MicroserviceStudentApplication.java
│       │   ├── Entity/Student.java
│       │   ├── controller/StudentController.java
│       │   ├── persistence/StudentRepository.java
│       │   └── service/
│       │       ├── IStudentService.java
│       │       └── StudentServiceImpl.java
│       └── resources/
│           └── application.yml           ← Apunta al Config Server
│
└── microservice-course/                  ← 📚 Course Service (Puerto 9090)
    ├── pom.xml
    └── src/main/
        ├── java/.../
        │   ├── MicroserviceCourseApplication.java
        │   ├── entity/Course.java
        │   ├── controller/CourseController.java
        │   ├── persistence/ICourseRepository.java
        │   ├── client/StudentClient.java       ← Feign Client
        │   ├── dto/StudentDTO.java
        │   ├── http/response/StudentByCourseResponse.java
        │   └── service/
        │       ├── ICourseService.java
        │       └── CourseServiceImpl.java
        └── resources/
            └── application.yaml          ← Apunta al Config Server
```

---

## 📦 Descripción de Microservicios

### 1. ⚙️ Config Server (`microservice-config`)

| Propiedad | Valor |
|---|---|
| **Puerto** | `8888` |
| **Perfil** | `native` (lee de classpath local) |
| **Anotación** | `@EnableConfigServer` |
| **Responsabilidad** | Centralizar la configuración de todos los microservicios |

El Config Server utiliza el perfil `native`, lo que significa que las configuraciones se almacenan **localmente** dentro del directorio `src/main/resources/configurations/`. Cada archivo YAML en esa carpeta corresponde a un microservicio y es identificado por el `spring.application.name` de cada servicio.

**Archivos de configuración que gestiona:**

| Archivo | Servicio destino | Contenido |
|---|---|---|
| `msvc-eureka.yml` | Eureka Server | Puerto, hostname, configuración de registro |
| `msvc-gateway.yml` | API Gateway | Puerto, rutas, predicados |
| `msvc-student.yml` | Student Service | Puerto, datasource MySQL, JPA, Eureka client |
| `msvc-course.yml` | Course Service | Puerto, datasource PostgreSQL, JPA, Eureka client |

---

### 2. 🔍 Eureka Server (`microservice-eureka`)

| Propiedad | Valor |
|---|---|
| **Puerto** | `8761` |
| **Anotación** | `@EnableEurekaServer` |
| **Dashboard** | `http://localhost:8761` |
| **Responsabilidad** | Registro y descubrimiento de servicios |

Configurado para **no registrarse a sí mismo** (`register-with-eureka: false`, `fetch-registry: false`). Los demás microservicios se registran aquí automáticamente al iniciar.

---

### 3. 🌐 API Gateway (`microservice-gateway`)

| Propiedad | Valor |
|---|---|
| **Puerto** | `8080` |
| **Anotación** | `@EnableDiscoveryClient` |
| **Responsabilidad** | Punto de entrada único, enrutamiento de peticiones |

**Rutas configuradas:**

| Route ID | URI destino | Predicado (Path) |
|---|---|---|
| `students` | `http://localhost:8090` | `/api/student/**` |
| `courses` | `http://localhost:9090` | `/api/course/**` |

> **Nota:** El Gateway no se registra en Eureka (`register-with-eureka: false`) pero sí puede descubrir servicios gracias a `@EnableDiscoveryClient`.

---

### 4. 🎓 Student Service (`microservice-student`)

| Propiedad | Valor |
|---|---|
| **Puerto** | `8090` |
| **Base de datos** | MySQL (`studentdb`) |
| **Anotaciones** | `@EnableDiscoveryClient`, `@SpringBootApplication` |
| **Responsabilidad** | CRUD de estudiantes |

**Capas de la aplicación:**

```
Controller ──▶ Service (Interface + Impl) ──▶ Repository ──▶ MySQL
```

---

### 5. 📚 Course Service (`microservice-course`)

| Propiedad | Valor |
|---|---|
| **Puerto** | `9090` |
| **Base de datos** | PostgreSQL (`coursedb`) |
| **Anotaciones** | `@EnableFeignClients`, `@EnableDiscoveryClient` |
| **Responsabilidad** | CRUD de cursos + consulta de estudiantes por curso |

**Capas de la aplicación:**

```
Controller ──▶ Service (Interface + Impl) ──▶ Repository ──▶ PostgreSQL
                       │
                       └──▶ StudentClient (Feign) ──▶ Gateway ──▶ Student Service
```

---

## 🗄 Modelo de Datos

### Tabla `students` (MySQL — `studentdb`)

| Columna | Tipo | Restricción | Descripción |
|---|---|---|---|
| `id` | `BIGINT` | `PK, AUTO_INCREMENT` | Identificador único |
| `name` | `VARCHAR` | — | Nombre del estudiante |
| `last_name` | `VARCHAR` | — | Apellido del estudiante |
| `email` | `VARCHAR` | — | Correo electrónico |
| `course_id` | `BIGINT` | — | ID del curso (FK lógica) |

### Tabla `courses` (PostgreSQL — `coursedb`)

| Columna | Tipo | Restricción | Descripción |
|---|---|---|---|
| `id` | `BIGINT` | `PK, AUTO_INCREMENT` | Identificador único |
| `name` | `VARCHAR` | — | Nombre del curso |
| `teacher` | `VARCHAR` | — | Nombre del profesor |

### Diagrama Entidad-Relación

```
┌──────────────────────┐          ┌──────────────────────┐
│      COURSES         │          │      STUDENTS        │
│   (PostgreSQL)       │          │      (MySQL)         │
├──────────────────────┤          ├──────────────────────┤
│ PK  id       BIGINT  │◄────────│ FK  course_id BIGINT │
│     name     VARCHAR │          │ PK  id        BIGINT │
│     teacher  VARCHAR │          │     name      VARCHAR│
│                      │          │     last_name VARCHAR│
│                      │          │     email     VARCHAR│
└──────────────────────┘          └──────────────────────┘
       (coursedb)                        (studentdb)

 Relación lógica: Un Course tiene muchos Students (1:N)
 La FK es lógica (no hay constraint físico entre BDs distintas)
```

> ⚠️ La relación entre cursos y estudiantes es **lógica** (no física), ya que residen en **bases de datos diferentes** (PostgreSQL vs MySQL). La integridad se mantiene a nivel de aplicación.

---

## 🔗 Comunicación entre Servicios

### OpenFeign Client

El microservicio de **Course** consume el microservicio de **Student** a través de un cliente Feign declarativo:

```java
@FeignClient(name = "msvc-student", url = "http://localhost:8080/api/student")
public interface StudentClient {

    @GetMapping("/search-by-course/{idCourse}")
    List<StudentDTO> findAllStudentByCourse(@PathVariable Long idCourse);
}
```

**Flujo de la comunicación inter-servicio:**

```
1. Cliente pide: GET http://localhost:8080/api/course/search-student/1
2. Gateway enruta a → Course Service (9090)
3. Course Service:
   a. Busca el curso en PostgreSQL
   b. Llama vía Feign a: GET http://localhost:8080/api/student/search-by-course/1
4. Gateway recibe la llamada Feign y enruta a → Student Service (8090)
5. Student Service consulta MySQL y retorna la lista de estudiantes
6. Course Service combina los datos y retorna StudentByCourseResponse
```

**Objeto de respuesta combinada (`StudentByCourseResponse`):**

```json
{
  "courseName": "Matemáticas",
  "teacher": "Prof. García",
  "studentDTOSList": [
    {
      "name": "Juan",
      "lastName": "Pérez",
      "email": "juan@email.com",
      "courseId": 1
    }
  ]
}
```

---

## ⚙️ Configuración Centralizada

Todos los microservicios apuntan al Config Server mediante la propiedad `spring.config.import` en su `application.yml` local:

```yaml
spring:
  application:
    name: msvc-<servicio>     # Identifica qué archivo de config tomar
  config:
    import: optional:configserver:http://localhost:8888
```

El prefijo `optional:` evita que el servicio falle si el Config Server no está disponible (arrancará con valores por defecto si los tiene).

### Mapa de configuraciones

```
Config Server (8888)
└── classpath:/configurations/
    ├── msvc-eureka.yml    → spring.application.name: msvc-eureka    → Puerto 8761
    ├── msvc-gateway.yml   → spring.application.name: msvc-gateway   → Puerto 8080
    ├── msvc-student.yml   → spring.application.name: msvc-student   → Puerto 8090
    └── msvc-course.yml    → spring.application.name: msvc-course    → Puerto 9090
```

---

## ✅ Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

| Software | Versión mínima | Verificar instalación |
|---|---|---|
| **Java JDK** | 21 | `java -version` |
| **Maven** | 3.8+ | `mvn -version` |
| **MySQL** | 8.0+ | `mysql --version` |
| **PostgreSQL** | 15+ | `psql --version` |
| **Git** | 2.x | `git --version` |
| **IDE** (recomendado) | IntelliJ IDEA | — |

---

## 🗃 Instalación y Configuración de Base de Datos

### 1. Clonar el repositorio

```bash
git clone git@github.com:YanFranko-GS/miservices-spring3.git
cd miservices-spring3
```

### 2. Crear la base de datos de Estudiantes (MySQL)

```sql
-- Conectarse a MySQL
mysql -u root -p

-- Crear la base de datos
CREATE DATABASE studentdb;

-- Verificar
SHOW DATABASES;
```

> **Credenciales configuradas:** `username: root` / `password: root`
> Si tus credenciales son diferentes, modifica el archivo `microservice-config/src/main/resources/configurations/msvc-student.yml`

### 3. Crear la base de datos de Cursos (PostgreSQL)

```sql
-- Conectarse a PostgreSQL
psql -U postgres

-- Crear la base de datos
CREATE DATABASE coursedb;

-- Verificar
\l
```

> **Credenciales configuradas:** `username: postgres` / `password: postgres`
> Si tus credenciales son diferentes, modifica el archivo `microservice-config/src/main/resources/configurations/msvc-course.yml`

### 4. Compilar el proyecto completo

```bash
# Desde la raíz del proyecto
mvn clean install -DskipTests
```

---

## 🚦 Orden de Ejecución

> ⚠️ **MUY IMPORTANTE:** Los microservicios deben iniciarse en un **orden específico** debido a las dependencias entre ellos. No respetar este orden causará errores de conexión.

```
Paso 1 ──▶ Config Server   (8888)   ← Primero SIEMPRE, todos dependen de él
   ⏳ Esperar a que arranque completamente

Paso 2 ──▶ Eureka Server   (8761)   ← Segundo, necesita la config del paso 1
   ⏳ Esperar a que arranque completamente

Paso 3 ──▶ API Gateway     (8080)   ← Tercero, necesita Eureka para descubrir servicios
   ⏳ Esperar a que arranque completamente

Paso 4 ──▶ Student Service (8090)   ← Cuarto, necesita MySQL + Config + Eureka
         + Course Service  (9090)   ← Cuarto, necesita PostgreSQL + Config + Eureka
                                      (estos dos se pueden arrancar en paralelo)
```

### Ejecución desde terminal

```bash
# Terminal 1 — Config Server (PRIMERO)
cd microservice-config
mvn spring-boot:run

# Terminal 2 — Eureka Server (esperar ~15 seg después del paso 1)
cd microservice-eureka
mvn spring-boot:run

# Terminal 3 — API Gateway (esperar ~10 seg después del paso 2)
cd microservice-gateway
mvn spring-boot:run

# Terminal 4 — Student Service (esperar ~10 seg después del paso 3)
cd microservice-student
mvn spring-boot:run

# Terminal 5 — Course Service (esperar ~10 seg después del paso 3)
cd microservice-course
mvn spring-boot:run
```

### Ejecución desde IntelliJ IDEA

1. Abrir el proyecto raíz (`SpringMicroservices/`) como proyecto Maven.
2. Ir a **Run → Edit Configurations**.
3. Crear 5 configuraciones de tipo **Spring Boot**, una por cada `*Application.java`.
4. Ejecutar en el orden indicado arriba, esperando que cada servicio arranque antes de iniciar el siguiente.

### Verificar que todo funciona

- **Config Server:** `http://localhost:8888/msvc-student/default` → debe retornar JSON con la configuración.
- **Eureka Dashboard:** `http://localhost:8761` → debe mostrar los servicios registrados.
- **Gateway + Student:** `http://localhost:8080/api/student/all` → debe retornar la lista de estudiantes.
- **Gateway + Course:** `http://localhost:8080/api/course/all` → debe retornar la lista de cursos.

---

## 📡 API Endpoints

### 🎓 Student Service — Base Path: `/api/student`

> Acceso a través del Gateway: `http://localhost:8080/api/student`
> Acceso directo: `http://localhost:8090/api/student`

| Método | Endpoint | Descripción | Request Body |
|---|---|---|---|
| `POST` | `/api/student/create` | Crear un nuevo estudiante | `Student` (JSON) |
| `GET` | `/api/student/all` | Listar todos los estudiantes | — |
| `GET` | `/api/student/search/{id}` | Buscar estudiante por ID | — |
| `GET` | `/api/student/search-by-course/{idCourse}` | Buscar estudiantes por curso | — |

### 📚 Course Service — Base Path: `/api/course`

> Acceso a través del Gateway: `http://localhost:8080/api/course`
> Acceso directo: `http://localhost:9090/api/course`

| Método | Endpoint | Descripción | Request Body |
|---|---|---|---|
| `POST` | `/api/course/create` | Crear un nuevo curso | `Course` (JSON) |
| `GET` | `/api/course/all` | Listar todos los cursos | — |
| `GET` | `/api/course/search/{id}` | Buscar curso por ID | — |
| `GET` | `/api/course/search-student/{idCourse}` | Buscar estudiantes de un curso (Feign) | — |

---

## 🧪 Ejemplos de Uso con Postman / cURL

### 1. Crear un Curso

```bash
curl -X POST http://localhost:8080/api/course/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Matemáticas Avanzadas",
    "teacher": "Prof. García"
  }'
```

**Response:** `201 Created` (sin body)

### 2. Crear un Estudiante

```bash
curl -X POST http://localhost:8080/api/student/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan",
    "lastName": "Pérez",
    "email": "juan.perez@email.com",
    "courseId": 1
  }'
```

**Response:** `201 Created` (sin body)

### 3. Listar todos los Estudiantes

```bash
curl http://localhost:8080/api/student/all
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Juan",
    "lastName": "Pérez",
    "email": "juan.perez@email.com",
    "courseId": 1
  }
]
```

### 4. Listar todos los Cursos

```bash
curl http://localhost:8080/api/course/all
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Matemáticas Avanzadas",
    "teacher": "Prof. García"
  }
]
```

### 5. Consultar Estudiantes de un Curso (comunicación inter-servicio)

```bash
curl http://localhost:8080/api/course/search-student/1
```

**Response:**
```json
{
  "courseName": "Matemáticas Avanzadas",
  "teacher": "Prof. García",
  "studentDTOSList": [
    {
      "name": "Juan",
      "lastName": "Pérez",
      "email": "juan.perez@email.com",
      "courseId": 1
    }
  ]
}
```

---

## 🔌 Puertos y URLs

| Servicio | Puerto | URL Base | Dashboard/Admin |
|---|---|---|---|
| **Config Server** | `8888` | `http://localhost:8888` | `http://localhost:8888/msvc-*/default` |
| **Eureka Server** | `8761` | `http://localhost:8761` | `http://localhost:8761` (Dashboard web) |
| **API Gateway** | `8080` | `http://localhost:8080` | — |
| **Student Service** | `8090` | `http://localhost:8090` | `http://localhost:8090/actuator` |
| **Course Service** | `9090` | `http://localhost:9090` | `http://localhost:9090/actuator` |

---

## 🔧 Troubleshooting

### ❌ Error: `Connection refused` al iniciar un servicio

**Causa:** El Config Server no está levantado o no terminó de arrancar.
**Solución:** Asegúrate de que el Config Server esté corriendo en el puerto `8888` antes de iniciar cualquier otro servicio. Gracias al prefijo `optional:` en la configuración, el servicio puede arrancar sin el Config Server, pero usará valores por defecto que podrían no ser correctos.

### ❌ Error: `Communications link failure` (MySQL)

**Causa:** MySQL no está corriendo o la base de datos `studentdb` no existe.
**Solución:**
```bash
# Verificar que MySQL está corriendo
sudo systemctl status mysql

# Crear la base de datos
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS studentdb;"
```

### ❌ Error: `Connection to localhost:5432 refused` (PostgreSQL)

**Causa:** PostgreSQL no está corriendo o la base de datos `coursedb` no existe.
**Solución:**
```bash
# Verificar que PostgreSQL está corriendo
sudo systemctl status postgresql

# Crear la base de datos
sudo -u postgres psql -c "CREATE DATABASE coursedb;"
```

### ❌ Error: `No instances available for msvc-student` (Feign)

**Causa:** El Student Service no está registrado en Eureka.
**Solución:** Verificar que el Student Service esté levantado y registrado en el dashboard de Eureka (`http://localhost:8761`).

### ❌ Error: `404 Not Found` en peticiones al Gateway

**Causa:** Las rutas del Gateway no coinciden con las rutas del servicio destino.
**Solución:** Verificar que los predicados en `msvc-gateway.yml` coincidan con los `@RequestMapping` de los controladores.

### ❌ Las tablas no se crean automáticamente

**Causa:** Hibernate está configurado con `ddl-auto: update`, pero la conexión a la BD falló silenciosamente.
**Solución:** Verificar credenciales y conectividad a las bases de datos en los archivos de configuración dentro de `microservice-config/src/main/resources/configurations/`.

---

## 📄 Licencia

Este proyecto es de uso educativo y personal.

---



**Desarrollado por [YanFranko-GS](https://github.com/YanFranko-GS)** 🚀