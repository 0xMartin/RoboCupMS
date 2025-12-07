package com.robogames.RoboCupMS.Business.Service;

import java.util.List;
import java.util.Optional;

import com.robogames.RoboCupMS.Business.Object.PlaygroundObj;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.Playground;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Repository.DisciplineRepository;
import com.robogames.RoboCupMS.Repository.PlaygroundRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Zajistuje spravu hrist
 */
@Service
public class PlaygroundService {

    @Autowired
    private PlaygroundRepository playgroundRepository;

    @Autowired
    private DisciplineRepository disciplineRepository;

    /**
     * Navrati vsechny hriste
     * 
     * @return Seznam vsech souteznich hrist
     */
    public List<Playground> getAll() {
        List<Playground> all = this.playgroundRepository.findAll();
        return all;
    }

    /**
     * Navrati vsechny hriste pro urcitou disciplinu
     * 
     * @param id ID discipliny
     * @return Seznam vsech souteznich hrist pro urcitou disciplinu
     */
    public List<Playground> get(Long id) throws Exception {
        // overi zda disciplina existuje
        Optional<Discipline> discipline = this.disciplineRepository.findById(id);
        if (!discipline.isPresent()) {
            throw new Exception(String.format("failure, discipline with ID [%d] not exists", id));
        }

        return discipline.get().getPlaygrounds();
    }

    /**
     * Navrati vsechny zapasy odehrane na konkretnim hristi
     * 
     * @param id ID hriste
     * @return Seznam zapasu
     */
    public List<RobotMatch> getMatches(Long id) throws Exception {
        // overi zda disciplina existuje
        Optional<Playground> p = this.playgroundRepository.findById(id);
        if (p.isPresent()) {
            return p.get().getMatches();
        } else {
            throw new Exception(String.format("failure, playground with ID [%d] not exists", id));
        }
    }

    /**
     * Vytvori nove soutezni hriste
     * 
     * @param playgroundObj Paramtery noveho hriste
     */
    public void create(PlaygroundObj playgroundObj) throws Exception {
        // overi zda disciplina existuje
        Optional<Discipline> discipline = this.disciplineRepository.findById(playgroundObj.getDisciplineID());
        if (!discipline.isPresent()) {
            throw new Exception(
                    String.format("failure, discipline with ID [%d] not exists", playgroundObj.getDisciplineID()));
        }

        // vytvori hriste
        Playground p = new Playground(playgroundObj.getName(), playgroundObj.getNumber(), discipline.get());
        this.playgroundRepository.save(p);
    }

    /**
     * Odstrani soutezni hriste
     * 
     * @param id ID hriste, ktere ma byt odstraneno
     */
    public void remove(Long id) throws Exception {
        Optional<Playground> p = this.playgroundRepository.findById(id);
        if (p.isPresent()) {
            // odstrani hriste
            this.playgroundRepository.delete(p.get());
        } else {
            throw new Exception(String.format("failure, playground with ID [%d] not exists", id));
        }
    }

    /**
     * Upravi parametry souzezniho hriste
     * 
     * @param id            ID hriste, ktere ma byt upraveno
     * @param playgroundObj Nove parametry hriste
     * 
     */
    public void edit(Long id, PlaygroundObj playgroundObj) throws Exception {
        // overi zda disciplina existuje
        Optional<Discipline> discipline = this.disciplineRepository.findById(playgroundObj.getDisciplineID());
        if (!discipline.isPresent()) {
            throw new Exception(String.format("failure, discipline with ID [%d] not exists", playgroundObj.getDisciplineID()));
        }

        // provede zmeni
        Optional<Playground> map = this.playgroundRepository.findById(id)
                .map(p -> {
                    p.setName(playgroundObj.getName());
                    p.setNumber(playgroundObj.getNumber());
                    p.setDiscipline(discipline.get());
                    return this.playgroundRepository.save(p);
                });
        if (!map.isPresent()) {
            throw new Exception(String.format("failure, playground with ID [%d] not exists", id));
        }
    }

}
