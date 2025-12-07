package com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Object;

public class MultiMatchGroupObj {

    /**
     * Rocnik souteze
     */
    private int year;

    /**
     * Seznam ID vsech robotu
     */
    private Long[] robots;

    /**
     * Hriste, na kterem se budou vsechny zapasy hrat
     */
    private Long playgroundID;

    public MultiMatchGroupObj() {
    }

    public MultiMatchGroupObj(int year, Long[] robots, Long playgroundID) {
        this.year = year;
        this.robots = robots;
        this.playgroundID = playgroundID;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Long[] getRobots() {
        return this.robots;
    }

    public void setRobots(Long[] robots) {
        this.robots = robots;
    }

    public Long getPlaygroundID() {
        return this.playgroundID;
    }

    public void setPlaygroundID(Long playgroundID) {
        this.playgroundID = playgroundID;
    }

}
