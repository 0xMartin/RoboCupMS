package com.robogames.RoboCupMS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Odezva serveru (na vystupu reprezentovana jako json)
 */
public class Response {

    /**
     * Typ odezvy (enum)
     */
    public static enum Type {
        RESPONSE,
        WARNING,
        ERROR
    }

    /**
     * Typ odezvy
     */
    public Response.Type type;

    /**
     * Data odezvy (zprava, objekt, cislo, ...)
     */
    public Object data;

    public Response(Response.Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Navrati ve json formatu
     */
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return String.format("{\'type\':\'%s\',\'data\':\'%s\'}", type.toString(), data.toString());
        }
    }

}
