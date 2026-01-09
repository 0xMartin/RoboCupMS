package com.robogames.RoboCupMS;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Globalni handler pro vyjimky.
 * Zajistuje jednotny format odpovedi pro vsechny chyby.
 * 
 * DULEZITE PRO FRONTEND:
 * - 401 = Token problem (missing/expired/invalid) - frontend MUSI odhlasit
 * - 403 s errorCode "NO_ROLE" = Uzivatel nema zadnou roli - frontend MUSI odhlasit
 * - 403 s errorCode "ACCESS_DENIED" = Uzivatel nema opravneni pro tuto akci - frontend NEOHLASUJE
 * - 400 = Business chyba - frontend NEOHLASUJE, zobrazi error message
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // Error kody pro frontend
    public static final String ERROR_ACCESS_DENIED = "ACCESS_DENIED";
    public static final String ERROR_INTERNAL = "INTERNAL_ERROR";

    /**
     * Handler pro AccessDeniedException (Spring Security @Secured/@PreAuthorize)
     * Toto se spusti kdyz uzivatel NEMA roli potrebnou pro pristup k endpointu.
     * 
     * Napr. uzivatel s roli COMPETITOR zkusi pristoupit k /api/admin/* endpointu.
     * 
     * DULEZITE: Vracime 403 s errorCode "ACCESS_DENIED" - frontend to NEMA interpretovat
     * jako problem s tokenem a NEMA odhlasovat uzivatele!
     * 
     * @param ex AccessDeniedException
     * @return JSON response s errorCode
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<Response> handleAccessDeniedException(AccessDeniedException ex) {
        Response response = new Response(Response.Type.ERROR, "You don't have permission for this action");
        response.setErrorCode(ERROR_ACCESS_DENIED);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handler pro vsechny ostatni neocekavane vyjimky.
     * 
     * Toto by se nemelo stat - vsechny ocekavane vyjimky by mely byt zachyceny
     * v controllerech. Pokud se toto spusti, je to bug.
     * 
     * @param ex Exception
     * @return JSON response s generic error message
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Response> handleGenericException(Exception ex) {
        // Zalogujeme pro debugging
        System.err.println("Unhandled exception: " + ex.getClass().getName() + " - " + ex.getMessage());
        ex.printStackTrace();
        
        Response response = new Response(Response.Type.ERROR, "Internal server error");
        response.setErrorCode(ERROR_INTERNAL);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
