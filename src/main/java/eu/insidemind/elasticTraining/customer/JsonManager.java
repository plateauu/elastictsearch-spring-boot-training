package eu.insidemind.elasticTraining.customer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.insidemind.elasticTraining.customer.domain.Customer;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class JsonManager {

    private final ObjectMapper objectMapper;

    JsonManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Customer deserialize(String json) {
        try {
            return objectMapper.readValue(json, Customer.class);
        } catch (IOException e) {
            throw new RuntimeException("new IO exception", e);
        }
    }

    String serialize(Customer customer) {
        try {
            return objectMapper.writeValueAsString(customer);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json parsing exception", e);
        }
    }
}
