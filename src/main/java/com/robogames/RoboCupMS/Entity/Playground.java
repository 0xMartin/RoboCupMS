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

/**
 * Hriste, na kterem roboti uskutecnuji zapasy mezi sebou
 */
@Entity(name = "playground")
public class Playground {

    /**
     * ID robota
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nazev hriste
     */
    @Column(name = "name", length = 40, nullable = false, unique = false)
    private String name;

    /**
     * Cislo hriste (melo by byt unikatni v ramci rocniku souteze)
     */
    @Column(name = "number", nullable = false, unique = false)
    private int number;

    /**
     * Pro kterou disciplinu je hriste urcene
     */
    @ManyToOne(optional = false)
    private Discipline discipline;

    /**
     * Vsechny zapasy odehrane na tomto hristi
     */
    @OneToMany(mappedBy = "playground", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<RobotMatch> matches;

    /**
     * Vytvori soutezni hriste. V systemu urcitovani poradi pro jednoznacne urceni
     * konani mista zapasu.
     */
    public Playground() {
        this.matches = new ArrayList<RobotMatch>();
    }

    /**
     * Vytvori soutezni hriste. V systemu urcitovani poradi pro jednoznacne urceni
     * konani mista zapasu.
     * 
     * @param _name       Jmeno hriste
     * @param _number     Cislo hriste
     * @param _discipline Disciplina, pro kterou je hriste urcene
     */
    public Playground(String _name, int _number, Discipline _discipline) {
        this.name = _name;
        this.number = _number;
        this.discipline = _discipline;
        this.matches = new ArrayList<RobotMatch>();
    }

    /**
     * Navrati ID hriste
     * 
     * @return ID hriste
     */
    public long getID() {
        return this.id;
    }

    /**
     * Navrati jmeno hriste
     * 
     * @return Jmeno hriste
     */
    public String getName() {
        return this.name;
    }

    /**
     * Navrati cislo hriste
     * 
     * @return Cislo hriste
     */
    public int getNumber() {
        return this.number;
    }

    /**
     * Navrati ID discipliny, pro kterou je toto hriste urcine
     * 
     * @return ID discipliny
     */
    public long getDisciplineID() {
        return this.discipline.getID();
    }

    /**
     * Navrati nazev discipliny, pro kterou je toto hriste urcine
     * 
     * @return Nazev discipliny
     */
    public String getDisciplineName() {
        return this.discipline.getName();
    }

    /**
     * Navrati vsechny odehrane zapasy na tomto hristi
     * 
     * @return Seznam zapasu
     */
    @JsonIgnore
    public List<RobotMatch> getMatches() {
        return this.matches;
    }

    /**
     * Nastavi nove jmeno hriste
     * 
     * @param _name Nove jmeno hriste
     */
    public void setName(String _name) {
        this.name = _name;
    }

    /**
     * Nastavi nove cislo hriste
     * 
     * @param _number Nove cislo hriste
     */
    public void setNumber(int _number) {
        this.number = _number;
    }

    /**
     * Nastavi disciplinu hriste
     * 
     * @param _discipline Disciplina
     */
    public void setDiscipline(Discipline _discipline) {
        this.discipline = _discipline;
    }

}
