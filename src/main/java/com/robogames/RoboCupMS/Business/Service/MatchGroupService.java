package com.robogames.RoboCupMS.Business.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.robogames.RoboCupMS.Business.Object.MatchGroupObj;
import com.robogames.RoboCupMS.Entity.MatchGroup;
import com.robogames.RoboCupMS.Repository.MatchGroupRepository;
import com.robogames.RoboCupMS.Repository.RobotMatchRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Zajistuje spravu zapasovych skupin
 */
@Service
public class MatchGroupService {

    @Autowired
    private MatchGroupRepository repository;

    @Autowired
    private RobotMatchRepository matchRepository;

    /**
     * Navrati vsechny zapasove skupiny
     * 
     * @return Seznam vsech skupin
     */
    public List<MatchGroup> getAll() {
        List<MatchGroup> all = this.repository.findAll();
        return all;
    }

    /**
     * Navrti vsechny zapasove skupiny pro specifikovany identifikator tvurce
     * skupiny
     * 
     * @param creatorID Identifikator tvurce skupiny
     * @return Seznam vsech skupin
     */
    public List<MatchGroup> getByCID(Long creatorID) {
        Stream<MatchGroup> list = this.repository.findAll().stream()
                .filter((r) -> (r.getCreatorIdentifier() == creatorID));
        List<MatchGroup> out = new ArrayList<MatchGroup>();
        list.forEach((m) -> {
            out.add(m);
        });
        return out;
    }

    /**
     * Navrati skupinu s konkretnim ID
     * 
     * @param id ID skupiny
     * @return Zapasova skupina
     */
    public MatchGroup getByID(Long id) throws Exception {
        Optional<MatchGroup> g = this.repository.findById(id);
        if (g.isPresent()) {
            return g.get();
        } else {
            throw new Exception(String.format("failure, group with ID [%d] not exists", id));
        }
    }

    /**
     * Vytvori novou zapasovou skupinu
     * 
     * @param groupObj Parametry nove zapasove skupiny
     * @return Informace o stavu provedeneho requestu
     */
    public void create(MatchGroupObj groupObj) throws Exception {
        MatchGroup g = new MatchGroup(groupObj.getCreatorID());
        this.repository.save(g);
    }

    /**
     * Odstrani skupinu
     * 
     * @param id ID skupiny
     * @return Informace o stavu provedeneho requestu
     */
    public void remove(Long id) throws Exception {
        // overi zda zapasova skupina existuje
        Optional<MatchGroup> group = this.repository.findById(id);
        if (!group.isPresent()) {
            throw new Exception(String.format("failure, match group with ID [%d] not exists", id));
        }

        // odstrani skupinu
        this.repository.delete(group.get());
    }

    /**
     * Odstrani vsechny skupiny s odpovidajicim ID tvurce skupiny
     * 
     * @param creatorid Identifikator tvurce skupiny
     */
    public void removeAll(Long creatorid) throws Exception {
        this.repository.findAll().stream().filter((group) -> (group.getCreatorIdentifier() == creatorid))
                .forEach((group) -> {
                    // odstrani vsechny zapasy skupiny
                    if (!group.getMatches().isEmpty()) {
                        this.matchRepository.deleteAll(group.getMatches());
                    }

                    // odstrani skupinu
                    this.repository.delete(group);
                });
    }

}
