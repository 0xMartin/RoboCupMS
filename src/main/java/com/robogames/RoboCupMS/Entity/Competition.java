package com.robogames.RoboCupMS.Entity;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "competition")
public class Competition {

    /**
     * ID tymu
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Rocnik souteze
     */
    @Column(name = "year", nullable = false, unique = true)
    private int year;

    /**
     * Datum souteze
     */
    @Column(name = "date", nullable = false, unique = false)
    private Date date;

    /**
     * Zacatek souteze
     */
    @Column(name = "start_time", nullable = false, unique = false)
    private Time startTime;

    /**
     * Konec souteze
     */
    @Column(name = "end_time", nullable = false, unique = false)
    private Time endTime;

    /**
     * Info o stavu souteze (zacala/nezacala)
     */
    @Column(name = "started", nullable = false, unique = false)
    private Boolean started;

    /**
     * Urcuje zda zobrazit globalni vysledky (1., 2., 3. misto) na hlavni strance
     */
    @Column(name = "show_results", nullable = false, unique = false)
    private Boolean showResults;

    /**
     * Registrace tymu do tohoto rocniku souteze
     */
    @OneToMany(mappedBy = "competition", fetch = FetchType.EAGER)
    private List<TeamRegistration> registrations;

    /**
     * Vytvori instanci rocniku souteze. Do souteze se muzi registrovat tymy, ktere
     * chteji v tomto danem rocniku soutezit.
     */
    public Competition() {
        this.registrations = new ArrayList<TeamRegistration>();
    }

    /**
     * Vytvori instanci rocniku souteze. Do souteze se muzi registrovat tymy, ktere
     * chteji v tomto danem rocniku soutezit.
     * 
     * @param _year  Rocnik souteze
     * @param _date  Datum konani souteze
     * @param _start Cas zacatku souteze
     * @param _end   Cas ukonceni souteze
     */
    public Competition(int _year, Date _date, Time _start, Time _end) {
        this.year = _year;
        this.date = _date;
        this.startTime = _start;
        this.endTime = _end;
        this.started = false;
        this.showResults = false;
        this.registrations = new ArrayList<TeamRegistration>();
    }

    /**
     * Navrati ID souteze
     * 
     * @return ID souteze
     */
    public Long getID() {
        return this.id;
    }

    /**
     * Navrati rocnik souteze
     * 
     * @return Rocnik souteze
     */
    public int getYear() {
        return this.year;
    }

    /**
     * Narati datum konani souteze
     * 
     * @return Datum konani souteze
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Navrati cas zacatku souteze
     * 
     * @return Cas zacatku souteze
     */
    public Time getStartTime() {
        return this.startTime;
    }

    /**
     * Navrati cas konce souteze
     * 
     * @return Cas konce souteze
     */
    public Time getEndTime() {
        return this.endTime;
    }

    /**
     * Navrati stav souteze (zacala/nazacala)
     * 
     * @return Stav souteze
     */
    public Boolean getStarted() {
        return this.started;
    }

    /**
     * Nastavi rocnik
     * 
     * @param _year Rocnik souteze
     */
    public void setYear(int _year) {
        this.year = _year;
    }

    /**
     * Nastavi datum konani
     * 
     * @param _date Datum konani souteze
     */
    public void setDate(Date _date) {
        this.date = _date;
    }

    /**
     * Nastavi cas zacatku souteze
     * 
     * @param _time Cas zacatku souteze
     */
    public void setStartTime(Time _time) {
        this.startTime = _time;
    }

    /**
     * Nastavi cas konce souteze
     * 
     * @param _time Cas konce souteze
     */
    public void setEndTime(Time _time) {
        this.endTime = _time;
    }

    /**
     * Nastavi stav souteze (zacala/nezacala)
     * 
     * @param s Stav
     */
    public void setStarted(Boolean s) {
        this.started = s;
    }

    /**zda jsou globalni vysledky zobrazeny
     * 
     * @return True pokud jsou vysledky zobrazeny
     */
    public Boolean getShowResults() {
        return this.showResults;
    }

    /**
     * Nastavi zda zobrazit globalni vysledky
     * 
     * @param showResults True pro zobrazeni vysledku
     */
    public void setShowResults(Boolean showResults) {
        this.showResults = showResults;
    }

    /**
     * Navrati 
     * Navrati vsechny registrace tymu do tohoto rocniku souteze
     * 
     * @return Seznam vsech registraci
     */
    @JsonIgnore
    public List<TeamRegistration> getRegistrations() {
        return this.registrations;
    }

}
