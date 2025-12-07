package com.robogames.RoboCupMS.Business.Object;

public class UserNameObj {

    private long id;
    private String name;
    private String surname;

    public UserNameObj() {
    }

    public UserNameObj(long id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
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

}
