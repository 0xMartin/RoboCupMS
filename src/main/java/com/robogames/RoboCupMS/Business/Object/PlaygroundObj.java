package com.robogames.RoboCupMS.Business.Object;

public class PlaygroundObj {

    /**
     * Nazev hriste
     */
    private String name;

    /**
     * Cislo hriste (pro jednodussi indentifikaci)
     */
    private int number;

    /**
     * ID discipliny, pro kterou je hriste urcene
     */
    private Long disciplineID;

    public PlaygroundObj() {
    }

    public PlaygroundObj(String name, int number, Long disciplineID) {
        this.name = name;
        this.number = number;
        this.disciplineID = disciplineID;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Long getDisciplineID() {
        return this.disciplineID;
    }

    public void setDisciplineID(Long disciplineID) {
        this.disciplineID = disciplineID;
    }

}
