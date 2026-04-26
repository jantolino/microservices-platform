package edu.market.notification.infrastructure.adapter.output.service;

import edu.market.notification.domain.enums.ReportFormatType;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.port.output.service.ReportGeneratorServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador de infraestructura para la generación de informes.
 * Implementa el puerto de salida ReportGeneratorServicePort.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportGeneratorServiceAdapter implements ReportGeneratorServicePort {
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    
    
    /**
     * Genera un informe en el formato especificado basado en una lista de notificaciones.
     * 
     * @param notifications Lista de notificaciones a incluir en el informe
     * @param format Formato del informe definido como enum para garantizar tipos válidos
     * @param startDate Fecha de inicio del período del informe
     * @param endDate Fecha de fin del período del informe
     * @return El contenido del informe como un array de bytes
     */
    @Override
    public byte[] generateReport(List<Notification> notifications, ReportFormatType format, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("ReportGeneratorServiceAdapter.generateReport - Generando informe en formato {} para el período {} a {}", 
                format, startDate.format(dateFormatter), endDate.format(dateFormatter));
        
        try {
            switch (format) {
                case CSV:
                    return generateCsvReport(notifications, startDate, endDate);
                case PDF:
                    return generatePdfReport(notifications, startDate, endDate);
                case JSON:
                    return generateJsonReport(notifications, startDate, endDate);
                default:
                    log.warn("ReportGeneratorServiceAdapter.generateReport - Formato de informe no soportado: {}", format);
                    return "Formato no soportado".getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("ReportGeneratorServiceAdapter.generateReport - Error al generar informe: {}", e.getMessage());
            return ("Error al generar informe: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Genera un informe en formato CSV.
     */
    private byte[] generateCsvReport(List<Notification> notifications, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("ReportGeneratorServiceAdapter.generateCsvReport - Generando informe CSV");
        
        StringBuilder csv = new StringBuilder();
        
        // Encabezados
        csv.append("ID,Asunto,Estado,Destinatario,Fecha de Creación,Canales,Prioridad\n");
        
        // Datos
        for (Notification notification : notifications) {
            csv.append(notification.getId()).append(",")
               .append(escapeForCsv(notification.getSubject())).append(",")
               .append(notification.getStatus()).append(",")
               .append(escapeForCsv(notification.getRecipient().email())).append(",")
               .append(notification.getCreatedAt().format(dateFormatter)).append(",")
               .append(notification.getChannels().stream()
                       .map(Enum::name)
                       .collect(Collectors.joining("|"))).append(",")
               .append(notification.getPriority()).append("\n");
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Genera un informe en formato PDF.
     * En una implementación real, se utilizaría una biblioteca como iText o Apache PDFBox.
     */
    private byte[] generatePdfReport(List<Notification> notifications, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("ReportGeneratorServiceAdapter.generatePdfReport - Generando informe PDF");
        
        // En una implementación real, aquí se generaría un PDF utilizando una biblioteca adecuada
        String pdfPlaceholder = "Este es un informe PDF simulado para el período " + 
                startDate.format(dateFormatter) + " a " + endDate.format(dateFormatter) + 
                " con " + notifications.size() + " notificaciones.";
        
        return pdfPlaceholder.getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Genera un informe en formato JSON.
     * En una implementación real, se utilizaría Jackson u otra biblioteca de serialización.
     */
    private byte[] generateJsonReport(List<Notification> notifications, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("ReportGeneratorServiceAdapter.generateJsonReport - Generando informe JSON");
        
        // En una implementación real, aquí se serializaría a JSON utilizando Jackson u otra biblioteca
        StringBuilder json = new StringBuilder();
        json.append("{\n")
            .append("  \"reportPeriod\": {\n")
            .append("    \"startDate\": \"").append(startDate.format(dateFormatter)).append("\",\n")
            .append("    \"endDate\": \"").append(endDate.format(dateFormatter)).append("\"\n")
            .append("  },\n")
            .append("  \"notificationsCount\": ").append(notifications.size()).append(",\n")
            .append("  \"notifications\": [\n");
        
        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            json.append("    {\n")
                .append("      \"id\": \"").append(notification.getId()).append("\",\n")
                .append("      \"subject\": \"").append(escapeForJson(notification.getSubject())).append("\",\n")
                .append("      \"status\": \"").append(notification.getStatus()).append("\",\n")
                .append("      \"recipient\": \"").append(escapeForJson(notification.getRecipient().email())).append("\",\n")
                .append("      \"createdAt\": \"").append(notification.getCreatedAt().format(dateFormatter)).append("\"\n")
                .append("    }").append(i < notifications.size() - 1 ? ",\n" : "\n");
        }
        
        json.append("  ]\n")
            .append("}");
        
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Escapa caracteres especiales para CSV.
     */
    private String escapeForCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // Escapar comillas y caracteres especiales
        String escaped = value.replace("\"", "\"\"");
        
        // Si contiene comas, comillas o saltos de línea, encerrarlo en comillas
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            escaped = "\"" + escaped + "\"";
        }
        
        return escaped;
    }
    
    /**
     * Escapa caracteres especiales para JSON.
     */
    private String escapeForJson(String value) {
        if (value == null) {
            return "";
        }
        
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
