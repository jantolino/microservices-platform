package edu.market.userservice.infrastructure.crosscutting.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Aspecto para manejar el logging en las capas de dominio y aplicación
 */
@Aspect
@Component
@Order(2)
@Slf4j
public class LoggingAspect {
    
    /**
     * Pointcut para los métodos de la capa de aplicación
     */
    @Pointcut("execution(* edu.market.userservice.application..*.*(..))")
    public void applicationLayerExecution() {}
    
    /**
     * Pointcut para los métodos de la capa de dominio
     */
    @Pointcut("execution(* edu.market.userservice.domain.service..*.*(..))")
    public void domainLayerExecution() {}
    
    /**
     * Log antes de la ejecución del método
     */
    @Before("applicationLayerExecution() || domainLayerExecution()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.debug("Iniciando ejecución de {}.{} con argumentos: {}", 
                className, methodName, Arrays.toString(joinPoint.getArgs()));
    }
    
    /**
     * Log después de la ejecución exitosa del método
     */
    @AfterReturning(pointcut = "applicationLayerExecution() || domainLayerExecution()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.debug("Finalizada ejecución de {}.{} con resultado: {}", 
                className, methodName, result);
    }
    
    /**
     * Log después de una excepción
     */
    @AfterThrowing(pointcut = "applicationLayerExecution() || domainLayerExecution()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.error("Excepción en {}.{}: {}", 
                className, methodName, exception.getMessage(), exception);
    }
    
    /**
     * Log de tiempo de ejecución para métodos que comienzan con 'execute'
     */
    @Around("applicationLayerExecution() && execution(* execute*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        long startTime = System.currentTimeMillis();
        log.info("Iniciando {}.{}", className, methodName);
        
        try {
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Completado {}.{} en {} ms", className, methodName, executionTime);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Error en {}.{} después de {} ms: {}", 
                    className, methodName, executionTime, e.getMessage(), e);
            throw e;
        }
    }
}
