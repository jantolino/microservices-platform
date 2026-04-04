# Eureka Server

## ¿De qué se trata el servicio?
Es un microservicio que actúa como un servidor de descubrimiento para otros servicios, permitiendo que los microservicios se registren y descubran entre sí dinámicamente.

## ¿Cómo está configurado?
- Utiliza Spring Cloud Netflix Eureka Server.
- Expone el puerto 8761.
- Permite que los servicios clientes (como config-server) se registren automáticamente.
- Puede ejecutarse como contenedor Docker.

## ¿Cuándo se utiliza?
Cuando necesitas que los microservicios encuentren y se comuniquen entre sí sin conocer sus direcciones IP o puertos de antemano.

## ¿Dónde se utiliza?
En arquitecturas de microservicios Spring Boot, especialmente cuando se requiere escalabilidad y balanceo de carga dinámico.

## ¿Para qué se utiliza?
Para habilitar el descubrimiento automático de servicios, facilitando la comunicación, resiliencia y escalabilidad de la arquitectura de microservicios.
