package com.robogames.RoboCupMS.Business.Service;

import com.robogames.RoboCupMS.Entity.DiplomaTemplate;
import com.robogames.RoboCupMS.Repository.DiplomaTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Zajišťuje správu template pro diplomy
 */
@Service
public class DiplomaTemplateService {

    @Autowired
    private DiplomaTemplateRepository diplomaTemplateRepository;

    /**
     * Vrátí aktuální template pro diplomy
     * 
     * @return Template pokud existuje
     * @throws Exception Pokud template není v databázi
     */
    public DiplomaTemplate getTemplate() throws Exception {
        Optional<DiplomaTemplate> template = diplomaTemplateRepository.findFirstByOrderByIdDesc();
        
        if (!template.isPresent()) {
            throw new Exception("Template for diplomas not found");
        }
        
        return template.get();
    }

    /**
     * Uloží nebo aktualizuje template pro diplomy
     * 
     * @param value Obsah template
     * @return Uložený template
     * @throws Exception Pokud dojde k chybě při ukládání
     */
    @Transactional
    public DiplomaTemplate saveTemplate(String value) throws Exception {
        if (value == null || value.trim().isEmpty()) {
            throw new Exception("Template value cannot be null or empty");
        }

        // Vytvoříme nový záznam pro historii
        DiplomaTemplate template = new DiplomaTemplate(value);
        return diplomaTemplateRepository.save(template);
    }
}