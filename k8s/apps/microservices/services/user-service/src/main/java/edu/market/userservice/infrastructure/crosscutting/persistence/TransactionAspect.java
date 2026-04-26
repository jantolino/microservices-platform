package edu.market.userservice.infrastructure.crosscutting.persistence;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Aspecto para manejar transacciones en los casos de uso
 * Reemplaza el uso de @Transactional en la capa de aplicación
 */
@Aspect
@Component
@Order(1)
@Slf4j // Alta prioridad para que se ejecute antes que otros aspectos
public class TransactionAspect {
    
    private final PlatformTransactionManager transactionManager;   
    
    public TransactionAspect(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    /**
     * Intercepta todas las ejecuciones de métodos en los casos de uso
     * y los ejecuta dentro de una transacción
     */
    @Around("execution(* edu.market.userservice.application.usecase..*.*(..))")
    public Object manageTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.debug("Iniciando transacción para {}.{} con argumentos: {}", 
                className, methodName, Arrays.toString(joinPoint.getArgs()));
        
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        
        try {
            Object result = transactionTemplate.execute(status -> {
                try {
                    Object proceedResult = joinPoint.proceed();
                    log.debug("Transacción exitosa para {}.{}", className, methodName);
                    return proceedResult;
                } catch (Throwable e) {
                    log.error("Error en transacción para {}.{}: {}", className, methodName, e.getMessage());
                    status.setRollbackOnly();
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException("Error en la ejecución del caso de uso", e);
                }
            });
            
            log.debug("Transacción completada para {}.{}", className, methodName);
            return result;
        } catch (Exception e) {
            log.error("Transacción fallida para {}.{}: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}
