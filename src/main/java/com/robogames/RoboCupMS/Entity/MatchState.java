package com.robogames.RoboCupMS.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;

/**
 * Entita reprezentujici mozne stavy zapasu (Enum)
 */
@Entity(name = "match_state")
public class MatchState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EMatchState name;

    /**
     * Entita reprezentujici mozne stav zapasu
     */
    public MatchState() {
    }

    /**
     * Entita reprezentujici mozne stav zapasu
     * 
     * @param name Nazev stavu zapasu
     */
    public MatchState(EMatchState name) {
        this.name = name;
    }

    /**
     * Navrati ID stavu zapasu
     * 
     * @return ID stavu zapasu
     */
    @JsonProperty("id")
    public Long getID() {
        return id;
    }

    /**
     * Navrati nazev stavu zapasu
     * 
     * @return Nazev stavu
     */
    public EMatchState getName() {
        return name;
    }

    /**
     * Nastavi nazev stavu zapasu
     * 
     * @param name Novy nazes stavu
     */
    public void setName(EMatchState name) {
        this.name = name;
    }

}
