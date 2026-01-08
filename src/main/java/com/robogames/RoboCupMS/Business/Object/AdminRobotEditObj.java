package com.robogames.RoboCupMS.Business.Object;

/**
 * Objekt pro editaci robota adminem
 */
public class AdminRobotEditObj {

    /**
     * Nove jmeno robota (null pokud nechceme menit)
     */
    private String name;

    /**
     * Nove cislo robota (null pokud nechceme menit)
     */
    private Long number;

    /**
     * ID nove discipliny (null pokud nechceme menit, -1 pro zruseni registrace do discipliny)
     */
    private Long disciplineId;

    /**
     * Novy stav potvrzeni registrace (null pokud nechceme menit)
     */
    private Boolean confirmed;

    public AdminRobotEditObj() {
    }

    public AdminRobotEditObj(String name, Long number, Long disciplineId, Boolean confirmed) {
        this.name = name;
        this.number = number;
        this.disciplineId = disciplineId;
        this.confirmed = confirmed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Long getDisciplineId() {
        return disciplineId;
    }

    public void setDisciplineId(Long disciplineId) {
        this.disciplineId = disciplineId;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }
}
