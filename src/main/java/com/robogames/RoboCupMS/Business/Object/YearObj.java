package com.robogames.RoboCupMS.Business.Object;

public class YearObj {
    
    private long id;

    private int year;


    public YearObj() {
    }

    public YearObj(long id, int year) {
        this.id = id;
        this.year = year;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}
