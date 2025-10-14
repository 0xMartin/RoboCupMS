package com.robogames.RoboCupMS.Entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.robogames.RoboCupMS.AppInit;
import com.robogames.RoboCupMS.Business.Enum.EScoreAggregation;
import com.robogames.RoboCupMS.Repository.ScoreAggregationRepository;

@Entity(name = "discipline")
public class Discipline {

    /**
     * Navratova hodnota ID "zadne" discipliny (navrati pokud robot neni registrovan
     * v zadne
     * discipline)
     */
    public static final int NOT_REGISTRED = -1;

    /**
     * Hodnota parametru "maxRounds" pokud nema byt robot limitovat pocetem zapasu
     */
    public static final int NOT_LIMITED_NUMBER_OF_ROUNDS = -1;

    /**
     * ID discipliny
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Nazev discipliny
     */
    @Column(name = "name", length = 80, nullable = false, unique = false)
    private String name;

    /**
     * Agregacni funkce skore (pouziva se pro automaticke vyhodnoceni skore)
     */
    @ManyToOne(optional = false)
    private ScoreAggregation scoreAggregation;

    /**
     * Casový limit na jeden zapas (v sekundách)
     */
    @Column(name = "time", length = 8192, nullable = false, unique = false)
    private int time;

    /**
     * Maximalni pocet zapasu odehranych robotem (hodnota bude ignorovana v pripada
     * zaporneho cisla)
     */
    @Column(name = "maxRounds", nullable = false, unique = false)
    private int maxRounds;

    /**
     * Popis discipliny
     */
    @Column(name = "description", length = 8192, nullable = true, unique = false)
    private String description;

    /**
     * Seznam vsech hrist pro tuto disciplinu
     */
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.REMOVE)
    private List<Playground> playgrounds;

    /**
     * Seznam vsech robotu, kteri jsou registrovani v teto discipline
     */
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.REMOVE)
    private List<Robot> robots;

    /**
     * Disciplina, ve ktere muzou roboti soutezit
     */
    public Discipline() {
        this.playgrounds = new ArrayList<Playground>();
    }

    /**
     * Disciplina, ve ktere muzou roboti soutezit
     * 
     * @param _name             Nazev discipliny
     * @param _description      Popis discipliny (max 8192 znaku)
     * @param _scoreAggregation Agregacni funkce skore (pouziva se pro automaticke
     *                          vyhodnoceni skore)
     * @param _time             Casový limit na jeden zapas (v sekundách)
     * @param _maxRounds        Maximalni pocet zapasu odehranych robotem (hodnota
     *                          bude ignorovana v pripada zaporneho cisla)
     */
    public Discipline(String _name, String _description, EScoreAggregation _scoreAggregation, int _time,
            int _maxRounds) {
        this.name = _name;
        ScoreAggregationRepository repository = (ScoreAggregationRepository) AppInit.contextProvider()
                .getApplicationContext()
                .getBean("scoreAggregationRepository");
        this.scoreAggregation = repository.findByName(_scoreAggregation).get();
        this.description = _description;
        this.time = _time;
        this.playgrounds = new ArrayList<Playground>();
        this.maxRounds = _maxRounds;
    }

    /**
     * Navrati ID discipliny
     * 
     * @return ID
     */
    public Long getID() {
        return this.id;
    }

    /**
     * Navrati nazev discipliny
     * 
     * @return Naze discipliny
     */
    public String getName() {
        return this.name;
    }

    /**
     * Navrati agregacni funkci skore
     * 
     * @return Agregacni funkce skore
     */
    public ScoreAggregation getScoreAggregation() {
        return this.scoreAggregation;
    }

    /**
     * Navrati casovy limit na odehrani jednoho zapasu
     * 
     * @return Casovy limit (sekundy)
     */
    public int getTime() {
        return this.time;
    }

    /**
     * Navrati maximalni pocet zapasu, ktere robot muze v teto discipline odehrat. V
     * pripade pokud pocet neni nijak omezen navrati neplatnou zapornou hodnotu
     * 
     * @return Pocet zapasu
     */
    public int getMaxRounds() {
        return this.maxRounds;
    }

    /**
     * Navrati popis discipliny
     * 
     * @return Popis discipliny
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Navrati seznam vsech hrist pro tuto disciplinu
     * 
     * @return Seznam vsech hrist pro tuto disciplinu
     */
    @JsonIgnore
    public List<Playground> getPlaygrounds() {
        return this.playgrounds;
    }

    /**
     * Navrati seznam vsech robotu registrovanych v discipline
     * 
     * @return Seznam vsech robotu v discipliny
     */
    @JsonIgnore
    public List<Robot> getRobots() {
        return this.robots;
    }

    /**
     * Nastavi novy nazev discipline
     * 
     * @param _name Novy nazev
     */
    public void setName(String _name) {
        this.name = _name;
    }

    /**
     * Nastavi novou agragacni funkci skore
     * 
     * @param _scoreAggregation Nova agregacni funkce pro skore
     */
    public void setScoreAggregation(ScoreAggregation _scoreAggregation) {
        this.scoreAggregation = _scoreAggregation;
    }

    /**
     * Nastavi novy popis discipliny
     * 
     * @param _description Novy popis discipliny (max 8192 znaku)
     */
    public void setDescription(String _description) {
        this.description = _description;
    }

    /**
     * Nastavi casovy limit na odehráni jednoto zapasu
     * 
     * @param _time Cas v sekundach
     */
    public void setTime(int _time) {
        this.time = _time;
    }

    /**
     * Nastavi maximalni pocet kol jednoho zapasu
     * 
     * @param _maxRounds Pocet kol
     */
    public void setMaxRounds(int _maxRounds){
        this.maxRounds = _maxRounds;
    }
}
