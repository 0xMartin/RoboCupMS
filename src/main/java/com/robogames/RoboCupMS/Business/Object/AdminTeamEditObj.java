package com.robogames.RoboCupMS.Business.Object;

/**
 * Objekt pro editaci tymu adminem
 */
public class AdminTeamEditObj {

    /**
     * Nove jmeno tymu (null pokud nechceme menit)
     */
    private String name;

    /**
     * ID noveho vedouciho tymu (null pokud nechceme menit)
     */
    private Long leaderId;

    public AdminTeamEditObj() {
    }

    public AdminTeamEditObj(String name, Long leaderId) {
        this.name = name;
        this.leaderId = leaderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }
}
