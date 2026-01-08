package com.robogames.RoboCupMS.Controller;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Response;
import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Service.DiplomaTemplateService;
import com.robogames.RoboCupMS.Entity.DiplomaTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(GlobalConfig.API_PREFIX + "/diploma")
@Transactional
public class DiplomaController {

    @Autowired
    private DiplomaTemplateService diplomaTemplateService;

    /**
     * Vrátí aktuální template pro diplomy
     * 
     * @return Template pro diplomy
     */
    @GetMapping("/template")
    @Secured({ ERole.Names.ADMIN })
    Response getTemplate() {
        DiplomaTemplate template;
        try {
            template = this.diplomaTemplateService.getTemplate();
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        
        return ResponseHandler.response(Map.of("value", template.getValue()));
    }

    /**
     * Uloží nebo aktualizuje template pro diplomy
     * 
     * @param body JSON objekt obsahující pole "value" s novým template
     * @return Uložený template
     */
    @PostMapping("/template")
    @Secured({ ERole.Names.ADMIN })
    Response saveTemplate(@RequestBody Map<String, String> body) {
        String value = body.get("value");
        
        if (value == null) {
            return ResponseHandler.error("Field 'value' is required");
        }
        
        DiplomaTemplate template;
        try {
            template = this.diplomaTemplateService.saveTemplate(value);
        } catch (Exception ex) {
            return ResponseHandler.error(ex.getMessage());
        }
        
        return ResponseHandler.response(Map.of("value", template.getValue()));
    }
}