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
import com.robogames.RoboCupMS.Business.Enum.ECategory;

@Entity(name = "team_registration")
public class TeamRegistration {

    /**
     * ID registrace tymu (do kazdeho rocniku souteze je nutne tym registrovat
     * znovu)
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Tym, ktery se prihlasuje do souteze
     */
    @ManyToOne
    private Team team;

    /**
     * Rocnik souteze
     */
    @ManyToOne
    private Competition competition;

    /**
     * Kategorie tymu
     */
    @ManyToOne
    private Category category;

    /**
     * Jmeno ucitele
     */
    @Column(name = "teacher_name", length = 40, nullable = true, unique = false)
    private String teacherName;

    /**
     * Prijmeni ucitele
     */
    @Column(name = "teacher_surname", length = 60, nullable = true, unique = false)
    private String teacherSurname;

    /**
     * Kontakt na ucitele
     */
    @Column(name = "teacher_contact", length = 120, nullable = true, unique = false)
    private String teacherContact;

    /**
     * Roboti, kteri jsou vytvoreni na tuto registraci tymu
     */
    @OneToMany(mappedBy = "teamRegistration", cascade = CascadeType.REMOVE)
    private List<Robot> robots;

    /**
     * Vytvori registraci tymu do konkretniho rocniku souteze. Bez registrace neni
     * mozne dale registrovat soutezni roboty.
     */
    public TeamRegistration() {
        this.robots = new ArrayList<Robot>();
        this.teacherName = "";
        this.teacherSurname = "";
        this.teacherContact = "";
    }

    /**
     * Vytvori registraci tymu do konkretniho rocniku souteze. Bez registrace neni
     * mozne dale registrovat soutezni roboty.
     * 
     * @param _team        Tym, ktery se registruje do souteze
     * @param _competition Rocnik souteze, do ktereho se tym registruje
     * @param _category    Kategori, ve ktere bude soutezit
     */
    public TeamRegistration(Team _team, Competition _competition, Category _category) {
        this.team = _team;
        this.competition = _competition;
        this.category = _category;
        this.robots = new ArrayList<Robot>();
        this.teacherName = "";
        this.teacherSurname = "";
        this.teacherContact = "";
    }

    /**
     * ID registrace
     * 
     * @return ID
     */
    public Long getID() {
        return this.id;
    }

    /**
     * ID registrovaneho tymu
     * 
     * @return ID tymu
     */
    public Long getTeamID() {
        return this.team.getID();
    }

    /**
     * Jmeno registrovaneho tymu
     * 
     * @return Jmeno tymu
     */
    public String getTeamName() {
        return this.team.getName();
    }

    /**
     * Navrati registrovany tym
     * 
     * @return Tym
     */
    @JsonIgnore
    public Team getTeam() {
        return this.team;
    }

    /**
     * Navrati rocnik souteze, do ktere se tym registuruje
     * 
     * @return Rocnik souteze
     */
    public int getCompetitionYear() {
        return this.competition.getYear();
    }

    /**
     * Navrati souteze, ve ktere je tym registrovany
     * 
     * @return Rocnik souteze
     */
    @JsonIgnore
    public Competition getCompetition() {
        return this.competition;
    }

    /**
     * Kategorie, ve ktere tym bude soutezit
     * 
     * @return Kategorie
     */
    public ECategory getCategory() {
        return this.category.getName();
    }

    /**
     * Navrati seznam vsech robotu, kteri jsou vytvoreni na tuto registraci tymu
     * 
     * @return Seznam vsech vytvorenych robotu
     */
    @JsonIgnore
    public List<Robot> getRobots() {
        return this.robots;
    }

    /**
     * Nastavi kategorii, ve ktere bude tym soutezit
     * 
     * @param _category Kategorie
     */
    public void setCategory(Category _category) {
        this.category = _category;
    }

     public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherSurname() {
        return teacherSurname;
    }

    public void setTeacherSurname(String teacherSurname) {
        this.teacherSurname = teacherSurname;
    }

    public String getTeacherContact() {
        return teacherContact;
    }

    public void setTeacherContact(String teacherContact) {
        this.teacherContact = teacherContact;
    }

}
