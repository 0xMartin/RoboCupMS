package com.robogames.RoboCupMS;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Stara se odpovedi ve vsech kontrolerech
 */
@Component
public class ResponseHandler {

    /**
     * Bezna odezva serveru. Pro navraceni odpovedi na pozadavek. (pokud vse
     * probehlo v poradku)
     * 
     * @param data Zprava
     * @return Response
     */
    public static Response response(Object data) {
        return new Response(Response.Type.RESPONSE, data);
    }

    /**
     * Zprava s varovanim
     * 
     * @param data Zprava
     * @return Response
     */
    public static Response warning(Object data) {
        return new Response(Response.Type.WARNING, data);
    }

    /**
     * Zprava ohlasujici chybu
     * 
     * @param data Zprava
     * @return Response
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public static Response error(Object data) {
        return new Response(Response.Type.ERROR, data);
    }

}
