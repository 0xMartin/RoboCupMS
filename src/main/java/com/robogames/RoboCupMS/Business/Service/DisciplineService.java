package com.robogames.RoboCupMS.Business.Service;

import java.util.List;
import java.util.Optional;

import com.robogames.RoboCupMS.Communication;
import com.robogames.RoboCupMS.Business.Object.DisciplineObj;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.ScoreAggregation;
import com.robogames.RoboCupMS.Repository.DisciplineRepository;
import com.robogames.RoboCupMS.Repository.ScoreAggregationRepository;

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

        // vsem robotum odebere tuto disciplinu
        d.get().getRobots().stream().forEach((r) -> {
            r.setDicipline(null);
        });

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

        Optional<Discipline> map = this.disciplineRepository.findById(id)
                .map(d -> {
                    d.setName(disciplineObj.getName());
                    d.setDescription(disciplineObj.getDescription());
                    d.setTime(disciplineObj.getTime());
                    d.setScoreAggregation(score.get());
                    d.setMaxRounds(disciplineObj.getMaxRounds());
                    return this.disciplineRepository.save(d);
                });
        if (!map.isPresent()) {
            throw new Exception(String.format("failure, dicipline with ID [%d] not found", id));
        }
    }

}
