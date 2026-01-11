package com.robogames.RoboCupMS.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entita reprezentujici zapas
 */
@Entity(name = "robot_match")
public class RobotMatch {

    /**
     * ID zapasu
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Robot, ktery bude soutezit
     */
    @ManyToOne(optional = false)
    private Robot robot;

    /**
     * Skupina, ve ktere robot soutezi (null -> pokud nesoutezi v zadne skupine)
     */
    @ManyToOne(optional = true)
    private MatchGroup group;

    /**
     * Hriste, na kterem se bude soutezit
     */
    @ManyToOne(optional = false)
    private Playground playground;

    /**
     * Aktualni stav zapasu
     */
    @ManyToOne(optional = false)
    private MatchState state;

    /**
     * Vysledne skore zapasu (kolik bodu dostava robot)
     */
    @Column(name = "score", nullable = true, unique = false)
    private float score;

    /**
     * Vytvori "naplanuje" zapas. Je mozne planovat skupinove zapasy (robo sumu,
     * robo strong, ...), nebo jen ciste zapsat vysledek zapasu (line follower =>
     * cas)
     */
    public RobotMatch() {
        this.score = 0;
    }

    /**
     * Vytvori "naplanuje" zapas. Je mozne planovat skupinove zapasy (robo sumu,
     * robo strong, ...), nebo jen ciste zapsat vysledek zapasu (line follower =>
     * cas)
     * 
     * @param _robot      Robot, ktery bude soutezit
     * @param _group      Skupina, ve ktere robot soutezi (null -> pokud nesoutezi v
     *                    zadne skupine)
     * @param _playground Hriste, na kterem se bude soutezit
     * @param _state      Aktualni stav zapasu
     */
    public RobotMatch(Robot _robot, MatchGroup _group, Playground _playground, MatchState _state) {
        this.robot = _robot;
        this.group = _group;
        this.playground = _playground;
        this.state = _state;
        this.score = 0;
    }

    /**
     * Navrati ID zapasu
     * 
     * @return ID zapasu
     */
    public long getID() {
        return this.id;
    }

    /**
     * Navrati skore zapasu
     * 
     * @return Skore
     */
    public float getScore() {
        return this.score;
    }

    /**
     * Navrati ID robota
     * 
     * @return ID robota
     */
    public long getRobotID() {
        return this.robot.getID();
    }

    /**
     * Navrati identifikacni cislo robota
     * 
     * @return Identifikacni cislo robota
     */
    public long getRobotNumber() {
        return this.robot.getNumber();
    }

    /**
     * Navrati jmeno robota
     * 
     * @return Jmeno robota
     */
    public String getRobotName() {
        return this.robot.getName();
    }

    /**
     * Navrati ID tymu
     * 
     * @return ID tymu
     */
    public long getTeamID() {
        return this.robot.getTeamRegistration().getTeamID();
    }

    /**
     * Navrati ID zapasove skupiny. Pokud neni ve zadne skupine navrati
     * MatchGroup.NOT_IN_GROUP
     * 
     * @return ID zapasove skupiny
     */
    public long getGroupID() {
        if (this.group == null) {
            return MatchGroup.NOT_IN_GROUP;
        } else {
            return this.group.getID();
        }
    }

    /**
     * Navrati ID hriste, na kterem se kona zapas
     * 
     * @return ID hriste
     */
    public long getPlaygroundID() {
        return this.playground.getID();
    }

    /**
     * Navrati aktualni stav zapasu
     * 
     * @return Aktualni stav zapasu
     */
    public MatchState getState() {
        return this.state;
    }

    /**
     * Navrati robota
     * 
     * @return Robot
     */
    @JsonIgnore
    public Robot getRobot() {
        return this.robot;
    }

    /**
     * Navrati hriste
     * 
     * @return Hriste
     */
    @JsonIgnore
    public Playground getPlayground() {
        return this.playground;
    }

    /**
     * Navrati zapasovou skupinu
     * 
     * @return Zapasova skupina
     */
    @JsonIgnore
    public MatchGroup getMatchGroup() {
        return this.group;
    }

    /**
     * Navrati nazev hriste
     * 
     * @return Nazev hriste
     */
    public String getPlaygroundName() {
        return this.playground.getName();
    }

    /**
     * Navrati cislo hriste
     * 
     * @return Cislo hriste
     */
    public int getPlaygroundNumber() {
        return this.playground.getNumber();
    }

    /**
     * Navrati nazev tymu
     * 
     * @return Nazev tymu
     */
    public String getTeamName() {
        return this.robot.getTeamRegistration().getTeam().getName();
    }

    /**
     * Navrati nazev discipliny
     * 
     * @return Nazev discipliny
     */
    public String getDisciplineName() {
        if (this.robot.getDiscipline() == null) {
            return "";
        }
        return this.robot.getDiscipline().getName();
    }

    /**
     * Navrati kategorii robota
     * 
     * @return Kategorie robota
     */
    public String getCategory() {
        return this.robot.getCategory().toString();
    }

    /**
     * Nastavi score zapasu
     * 
     * @param _score Score
     */
    public void setScore(float _score) {
        this.score = _score;
    }

    /**
     * Zmeni robota
     * 
     * @param _robot Novy robot
     */
    public void setRobot(Robot _robot) {
        this.robot = _robot;
    }

    /**
     * Zmeni hriste
     * 
     * @param _playground Nove hriste
     */
    public void setPlaygrond(Playground _playground) {
        this.playground = _playground;
    }

    /**
     * Zmeni aktualni stav zapasu
     * 
     * @param _state Novy stav zapasu
     */
    public void setMatchState(MatchState _state) {
        this.state = _state;
    }

}
