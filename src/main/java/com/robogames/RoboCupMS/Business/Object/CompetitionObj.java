package com.robogames.RoboCupMS.Business.Object;

import java.sql.Time;
import java.util.Date;

public class CompetitionObj {

    /**
     * Rok konani souteze
     */
    private int year;

    /**
     * Datum konani souteze
     */
    private Date date;

    /**
     * Cas zahajeni souteze
     */
    private Time startTime;

    /**
     * Cas ukonceni souteze
     */
    private Time endTime;

    /**
     * Urcuje zda zobrazit globalni vysledky
     */
    private Boolean showResults;

    public CompetitionObj() {
    }

    public CompetitionObj(int year, Date date, Time startTime, Time endTime) {
        this.year = year;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public Boolean getShowResults() {
        return this.showResults;
    }

    public void setShowResults(Boolean showResults) {
        this.showResults = showResults;
    }

}
