# Arquitectura de Microservicios - Ejemplo tipo Amazon

Estructura basada en mejores prácticas de Spring Boot, Clean/Hexagonal Architecture y despliegue moderno.

## Estructura principal

```
services/
├── catalog-service/
├── user-service/
├── cart-service/
├── order-service/
├── billing-service/
├── tracking-service/
├── notification-service/
├── recommendation-service/
├── support-service/
├── admin-service/
└── integration-service/
servers/
├── config-server/
├── eureka-server/
└── auth-server/
gateway/
README.md
```

### Función de cada carpeta

- **services/**: Directorio principal que contiene todos los microservicios de negocio
  - **catalog-service/**: Microservicio de catálogo de productos y servicios.
  - **user-service/**: Microservicio de gestión de usuarios (clientes, perfiles, preferencias).
  - **cart-service/**: Microservicio para manejo de carritos de compra.
  - **order-service/**: Microservicio para procesamiento y gestión de pedidos.
  - **billing-service/**: Microservicio de facturación y pagos.
  - **tracking-service/**: Microservicio para rastreo de envíos y estado de pedidos.
  - **notification-service/**: Microservicio para envío de notificaciones (email, SMS, push).
  - **recommendation-service/**: Microservicio de recomendaciones personalizadas de productos.
  - **support-service/**: Microservicio de soporte y atención al cliente (tickets, chat).
  - **admin-service/**: Microservicio para administración interna (gestión de usuarios, productos, reportes).
  - **integration-service/**: Microservicio para integraciones externas (pasarelas de pago, logística, etc).
- **servers/**: Directorio que contiene los servidores de infraestructura
  - **config-server/**: Servidor centralizado de configuración (Spring Cloud Config).
  - **eureka-server/**: Discovery server (Eureka) para registro y descubrimiento de microservicios.
  - **auth-server/**: Authorization Server (OAuth2/Spring Authorization Server) para autenticación y emisión de tokens.
- **gateway/**: API Gateway (Spring Cloud Gateway) para enrutar y proteger las APIs.
- **k8s/**: Archivos de despliegue y configuración para Kubernetes.
- **scripts/**: Scripts útiles para automatización, despliegue, migraciones, etc.

- **README.md**: Documentación principal del proyecto.
- **docker-compose.yml**: Orquestador de contenedores para desarrollo local.

Cada carpeta representa un microservicio o componente de infraestructura independiente. Puedes agregar los archivos fuente y configuración en cada uno según el patrón hexagonal/clean architecture.

---

### Ejemplo de estructura recomendada para microservicio (arquitectura hexagonal)

```text
├───logs
├───src
│   ├───main
│   │   ├───java
│   │   │   └───edu
│   │   │       └───market
│   │   │           └───userservice
│   │   │               ├───application
│   │   │               │   ├───port
│   │   │               │   │   └───input
│   │   │               │   └───usecase
│   │   │               │       ├───example1
│   │   │               │       └───example2
│   │   │               ├───domain
│   │   │               │   ├───enums
│   │   │               │   ├───event
│   │   │               │   ├───exception
│   │   │               │   ├───mapper
│   │   │               │   ├───model
│   │   │               │   ├───port
│   │   │               │   │   └───output
│   │   │               │   ├───service
│   │   │               │   └───vo
│   │   │               └───infrastructure
│   │   │                   ├───adapter
│   │   │                   │   ├───input
│   │   │                   │   │   └───web
│   │   │                   │   │       ├───api
│   │   │                   │   │       ├───controller
│   │   │                   │   │       ├───dto
│   │   │                   │   │       │   ├───request
│   │   │                   │   │       │   └───response
│   │   │                   │   │       ├───mapper
│   │   │                   │   │       └───util
│   │   │                   │   └───output
│   │   │                   │       ├───event
│   │   │                   │       └───persistence
│   │   │                   │           ├───entity
│   │   │                   │           ├───exception
│   │   │                   │           ├───mapper
│   │   │                   │           └───repository
│   │   │                   ├───config
│   │   │                   └───crosscutting
│   │   │                       ├───exception
│   │   │                       ├───logging
│   │   │                       ├───persistence
│   │   │                       └───security
│   │   │                           └───filter
│   │   └───resources
│   │       ├───static
│   │       └───templates
│   └───test
│       ├───java
│       └───resources
```

- **Nota:** Adapta los nombres de carpetas y clases según el microservicio correspondiente.
- Esta estructura facilita el desacoplamiento, la escalabilidad y el mantenimiento del código.