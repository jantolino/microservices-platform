# Config Server

## ¿De qué se trata el servicio?
Es un microservicio encargado de centralizar y administrar la configuración externa de otros servicios de la arquitectura. Permite que los microservicios obtengan sus propiedades de configuración (por ambiente) de forma centralizada y dinámica.

## ¿Cómo está configurado?
- Utiliza Spring Cloud Config Server.
- Almacena las propiedades en una base de datos H2 en memoria.
- Expone el puerto 8890.
- Se registra automáticamente en Eureka para descubrimiento de servicios.
- Puede ejecutarse como contenedor Docker.

## ¿Cuándo se utiliza?
Cuando otros microservicios requieren obtener sus configuraciones externas (por ejemplo, variables de entorno, credenciales, endpoints, etc.) al arrancar o durante su ejecución.

## ¿Dónde se utiliza?
En arquitecturas de microservicios basadas en Spring Boot, donde se busca centralizar la gestión de configuración para todos los servicios.

## ¿Para qué se utiliza?
Para evitar la duplicidad y dispersión de archivos de configuración, facilitando la administración, actualización y despliegue de cambios de configuración en todos los entornos (dev, qa, prod, etc.).
