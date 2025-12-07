package com.robogames.RoboCupMS.Business.Object;

import java.util.Date;

public class UserEditObj {

    /**
     * Nove jmeno uzivatele
     */
    private String name;

    /**
     * Nove prijmeni uzivatele
     */
    private String surname;

    /**
     * Novy datum narozeni
     */
    private Date birthDate;

    public UserEditObj() {
    }

    public UserEditObj(String name, String surname, Date birthDate) {
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return this.surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Date getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

}
