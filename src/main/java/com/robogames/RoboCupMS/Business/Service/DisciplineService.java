package com.robogames.RoboCupMS.Business.Service;

import java.util.List;
import java.util.Optional;

import com.robogames.RoboCupMS.Communication;
import com.robogames.RoboCupMS.Business.Object.DisciplineObj;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.ScoreAggregation;
import com.robogames.RoboCupMS.Entity.ScoreType;
import com.robogames.RoboCupMS.Repository.DisciplineRepository;
import com.robogames.RoboCupMS.Repository.ScoreAggregationRepository;
import com.robogames.RoboCupMS.Repository.ScoreTypeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Zajistuje spravu souteznich disciplin
 */
@Service
public class DisciplineService {

    @Autowired
    private DisciplineRepository disciplineRepository;

    @Autowired
    private ScoreAggregationRepository aggregationRepository;

    @Autowired
    private ScoreTypeRepository scoreTypeRepository;

    /**
     * Typy zprav
     */
    public static enum Message {
        CREATE,
        REMOVE
    }


    /**
     * Navrati vsechny vytvorene discipliny
     * 
     * @return Seznam disciplin
     */
    public List<Discipline> getAll() {
        List<Discipline> all = this.disciplineRepository.findAll();
        return all;
    }

    /**
     * Navrati vsechny viditelne discipliny (pro bezne uzivatele)
     * 
     * @return Seznam viditelnych disciplin
     */
    public List<Discipline> getAllVisible() {
        List<Discipline> all = this.disciplineRepository.findAll();
        return all.stream()
                .filter(d -> !Boolean.TRUE.equals(d.getHidden()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Navarti disciplinu s konkretim ID
     * 
     * @param id ID pozadovane discipliny
     * @return Disciplina
     */
    public Discipline get(Long id) throws Exception {
        Optional<Discipline> d = this.disciplineRepository.findById(id);
        if (d.isPresent()) {
            return d.get();
        } else {
            throw new Exception(String.format("failure, dicipline with ID [%d] not found", id));
        }
    }

    /**
     * Vytvori novou disciplinu
     * 
     * @param disciplineObj Parametry nove discipliny
     */
    public void create(DisciplineObj disciplineObj) throws Exception {
        Discipline discipline = new Discipline(
                disciplineObj.getName(),
                disciplineObj.getDescription(),
                disciplineObj.getScoreAggregation(),
                disciplineObj.getTime(),
                disciplineObj.getMaxRounds());
        
        // Set scoreType if provided
        if (disciplineObj.getScoreType() != null) {
            Optional<ScoreType> scoreType = this.scoreTypeRepository.findByName(disciplineObj.getScoreType());
            if (scoreType.isPresent()) {
                discipline.setScoreType(scoreType.get());
            }
        }
        
        // Set highScoreWin if provided
        if (disciplineObj.getHighScoreWin() != null) {
            discipline.setHighScoreWin(disciplineObj.getHighScoreWin());
        }

        // Set hidden if provided (defaults to false)
        if (disciplineObj.getHidden() != null) {
            discipline.setHidden(disciplineObj.getHidden());
        }
        
        this.disciplineRepository.save(discipline);

        // odesle do komunikacniho systemu zpravu
        Communication.getInstance().sendAll(this, DisciplineService.Message.CREATE);
    }

    /**
     * Z databaze odstrani disciplinu
     * 
     * @param id ID discipliny, ktera ma byt odstraneni
     */
    public void remove(Long id) throws Exception {
        Optional<Discipline> d = this.disciplineRepository.findById(id);

        // overi zda disciplina existuje
        if (!d.isPresent()) {
            throw new Exception(String.format("failure, dicipline with ID [%d] not exists", id));
        }

        // overi zda discipliny nema jiz prirazene nejake hriste
        if (!d.get().getPlaygrounds().isEmpty()) {
            throw new Exception(String.format("failure, dicipline with ID [%d] have created a playgrounds", id));
        }

        // overi zda v discipline nejsou registrovani zadni roboti
        if (!d.get().getRobots().isEmpty()) {
            throw new Exception(String.format("failure, dicipline with ID [%d] has registered robots and cannot be removed", id));
        }

        // odstrani disciplinu
        this.disciplineRepository.delete(d.get());

        // odesle do komunikacniho systemu zpravu
        Communication.getInstance().sendAll(this, DisciplineService.Message.REMOVE);
    }

    /**
     * Upravi disciplinu (nazev nebo popis)
     * 
     * @param id            ID discipliny jejiz data maji byt zmeneny
     * @param disciplineObj Paramtery nove discipliny
     */
    public void edit(Long id, DisciplineObj disciplineObj) throws Exception {
        Optional<ScoreAggregation> score = this.aggregationRepository.findByName(disciplineObj.getScoreAggregation());
        
        // Get scoreType if provided
        Optional<ScoreType> scoreType = Optional.empty();
        if (disciplineObj.getScoreType() != null) {
            scoreType = this.scoreTypeRepository.findByName(disciplineObj.getScoreType());
        }
        final Optional<ScoreType> finalScoreType = scoreType;

        Optional<Discipline> map = this.disciplineRepository.findById(id)
                .map(d -> {
                    d.setName(disciplineObj.getName());
                    d.setDescription(disciplineObj.getDescription());
                    d.setTime(disciplineObj.getTime());
                    d.setScoreAggregation(score.get());
                    d.setMaxRounds(disciplineObj.getMaxRounds());
                    
                    // Set scoreType if provided
                    if (finalScoreType.isPresent()) {
                        d.setScoreType(finalScoreType.get());
                    }
                    
                    // Set highScoreWin if provided
                    if (disciplineObj.getHighScoreWin() != null) {
                        d.setHighScoreWin(disciplineObj.getHighScoreWin());
                    }
                    
                    // Set hidden if provided
                    if (disciplineObj.getHidden() != null) {
                        d.setHidden(disciplineObj.getHidden());
                    }
                    
                    return this.disciplineRepository.save(d);
                });
        if (!map.isPresent()) {
            throw new Exception(String.format("failure, dicipline with ID [%d] not found", id));
        }
    }

}
