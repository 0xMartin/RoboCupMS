package com.robogames.RoboCupMS.Entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.robogames.RoboCupMS.Business.Enum.ECategory;

/**
 * Robot, se kterym soutezi tymy. Nejdrive je nutne robot vytvorit a pak ho
 * registrovat do nejake kategorie. V den konani souteze pak asistent
 * zkontroluje robota jestli splnuje pozadavky a systemu povrdi jeho registraci.
 */
@Entity(name = "robot")
public class Robot {

    /**
     * ID robota
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cislo robota (unikatni v ramci rocniku a sve kategorie)
     */
    @Column(name = "number", nullable = false, unique = false)
    private Long number;

    /**
     * Jmeno robota (unikatni v ramci rocniku)
     */
    @Column(name = "name", length = 40, nullable = false, unique = false)
    private String name;

    /**
     * Disciplina, ve ktere bude robot soutezit. Jeden robot muze soutezit vzdy jen
     * v jedne discipline. Pokod by pravidla souteze dovolovali, aby jeden fyzicky
     * robot mohl soutezit i ve vice discpilinach tak i v tomto pripade je nutne v
     * systemu vytvorit dva roboty (v ramci kategorie musi mit kazdy robot unikatni
     * identifikacni cislo)
     */
    @ManyToOne
    private Discipline discipline;

    /**
     * Robot se vytvari na registraci tymu do souteze (kazdeho robota je tedy nutne
     * znovu vytvaret pro kazdy rocnik znovu)
     */
    @ManyToOne
    private TeamRegistration teamRegistration;

    /**
     * Povrzeni registrace. Overeni provadi asisten primo na soutezi. Robot muze
     * soutezit jen pokud je jeho registrace povtzena.
     */
    @Column(name = "confirmed", nullable = false, unique = false)
    private Boolean confirmed;

    /**
     * List of all matches where this robot is robot A
     */
    @OneToMany(mappedBy = "robotA", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<RobotMatch> matchesAsRobotA;

    /**
     * List of all matches where this robot is robot B
     */
    @OneToMany(mappedBy = "robotB", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<RobotMatch> matchesAsRobotB;

    /**
     * Creates a robot that can compete in categories
     */
    public Robot() {
        this.confirmed = false;
        this.matchesAsRobotA = new ArrayList<RobotMatch>();
        this.matchesAsRobotB = new ArrayList<RobotMatch>();
    }

    /**
     * Creates a robot that can compete in categories
     * 
     * @param _name             Robot name
     * @param _number           Robot number (identifies the robot during matches)
     * @param _teamRegistration Team registration to which this robot belongs
     */
    public Robot(String _name, long _number, TeamRegistration _teamRegistration) {
        this.name = _name;
        this.number = _number;
        this.teamRegistration = _teamRegistration;
        this.confirmed = false;
        this.matchesAsRobotA = new ArrayList<RobotMatch>();
        this.matchesAsRobotB = new ArrayList<RobotMatch>();
    }

    /**
     * Navrati ID robota
     * 
     * @return ID
     */
    @JsonProperty("id")
    public Long getID() {
        return this.id;
    }

    /**
     * Navrati jmeno robota
     * 
     * @return Jmeno robota
     */
    public String getName() {
        return this.name;
    }

    /**
     * Navrati identifikacni cislo robota
     * 
     * @return Cislo robota
     */
    public Long getNumber() {
        return this.number;
    }

    /**
     * Navrati ID registrace tymu
     * 
     * @return ID registrace tymu
     */
    public Long getTeamRegistrationID() {
        return this.teamRegistration.getID();
    }

    /**
     * Navrati jmeno tymu, ktery tohoto robota vlastni
     * 
     * @return Jmeno tymu
     */
    public String getTeamName() {
        return this.teamRegistration.getTeamName();
    }

    /**
     * Navrati registraci tymu, na kterou je tento robot vytvoren
     * 
     * @return TeamRegistration
     */
    @JsonIgnore
    public TeamRegistration getTeamRegistration() {
        return this.teamRegistration;
    }

    /**
     * Navrati ID discipliny, ve ktere robot soutezi
     * 
     * @return ID discipliny
     */
    public long getDisciplineID() {
        if (this.discipline == null) {
            return Discipline.NOT_REGISTRED;
        } else {
            return this.discipline.getID();
        }
    }

    /**
     * Navrati nazev discipliny, ve ktere robot soutezi
     * 
     * @return Nazev discipliny
     */
    public String getDiciplineName() {
        if (this.discipline == null) {
            return "";
        } else {
            return this.discipline.getName();
        }
    }

    /**
     * Navrati kategorii, ve ktere robot soutezi
     * 
     * @return Soutezni kategorie
     */
    public ECategory getCategory() {
        return this.teamRegistration.getCategory();
    }

    /**
     * Navrati informaci o tom zda byla registrace robota jiz potvrzena
     * 
     * @return boolean
     */
    public boolean getConfirmed() {
        return this.confirmed;
    }

    /**
     * Navrati pocet clenu tymu, ktery vlastni tohoto robota
     * 
     * @return Pocet clenu tymu
     */
    public int getTeamMemberCount() {
        return this.teamRegistration.getTeam().getMemberCount();
    }

    /**
     * Navrati disciplinu, ve ktere robot soutezi
     * 
     * @return Discipline
     */
    @JsonIgnore
    public Discipline getDiscipline() {
        return this.discipline;
    }

    /**
     * Get all matches where this robot participates (as robot A or B)
     * 
     * @return List of all matches
     */
    @JsonIgnore
    public List<RobotMatch> getMatches() {
        List<RobotMatch> allMatches = new ArrayList<RobotMatch>();
        if (this.matchesAsRobotA != null) {
            allMatches.addAll(this.matchesAsRobotA);
        }
        if (this.matchesAsRobotB != null) {
            allMatches.addAll(this.matchesAsRobotB);
        }
        return allMatches;
    }

    /**
     * Get matches where this robot is robot A
     * 
     * @return List of matches as robot A
     */
    @JsonIgnore
    public List<RobotMatch> getMatchesAsRobotA() {
        return this.matchesAsRobotA;
    }

    /**
     * Get matches where this robot is robot B
     * 
     * @return List of matches as robot B
     */
    @JsonIgnore
    public List<RobotMatch> getMatchesAsRobotB() {
        return this.matchesAsRobotB;
    }

    /**
     * Nastavi nove jmeno robota
     * 
     * @param _name Nove jmeno robota
     */
    public void setName(String _name) {
        this.name = _name;
    }

    /**
     * Nastavi nove identifikacni cislo robota
     * 
     * @param _number Nove identifikacni cislo
     */
    public void setNumber(long _number) {
        this.number = _number;
    }

    /**
     * Nastavi tymovou registraci v damen rocniku souteze
     * 
     * @param _registration Tymova registrace
     */
    public void setTeamRegistration(TeamRegistration _registration) {
        this.teamRegistration = _registration;
    }

    /**
     * Nastavi disciplinu, ve ktere bude robot soutezit
     * 
     * @param _discipline Disciplina, do ktere robota registujem
     */
    public void setDicipline(Discipline _discipline) {
        this.discipline = _discipline;
    }

    /**
     * Nastavi povrzeni registrace
     * 
     * @param _confirmed Nova hodnota
     */
    public void setConfirmed(boolean _confirmed) {
        this.confirmed = _confirmed;
    }

}
