package edu.market.notification.domain.enums;

/**
 * Enumeración que define los formatos disponibles para la generación de informes.
 * Siguiendo el principio de diseño por contrato, esta enumeración garantiza
 * que solo se utilicen formatos válidos y soportados por el sistema.
 */
public enum ReportFormatType {
    PDF,
    CSV,
    JSON,
    EXCEL,
    HTML;
    
    /**
     * Convierte un string a un ReportFormatType de forma segura.
     * 
     * @param format String que representa el formato
     * @return El enum correspondiente o null si no es válido
     */
    public static ReportFormatType fromString(String format) {
        if (format == null) {
            return null;
        }
        
        try {
            return ReportFormatType.valueOf(format.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
