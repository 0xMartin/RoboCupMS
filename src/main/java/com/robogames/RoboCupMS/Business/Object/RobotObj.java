package com.robogames.RoboCupMS.Business.Object;

public class RobotObj {

    /**
     * Jmeno robota
     */
    private String name;

    public RobotObj() {
    }

    public RobotObj(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
