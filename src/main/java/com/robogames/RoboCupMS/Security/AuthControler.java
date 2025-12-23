package com.robogames.RoboCupMS.Security;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Security.AuthService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(GlobalConfig.API_PREFIX)
public class AuthControler {

    @Autowired
    private AuthService authService;

    /**
     * Odhlasi uzivatele ze systemu (odstrani pristupovy token z databaze)
     * 
     * @param email Email uzivatele
     * @return Status
     */
    @PostMapping("/logout")
    public Response logout() {
        try {
            this.authService.logout();
            return ResponseHandler.response("success");
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
    }

    /**
     * Vymeni code prijaty z Keycloaku za pristupovy token prihlaseneho
     * uzivatele, overi platnost tokenu a vyhodnoti data ziskana z tokenu
     * 
     * @param code
     * @return Status
     */
    @PostMapping("/validate")
    public Response validate(@RequestBody Map<String, String> json) {
        String code = json.get("code");
        try {
            String token = this.authService.exchange(code);
            return ResponseHandler.response(token);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseHandler.error(ex.getMessage());
        }
    }

}
