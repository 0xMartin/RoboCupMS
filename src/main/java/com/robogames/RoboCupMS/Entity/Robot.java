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
     * Seznam vsech odehranych zapasu
     */
    @OneToMany(mappedBy = "robot", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<RobotMatch> matches;

    /**
     * Vytvori robota, ten pokud se prihlasi do urcite kategorie tak muze zapasit
     */
    public Robot() {
        this.confirmed = false;
        this.matches = new ArrayList<RobotMatch>();
    }

    /**
     * Vytvori robota, ten pokud se prihlasi do urcite kategorie tak muze zapasit
     * 
     * @param _name             Jmeno robota
     * @param _number           Cislo robota (toto cislo identifikuje robota pri
     *                          jednotlivych
     *                          zapasech)
     * @param _teamRegistration Registrace tymu, na kterou je robot vytvoren
     */
    public Robot(String _name, long _number, TeamRegistration _teamRegistration) {
        this.name = _name;
        this.number = _number;
        this.teamRegistration = _teamRegistration;
        this.confirmed = false;
        this.matches = new ArrayList<RobotMatch>();
    }

    /**
     * Navrati ID robota
     * 
     * @return ID
     */
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
     * Seznam vsech zapasu, ktere robot odehral nebo jsou jen naplanovane
     * 
     * @return Seznam zapasu
     */
    @JsonIgnore
    public List<RobotMatch> getMatches() {
        return this.matches;
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
