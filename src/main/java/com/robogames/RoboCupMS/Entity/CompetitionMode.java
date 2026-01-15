package com.robogames.RoboCupMS.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robogames.RoboCupMS.Business.Enum.ECompetitionMode;

/**
 * Entity representing competition modes (TOURNAMENT, BEST_SCORE)
 */
@Entity(name = "competition_mode")
public class CompetitionMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ECompetitionMode name;

    /**
     * Default constructor
     */
    public CompetitionMode() {
    }

    /**
     * Constructor with mode name
     * 
     * @param name The competition mode name
     */
    public CompetitionMode(ECompetitionMode name) {
        this.name = name;
    }

    /**
     * Get the ID of this competition mode
     * 
     * @return The ID
     */
    @JsonProperty("id")
    public Long getID() {
        return id;
    }

    /**
     * Get the name of this competition mode
     * 
     * @return The mode name
     */
    public ECompetitionMode getName() {
        return name;
    }

    /**
     * Set the name of this competition mode
     * 
     * @param name The new mode name
     */
    public void setName(ECompetitionMode name) {
        this.name = name;
    }

}
