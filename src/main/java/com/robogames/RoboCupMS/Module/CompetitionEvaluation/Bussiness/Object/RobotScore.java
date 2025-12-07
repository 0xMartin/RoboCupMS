package com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Entity.Robot;

/**
 * Uklada celkove dosazene skore robota v soutezi
 */
public class RobotScore {

    private final Robot robot;

    private final float score;

    /**
     * Uklada celkove dosazene skore robota v soutezi
     * 
     * @param _robot Robot
     * @param _score Celkove score
     */
    public RobotScore(Robot _robot, float _score) {
        this.robot = _robot;
        this.score = _score;
    }

    /**
     * Navrati robota
     * 
     * @return Robot
     */
    @JsonIgnore
    public Robot getRobot() {
        return this.robot;
    }

    /**
     * Navrati ID robota
     * 
     * @return Robot
     */
    public long getRobotID() {
        if (this.robot == null) {
            return -1;
        }
        return this.robot.getID();
    }

    /**
     * Navrati jmeno robota
     * 
     * @return Jmeno robota
     */
    public String getRobotName() {
        if (this.robot == null) {
            return "";
        }
        return this.robot.getName();
    }

    /**
     * Navrati ID discipliny, ve ktere robot soutezi
     * 
     * @return ID discipliny
     */
    public long getDisciplindeID() {
        return this.robot.getDisciplineID();
    }

    /**
     * Navrati nazev discipliny, ve ktere robot soutezi
     * 
     * @return Nazev discipliny
     */
    public String getDisciplindeName() {
        return this.robot.getDiciplineName();
    }

    /**
     * Navrati nazev kategorie, ve ktere robot soutezi
     * 
     * @return Soutezni kategorie
     */
    public ECategory getCategory() {
        return this.robot.getCategory();
    }

    /**
     * Navrati id tymu, ktery vlastni robota
     * @return ID tymu
     */
    public long getTeamID() {
        return this.robot.getTeamRegistration().getTeamID();
    }

    /**
     * Navrati jmeno tymu, ktery vlastni robota
     * @return Jmeno tymu
     */
    public String getTeamName() {
        return this.robot.getTeamName();
    }

    /**
     * Navrati celkove skore
     * 
     * @return Celkove skore
     */
    public float getScore() {
        return this.score;
    }

}
