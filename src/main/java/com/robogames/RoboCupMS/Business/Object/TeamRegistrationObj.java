package com.robogames.RoboCupMS.Business.Object;

public class TeamRegistrationObj {

    /**
     * Rocnik souteze, do ktereho se tym registruje
     */
    private int year;

    /**
     * Jmeno ucitele
     */
    private String teacherName;

    /**
     * Prijmeni ucitele
     */
    private String teacherSurname;

    /**
     * Kontakt na ucitele
     */
    private String teacherContact;

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

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherSurname() {
        return teacherSurname;
    }

    public void setTeacherSurname(String teacherSurname) {
        this.teacherSurname = teacherSurname;
    }

    public String getTeacherContact() {
        return teacherContact;
    }

    public void setTeacherContact(String teacherContact) {
        this.teacherContact = teacherContact;
    }

}
