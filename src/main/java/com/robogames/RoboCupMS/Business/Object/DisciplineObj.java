package com.robogames.RoboCupMS.Business.Object;

import com.robogames.RoboCupMS.Business.Enum.EScoreAggregation;
import com.robogames.RoboCupMS.Business.Enum.EScoreType;

public class DisciplineObj {
    
    /**
     * Nazev discipliny
     */
    private String name;

    /**
     * Popis discipliny
     */
    private String description;

    /**
     * Agregacni funkce skore (pro automatizovane vyhodnoceni) [MIN, MAX, SUM]
     */
    private EScoreAggregation scoreAggregation;

    /**
     * Casovy limit na jeden zapas pro tuto disciplinu
     */
    private int time;

    /**
     * Maximalni pocet zapasu, ktere robotu muze v teto discipline odehrat
     */
    private int maxRounds;

    /**
     * Score type for this discipline (TIME or SCORE)
     */
    private EScoreType scoreType;

    /**
     * Determines how to select the winner based on score
     * true = higher score wins (e.g., sumo wins count)
     * false = lower score wins (e.g., time-based like line follower)
     */
    private Boolean highScoreWin;


    public DisciplineObj() {
    }

    public DisciplineObj(String name, String description, EScoreAggregation scoreAggregation, int time, int maxRounds) {
        this.name = name;
        this.description = description;
        this.scoreAggregation = scoreAggregation;
        this.time = time;
        this.maxRounds = maxRounds;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EScoreAggregation getScoreAggregation() {
        return this.scoreAggregation;
    }

    public void setScoreAggregation(EScoreAggregation scoreAggregation) {
        this.scoreAggregation = scoreAggregation;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getMaxRounds() {
        return this.maxRounds;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    public EScoreType getScoreType() {
        return this.scoreType;
    }

    public void setScoreType(EScoreType scoreType) {
        this.scoreType = scoreType;
    }

    public Boolean getHighScoreWin() {
        return this.highScoreWin;
    }

    public void setHighScoreWin(Boolean highScoreWin) {
        this.highScoreWin = highScoreWin;
    }

}
