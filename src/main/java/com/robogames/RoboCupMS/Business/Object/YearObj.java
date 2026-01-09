package com.robogames.RoboCupMS.Business.Object;

public class YearObj {
    
    private long id;

    private int year;

    private int robotCount;


    public YearObj() {
    }

    public YearObj(long id, int year) {
        this.id = id;
        this.year = year;
        this.robotCount = 0;
    }

    public YearObj(long id, int year, int robotCount) {
        this.id = id;
        this.year = year;
        this.robotCount = robotCount;
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

    public int getRobotCount() {
        return this.robotCount;
    }

    public void setRobotCount(int robotCount) {
        this.robotCount = robotCount;
    }

}
