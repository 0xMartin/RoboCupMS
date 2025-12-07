package com.robogames.RoboCupMS.Business.Object;

public class TeamRegistrationObj {

    /**
     * Rocnik souteze, do ktereho se tym registruje
     */
    private int year;

    public TeamRegistrationObj() {
    }

    public TeamRegistrationObj(int year, boolean open) {
        this.year = year;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}
