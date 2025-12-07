package com.robogames.RoboCupMS.Business.Service;

import java.util.List;
import java.util.Optional;

import com.robogames.RoboCupMS.Communication;
import com.robogames.RoboCupMS.Business.Object.CompetitionObj;
import com.robogames.RoboCupMS.Entity.Competition;
import com.robogames.RoboCupMS.Entity.TeamRegistration;
import com.robogames.RoboCupMS.Repository.CompetitionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Zajistuje spravu soutezi
 */
@Service
public class CompetitionService {

    @Autowired
    private CompetitionRepository repository;

    /**
     * Typy zprav
     */
    public static enum Message {
        CREATE,
        REMOVE,
        START
    }

    /**
     * Navrati vsechny uskutecnene a naplanovane rocniky soutezi
     * 
     * @return List soutezi
     */
    public List<Competition> getAll() {
        List<Competition> all = repository.findAll();
        return all;
    }

    /**
     * Navrati vsechny registrace tymu pro dany rocnik souteze
     * 
     * @param year Rocnik souteze
     * @return List vsech registraci
     */
    public List<TeamRegistration> allRegistrations(int year) throws Exception {
        Optional<Competition> c = this.repository.findByYear(year);
        if (c.isPresent()) {
            return c.get().getRegistrations();
        } else {
            throw new Exception(String.format("failure, compatition [year: %d] not exists", year));
        }
    }

    /**
     * Vytvori novy rocnik souteze
     * 
     * @param compatitionObj Parametry nové souteze
     */
    public void create(CompetitionObj compatitionObj) throws Exception {
        if (this.repository.findByYear(compatitionObj.getYear()).isPresent()) {
            throw new Exception("failure, the competition has already been created for this year");
        }

        Competition c = new Competition(
                compatitionObj.getYear(),
                compatitionObj.getDate(),
                compatitionObj.getStartTime(),
                compatitionObj.getEndTime());
        this.repository.save(c);

        // odesle do komunikacniho systemu zpravu
        Communication.getInstance().sendAll(this, CompetitionService.Message.CREATE);
    }

    /**
     * Odstrani soutez z databaze a s ni i vsechny data
     * 
     * @param id ID souteze
     */
    public void remove(Long id) throws Exception {
        Optional<Competition> c = this.repository.findById(id);
        if (!c.isPresent()) {
            throw new Exception(String.format("failure, compatition with ID [%d] not exists", id));
        }

        // pokud soutezi jiz zacala nebude mozne ji odstranit
        if(c.get().getStarted()) {
            throw new Exception(String.format("failure, compatition with ID [%d] already begin", id));    
        }

        // odstrani soutez
        this.repository.delete(c.get());

        // odesle do komunikacniho systemu zpravu
        Communication.getInstance().sendAll(this, CompetitionService.Message.REMOVE);
    }

    /**
     * Upravi parametry souteze, mozne jen pokud jeste nezacala
     * 
     * @param id             ID souteze jejiz parametry maji byt upraveny
     * @param compatitionObj Nové paramtery souteze
     */
    public void edit(Long id, CompetitionObj compatitionObj) throws Exception {
        Optional<Competition> c = repository.findById(id);
        if (!c.isPresent()) {
            throw new Exception(String.format("failure, compatition with ID [%d] not exists", id));
        }

        if (c.get().getStarted()) {
            throw new Exception(String.format("failure, compatition with ID [%d] has already begun", id));
        }

        // provede zmeny
        c.get().setYear(compatitionObj.getYear());
        c.get().setDate(compatitionObj.getDate());
        c.get().setStartTime(compatitionObj.getStartTime());
        c.get().setEndTime(compatitionObj.getEndTime());
        repository.save(c.get());
    }

    /**
     * Zahaji soutez
     * 
     * @param id ID souteze
     * @throws Exception
     */
    public void start(Long id) throws Exception {
        // overi zda soutez existuje
        Optional<Competition> competition = this.repository.findById(id);
        if (!competition.isPresent()) {
            throw new Exception(String.format("failure, competition with ID [%d] not exists", id));
        }

        // spusti soutez a ulozi zmeny
        competition.get().setStarted(true);
        this.repository.save(competition.get());

        // odesle do komunikacniho systemu zpravu
        Communication.getInstance().sendAll(this, CompetitionService.Message.START);
    }

}
