package com.robogames.RoboCupMS.Business.Object;

/**
 * Objekt pro registraci tymu do souteze adminem
 */
public class AdminTeamRegistrationObj {

    /**
     * ID tymu, ktery se registruje
     */
    private Long teamId;

    /**
     * Rocnik souteze, do ktereho se tym registruje
     */
    private int year;

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

    public AdminTeamRegistrationObj() {
    }

    public AdminTeamRegistrationObj(Long teamId, int year, String teacherName, String teacherSurname, String teacherContact) {
        this.teamId = teamId;
        this.year = year;
        this.teacherName = teacherName;
        this.teacherSurname = teacherSurname;
        this.teacherContact = teacherContact;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public int getYear() {
        return year;
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
