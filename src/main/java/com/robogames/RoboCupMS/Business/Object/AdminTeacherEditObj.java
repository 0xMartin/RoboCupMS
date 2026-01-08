package com.robogames.RoboCupMS.Business.Object;

/**
 * Objekt pro editaci udaju o uciteli (zodpovedne osobe) v registraci tymu
 */
public class AdminTeacherEditObj {

    /**
     * Jmeno ucitele (zodpovedne osoby)
     */
    private String teacherName;

    /**
     * Prijmeni ucitele (zodpovedne osoby)
     */
    private String teacherSurname;

    /**
     * Kontakt na ucitele (zodpovedne osoby)
     */
    private String teacherContact;

    public AdminTeacherEditObj() {
    }

    public AdminTeacherEditObj(String teacherName, String teacherSurname, String teacherContact) {
        this.teacherName = teacherName;
        this.teacherSurname = teacherSurname;
        this.teacherContact = teacherContact;
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
