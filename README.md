# 🐾 Sanos y Salvos — Sistema de Gestión y Rescate Integral de Mascotas

> Proyecto Final · Asignatura Full Stack 3 · Ingeniería en Informática · DUOC UC

Plataforma basada en **microservicios** para la gestión, cuidado y rescate de mascotas en la **Municipalidad de Maipú**. Permite a vecinos reportar mascotas perdidas, a veterinarios consultar historial clínico por chip RFID, y a la municipalidad coordinar patrullas de rescate en tiempo real.

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    BROWSER (React + Vite · puerto 3000)                 │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │ HTTP + JWT Bearer Token
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              API GATEWAY  (Spring Cloud Gateway · puerto 8080)          │
│   • Valida JWT en cada request protegido                                │
│   • Extrae claim "rol" del token y aplica control de acceso por rol     │
│   • Propaga X-User-Role y X-User-Id como headers internos               │
│   • PUT /api/mascotas/{id}/despachar → solo rol MUNICIPALIDAD           │
└────────────┬──────────────────────────────────┬────────────────────────┘
             │ Routing via Eureka (lb://)        │
             ▼                                   ▼
┌────────────────────────┐       ┌───────────────────────────────────────┐
│  EUREKA DISCOVERY      │       │  ms-usuarios  (puerto 8081)           │
│  SERVER (puerto 8761)  │       │  • Spring Security + BCrypt           │
│  Registro y balanceo   │       │  • JWT: genera token con {sub,rol,id} │
│  de carga de servicios │       │  • PostgreSQL: tabla usuarios         │
└────────────────────────┘       │  • DataSeeder: 3 usuarios de prueba   │
                                 └───────────────────────────────────────┘
                                 ┌───────────────────────────────────────┐
                                 │  ms-mascotas  (puerto 8082)           │
                                 │  • MongoDB: colección mascotas        │
                                 │  • FeignClient → ms-usuarios          │
                                 │  • Circuit Breaker: Resilience4j      │
                                 │  • RabbitMQ publisher:                │
                                 │    · mascotas.nueva  (POST)           │
                                 │    · mascotas.busqueda (PUT perdida)  │
                                 └──────────────┬────────────────────────┘
                                                │ AMQP · TopicExchange
                                                │ mascotas.exchange
                                                │ binding: mascotas.#
                                                ▼
                                 ┌───────────────────────────────────────┐
                                 │  ms-notificaciones  (puerto 8083)     │
                                 │  • RabbitMQ consumer                  │
                                 │  • Simula Email + SMS + Push          │
                                 └───────────────────────────────────────┘
```

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Frontend | React + Vite | 19 / 6 |
| Servidor frontend | Nginx Alpine | latest |
| Backend | Spring Boot | 3.5.x |
| Orquestación cloud | Spring Cloud (Eureka, Gateway, Feign) | 2025.0.2 |
| Seguridad | Spring Security + JWT (JJWT) | HS256 |
| Resiliencia | Resilience4j Circuit Breaker | — |
| Mensajería | RabbitMQ 3 Management | — |
| DB relacional | PostgreSQL 15 | — |
| DB documental | MongoDB | latest |
| Contenedores | Docker + Docker Compose | — |

---

## 🚀 Cómo Levantar el Proyecto

### Prerequisito único
Tener instalado **Docker Desktop** (incluye Docker Compose).

### Un solo comando
```bash
docker-compose up --build
```

El primer build tarda ~5 minutos mientras descarga imágenes y compila los JARs. Los siguientes levantamientos son instantáneos (caché de Docker).

### Accesos una vez levantado

| Servicio | URL |
|---|---|
| **Aplicación web** | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| Swagger ms-usuarios | http://localhost:8081/swagger-ui.html |
| Swagger ms-mascotas | http://localhost:8082/swagger-ui.html |

---

## 👥 Usuarios de Prueba (DataSeeder)

Al levantar el stack por primera vez, el sistema inyecta automáticamente tres usuarios en PostgreSQL:

| Rol | Email | Contraseña | RUT |
|---|---|---|---|
| `VECINO` | vecino@vecino.cl | password123 | 11.111.111-1 |
| `VETERINARIO` | claudio@veterinaria.cl | password123 | 12.345.678-5 |
| `MUNICIPALIDAD` | admin@maipu.cl | password123 | 9.876.543-3 |

---

## 📡 API Endpoints Principales

### ms-usuarios (vía Gateway → puerto 8080)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `POST` | `/api/usuarios/login` | ❌ Pública | Autenticación, retorna JWT |
| `POST` | `/api/usuarios/registro` | ❌ Pública | Registro de nuevo usuario |
| `GET` | `/api/usuarios` | ✅ JWT | Listar todos los usuarios |
| `GET` | `/api/usuarios/{id}` | ✅ JWT | Buscar usuario por ID |

### ms-mascotas (vía Gateway → puerto 8080)

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `POST` | `/api/mascotas` | ✅ Cualquier rol | Registrar mascota → evento RabbitMQ |
| `GET` | `/api/mascotas` | ✅ Cualquier rol | Listar todas |
| `GET` | `/api/mascotas/{id}` | ✅ Cualquier rol | Buscar por ID |
| `GET` | `/api/mascotas/chip/{chip}` | ✅ Cualquier rol | Buscar por chip RFID |
| `GET` | `/api/mascotas/estado/{estado}` | ✅ Cualquier rol | Filtrar por estado |
| `PUT` | `/api/mascotas/{id}` | ✅ Cualquier rol | Actualizar (reportar pérdida → evento RabbitMQ si estado = "Buscado 🚨") |
| `PUT` | `/api/mascotas/{id}/despachar` | 🔒 Solo `MUNICIPALIDAD` | Asignar patrulla de rescate |

---

## 🔐 Flujo de Seguridad JWT

```
1. POST /api/usuarios/login
   └─► ms-usuarios valida email + BCrypt
   └─► Genera JWT con payload: { sub: email, rol: "VECINO", id: 1 }
   └─► Response: { token, role, id }

2. Frontend guarda en localStorage (3 keys separadas)

3. Cada request protegido → Gateway → AuthenticationFilter
   ├─ Verifica firma HMAC-SHA256 con secret compartido
   ├─ Extrae claim "rol"
   ├─ Si path termina en /despachar y rol ≠ MUNICIPALIDAD → 403 Forbidden
   └─ Propaga X-User-Role y X-User-Id como headers internos
```

---

## 📨 Flujo Asíncrono RabbitMQ

```
Evento: Mascota registrada
  ms-mascotas → Exchange "mascotas.exchange" → key "mascotas.nueva"
  └─► ms-notificaciones (binding mascotas.#)
  └─► [SIMULADO] Email + SMS + Push

Evento: Mascota reportada perdida (estado = "Buscado 🚨")
  ms-mascotas → Exchange "mascotas.exchange" → key "mascotas.busqueda"
  └─► ms-notificaciones (binding mascotas.#)
  └─► [SIMULADO] Alerta a dueño + Municipalidad + vecinos del sector
```

---

## 📁 Estructura del Proyecto

```
sanos-y-salvos/
├── docker-compose.yml          ← Orquestación completa del stack
│
├── frontend/                   ← React 19 + Vite
│   ├── Dockerfile              ← Multi-stage: Node build + Nginx serve
│   ├── nginx.conf              ← SPA routing + cache de assets
│   ├── .dockerignore
│   └── src/components/
│       ├── Login.jsx
│       ├── Registro.jsx
│       ├── Dashboard.jsx
│       ├── VecinoView.jsx      ← Mapa Leaflet + reporte pérdida
│       ├── VeterinarioView.jsx ← Búsqueda RFID + historial clínico
│       └── MunicipioView.jsx   ← Consola de despacho + KPIs
│
├── api-gateway/                ← Spring Cloud Gateway (puerto 8080)
│   └── filter/AuthenticationFilter.java  ← JWT + control de roles
│
├── discovery-server/           ← Eureka Server (puerto 8761)
│
├── ms-usuarios/                ← Spring Boot + PostgreSQL (puerto 8081)
│   ├── models/Usuario.java
│   ├── security/JwtUtil.java   ← Genera JWT con claims {rol, id}
│   ├── services/UsuarioService.java
│   └── controllers/
│       ├── UsuarioController.java
│       └── DataSeeder.java     ← 3 usuarios de prueba al arranque
│
├── ms-mascotas/                ← Spring Boot + MongoDB (puerto 8082)
│   ├── models/Mascota.java
│   ├── services/MascotaService.java  ← Publisher RabbitMQ
│   ├── controllers/MascotaController.java
│   ├── clients/UsuarioClient.java    ← Feign + Circuit Breaker
│   └── config/RabbitConfig.java
│
└── ms-notificaciones/          ← Spring Boot + RabbitMQ (puerto 8083)
    ├── config/RabbitConfig.java      ← Binding wildcard mascotas.#
    └── listeners/NotificacionListener.java
```

---

## 👨‍💻 Autor

Desarrollado para la asignatura **Full Stack 3** · Ingeniería en Informática · DUOC UC  
Municipio de referencia: **Maipú, Región Metropolitana, Chile**
