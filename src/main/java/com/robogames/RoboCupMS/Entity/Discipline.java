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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.robogames.RoboCupMS.AppInit;
import com.robogames.RoboCupMS.Business.Enum.EScoreAggregation;
import com.robogames.RoboCupMS.Business.Enum.EScoreType;
import com.robogames.RoboCupMS.Repository.ScoreAggregationRepository;

@Entity(name = "discipline")
public class Discipline {

    /**
     * Return value for ID when robot is not registered in any discipline
     */
    public static final int NOT_REGISTRED = -1;

    /**
     * Value for "maxRounds" parameter when robot should not be limited by match count
     */
    public static final int NOT_LIMITED_NUMBER_OF_ROUNDS = -1;

    /**
     * Discipline ID
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Discipline name
     */
    @Column(name = "name", length = 80, nullable = false, unique = false)
    private String name;

    /**
     * Score aggregation function (used for automatic score evaluation)
     */
    @ManyToOne(optional = false)
    private ScoreAggregation scoreAggregation;

    /**
     * Score type for this discipline (TIME or SCORE)
     */
    @ManyToOne(optional = true)
    private ScoreType scoreType;

    /**
     * Determines how to select the winner based on score
     * true = higher score wins (e.g., sumo wins count)
     * false = lower score wins (e.g., time-based like line follower)
     */
    @Column(name = "high_score_win", nullable = false)
    private Boolean highScoreWin;

    /**
     * Time limit for one match (in seconds)
     */
    @Column(name = "time", length = 8192, nullable = false, unique = false)
    private int time;

    /**
     * Maximum number of matches a robot can play (negative value means no limit)
     */
    @Column(name = "maxRounds", nullable = false, unique = false)
    private int maxRounds;

    /**
     * Discipline description
     */
    @Column(name = "description", length = 8192, nullable = true, unique = false)
    private String description;

    /**
     * List of all playgrounds for this discipline
     */
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.REMOVE)
    private List<Playground> playgrounds;

    /**
     * List of all robots registered in this discipline
     */
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.REMOVE)
    private List<Robot> robots;

    /**
     * Default constructor
     */
    public Discipline() {
        this.playgrounds = new ArrayList<Playground>();
        this.highScoreWin = true;
    }

    /**
     * Constructor with parameters
     * 
     * @param _name             Discipline name
     * @param _description      Discipline description (max 8192 characters)
     * @param _scoreAggregation Score aggregation function
     * @param _time             Time limit for one match (in seconds)
     * @param _maxRounds        Maximum number of matches (negative = no limit)
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
        this.highScoreWin = true;
        this.playgrounds = new ArrayList<Playground>();
        this.maxRounds = _maxRounds;
    }

    /**
     * Navrati ID discipliny
     * 
     * @return ID
     */
    @JsonProperty("id")
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
     * Get the score aggregation function
     * 
     * @return Score aggregation function
     */
    public ScoreAggregation getScoreAggregation() {
        return this.scoreAggregation;
    }

    /**
     * Get the score type
     * 
     * @return Score type or null
     */
    @JsonIgnore
    public ScoreType getScoreType() {
        return this.scoreType;
    }

    /**
     * Get the score type name
     * 
     * @return Score type name or null
     */
    public EScoreType getScoreTypeName() {
        return this.scoreType != null ? this.scoreType.getName() : null;
    }

    /**
     * Get whether higher score wins in this discipline
     * 
     * @return true if higher score wins
     */
    public Boolean getHighScoreWin() {
        return this.highScoreWin;
    }

    /**
     * Get the time limit for one match
     * 
     * @return Time limit (seconds)
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
     * Set max rounds
     * 
     * @param _maxRounds Number of rounds
     */
    public void setMaxRounds(int _maxRounds){
        this.maxRounds = _maxRounds;
    }

    /**
     * Set the score type
     * 
     * @param _scoreType Score type
     */
    public void setScoreType(ScoreType _scoreType) {
        this.scoreType = _scoreType;
    }

    /**
     * Set whether higher score wins
     * 
     * @param _highScoreWin true if higher score wins
     */
    public void setHighScoreWin(Boolean _highScoreWin) {
        this.highScoreWin = _highScoreWin;
    }
}
