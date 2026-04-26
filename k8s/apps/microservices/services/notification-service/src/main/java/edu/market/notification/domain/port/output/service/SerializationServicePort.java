package edu.market.notification.domain.port.output.service;

public interface SerializationServicePort {
    
    String serialize(Object object);
    
    <T> T deserialize(String json, Class<T> type);
}
