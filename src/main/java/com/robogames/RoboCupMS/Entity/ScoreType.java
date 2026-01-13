package com.robogames.RoboCupMS.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robogames.RoboCupMS.Business.Enum.EScoreType;

/**
 * Entity representing score types (time, points, etc.)
 */
@Entity(name = "score_type")
public class ScoreType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EScoreType name;

    /**
     * Default constructor
     */
    public ScoreType() {
    }

    /**
     * Constructor with score type name
     * 
     * @param name The score type name
     */
    public ScoreType(EScoreType name) {
        this.name = name;
    }

    /**
     * Get the ID of this score type
     * 
     * @return The ID
     */
    @JsonProperty("id")
    public Long getID() {
        return id;
    }

    /**
     * Get the name of this score type
     * 
     * @return The score type name
     */
    public EScoreType getName() {
        return name;
    }

    /**
     * Set the name of this score type
     * 
     * @param name The new score type name
     */
    public void setName(EScoreType name) {
        this.name = name;
    }

}
