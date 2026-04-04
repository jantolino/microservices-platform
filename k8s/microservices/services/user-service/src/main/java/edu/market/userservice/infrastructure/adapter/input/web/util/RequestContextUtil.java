package edu.market.userservice.infrastructure.adapter.input.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilidad para extraer información del contexto de la petición HTTP actual
 */
@Slf4j
@Component
public class RequestContextUtil {

    /**
     * Obtiene la dirección IP del cliente que realiza la petición
     * @return Dirección IP del cliente
     */
    public String getClientIp() {
        log.debug("Obteniendo IP del cliente");
        return Optional.ofNullable(getCurrentRequest())
                .map(HttpServletRequest::getRemoteAddr)
                .orElse("unknown");
    }

    /**
     * Obtiene el User-Agent del cliente que realiza la petición
     * @return User-Agent del cliente
     */
    public String getUserAgent() {
        log.debug("Obteniendo User-Agent del cliente");
        return Optional.ofNullable(getCurrentRequest())
                .map(request -> request.getHeader("User-Agent"))
                .orElse("unknown");
    }

    /**
     * Obtiene la ruta completa de la petición actual (incluyendo query params)
     * @return Ruta completa de la petición
     */
    public String getRequestPath() {
        log.debug("Obteniendo ruta de la petición");
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "";
        }
        
        String queryString = request.getQueryString();
        if (queryString == null) {
            return request.getRequestURI();
        } else {
            return request.getRequestURI() + "?" + queryString;
        }
    }

    /**
     * Obtiene el host de la petición actual
     * @return Host de la petición
     */
    public String getRequestHost() {
        log.debug("Obteniendo host de la petición");
        return Optional.ofNullable(getCurrentRequest())
                .map(request -> {
                    String scheme = request.getScheme();
                    String serverName = request.getServerName();
                    int serverPort = request.getServerPort();
                    
                    StringBuilder url = new StringBuilder();
                    url.append(scheme).append("://").append(serverName);
                    
                    if ((scheme.equals("http") && serverPort != 80) || 
                        (scheme.equals("https") && serverPort != 443)) {
                        url.append(":").append(serverPort);
                    }
                    
                    return url.toString();
                })
                .orElse("");
    }

    /**
     * Obtiene la URL completa de la petición actual
     * @return URL completa
     */
    public String getFullUrl() {
        log.debug("Obteniendo URL completa de la petición");
        String host = getRequestHost();
        String path = getRequestPath();
        
        if (host.isEmpty() || path.isEmpty()) {
            return "";
        }
        
        return host + path;
    }

    /**
     * Obtiene el objeto HttpServletRequest de la petición actual
     * @return HttpServletRequest o null si no está disponible
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        return attributes != null ? attributes.getRequest() : null;
    }
}
