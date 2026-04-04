package edu.market.userservice.infrastructure.crosscutting.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PersistenceLoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PersistenceLoggingAspect.class);

    @Pointcut("within(edu.market.userservice.adapter.out.persistence.repository..*)")
    public void repositoryLayer() {}

    @Before("repositoryLayer()")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("[PERSISTENCIA] Llamada a: {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "repositoryLayer()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        logger.info("[PERSISTENCIA] Respuesta de: {}.{} -> {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), result);
    }
}
