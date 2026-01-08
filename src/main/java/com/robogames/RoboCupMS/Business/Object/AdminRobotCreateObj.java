package com.robogames.RoboCupMS.Business.Object;

/**
 * Objekt pro vytvoreni robota adminem
 */
public class AdminRobotCreateObj {

    /**
     * ID registrace tymu, na kterou se robot vytvori
     */
    private Long teamRegistrationId;

    /**
     * Jmeno robota
     */
    private String name;

    /**
     * ID discipliny, do ktere bude robot registrovan (nepovinne)
     */
    private Long disciplineId;

    public AdminRobotCreateObj() {
    }

    public AdminRobotCreateObj(Long teamRegistrationId, String name, Long disciplineId) {
        this.teamRegistrationId = teamRegistrationId;
        this.name = name;
        this.disciplineId = disciplineId;
    }

    public Long getTeamRegistrationId() {
        return teamRegistrationId;
    }

    public void setTeamRegistrationId(Long teamRegistrationId) {
        this.teamRegistrationId = teamRegistrationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDisciplineId() {
        return disciplineId;
    }

    public void setDisciplineId(Long disciplineId) {
        this.disciplineId = disciplineId;
    }
}
