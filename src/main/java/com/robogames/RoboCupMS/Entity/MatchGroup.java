package com.robogames.RoboCupMS.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.robogames.RoboCupMS.Repository.MatchGroupRepository;

/**
 * Herni skupina. Pouzije se jen v pripade pokud proti sobe zapasi vice robotu a
 * je nutne rozhodnou, ktery z nich vyhral a dostava body. (napr.: robo sumo,
 * robo strong, ...)
 */
@Entity(name = "match_group")
public class MatchGroup {

    /**
     * Navratova hodnota ID pokud neni definovana zadna skupina
     */
    public static final long NOT_IN_GROUP = -1;

    /**
     * ID skupiny
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifikato tvurce skupiny. Muze byt libovolny (napr ID uzivatele, ID
     * systemu, ktery automatizovane vytvori skupinu zapasu)
     */
    @Column(name = "creatorIdentifier", nullable = false, unique = false)
    private Long creatorIdentifier;

    /**
     * Zapasy, ktere jsou v teto skupine
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<RobotMatch> matches;

    /**
     * Vytvori skupinu zapasu pro vice robotu
     */
    public MatchGroup() {
        this.matches = new ArrayList<RobotMatch>();
    }

    /**
     * Vytvori skupinu zapasu pro vice robotu
     * 
     * @param _creatorIdentifier Identifikator tvurce skupiny
     */
    public MatchGroup(long _creatorIdentifier) {
        this.creatorIdentifier = _creatorIdentifier;
        this.matches = new ArrayList<RobotMatch>();
    }

    /**
     * Navrati ID skupiny
     * 
     * @return ID skupiny
     */
    public long getID() {
        return this.id;
    }

    /**
     * Navrati identifikator tvurce skupiny
     * 
     * @return ID tvurce
     */
    public long getCreatorIdentifier() {
        return this.creatorIdentifier;
    }

    /**
     * Navrati vsechny zapas teto skupiny
     * 
     * @return Seznam vsech zapasu
     */
    @JsonIgnore
    public List<RobotMatch> getMatches() {
        return this.matches;
    }

    /**
     * Nastavi identifikator tvurce skupiny
     * 
     * @param _creatorIdentifier Novy indentifikator tvurce
     */
    public void setCreatorIdentifier(long _creatorIdentifier) {
        this.creatorIdentifier = _creatorIdentifier;
    }

    /**
     * Vygeneruje unikatni identifikator tvurce zapasove skupiny
     * 
     * @return Unikatni identifikator
     */
    public static long generateCreatorIdentifier(MatchGroupRepository repository) {
        final List<MatchGroup> all = repository.findAll();
        final Random rnd = new Random();
        // nahodne vygeneruje identifikacni cislo, pokud jiz existuje tak generuje znovu
        // (omezene pocetem iteraci)
        long creatorIdentifier = 0;
        for (int i = 0; i < 500; ++i) {
            creatorIdentifier = rnd.nextLong();
            // test unikatnosti
            boolean unique = true;
            for (MatchGroup m : all) {
                if (m.getCreatorIdentifier() == creatorIdentifier) {
                    unique = false;
                    break;
                }
            }
            if (unique) {
                break;
            }
        }
        return creatorIdentifier;
    }

}
