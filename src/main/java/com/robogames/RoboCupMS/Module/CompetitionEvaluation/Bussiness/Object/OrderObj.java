package com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Object;

public class OrderObj {

    private final int place;

    private final RobotScore score;

    public OrderObj(int _place, RobotScore _score) {
        this.place = _place;
        this.score = _score;
    }

    /**
     * Navrati umisteni
     * 
     * @return Umisteni robota
     */
    public int getPlace() {
        return this.place;
    }

    /**
     * Navrati RobotScore
     * 
     * @return RobotScore
     */
    public RobotScore getData() {
        return this.score;
    }

}
