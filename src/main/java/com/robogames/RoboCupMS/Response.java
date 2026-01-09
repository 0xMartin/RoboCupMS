package com.robogames.RoboCupMS;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Odezva serveru (na vystupu reprezentovana jako json)
 * 
 * FORMAT:
 * {
 *   "type": "RESPONSE" | "WARNING" | "ERROR",
 *   "data": any,
 *   "errorCode": string (pouze pri ERROR, volitelne)
 * }
 * 
 * ERROR CODES PRO FRONTEND:
 * - TOKEN_MISSING, TOKEN_EXPIRED, TOKEN_INVALID - 401, frontend MUSI odhlasit
 * - NO_ROLE - 403, frontend MUSI odhlasit (uzivatel nema zadnou roli)
 * - ACCESS_DENIED - 403, frontend NEOHLASUJE (uzivatel nema opravneni pro akci)
 * - INTERNAL_ERROR - 500, frontend NEOHLASUJE
 * - null/chybi - 400, business chyba, frontend NEOHLASUJE
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

    /**
     * Kod chyby pro frontend (pouze pri ERROR)
     * Umoznuje frontendu rozlisit typ chyby a spravne na ni reagovat.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String errorCode;

    public Response(Response.Type type, Object data) {
        this.type = type;
        this.data = data;
        this.errorCode = null;
    }

    public Response(Response.Type type, Object data, String errorCode) {
        this.type = type;
        this.data = data;
        this.errorCode = errorCode;
    }

    /**
     * Nastavi error code
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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
