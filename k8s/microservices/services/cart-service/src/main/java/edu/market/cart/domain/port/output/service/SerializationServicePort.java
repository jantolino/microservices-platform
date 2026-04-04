package edu.market.cart.domain.port.output.service;

public interface SerializationServicePort {
    
    String serialize(Object object);
    
    <T> T deserialize(String json, Class<T> type);
}
