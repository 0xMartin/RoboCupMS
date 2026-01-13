package com.robogames.RoboCupMS.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;

/**
 * Entity representing tournament phases (preliminary, semifinal, final, etc.)
 */
@Entity(name = "tournament_phase")
public class TournamentPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ETournamentPhase name;

    /**
     * Default constructor
     */
    public TournamentPhase() {
    }

    /**
     * Constructor with phase name
     * 
     * @param name The tournament phase name
     */
    public TournamentPhase(ETournamentPhase name) {
        this.name = name;
    }

    /**
     * Get the ID of this tournament phase
     * 
     * @return The ID
     */
    @JsonProperty("id")
    public Long getID() {
        return id;
    }

    /**
     * Get the name of this tournament phase
     * 
     * @return The phase name
     */
    public ETournamentPhase getName() {
        return name;
    }

    /**
     * Set the name of this tournament phase
     * 
     * @param name The new phase name
     */
    public void setName(ETournamentPhase name) {
        this.name = name;
    }

}
