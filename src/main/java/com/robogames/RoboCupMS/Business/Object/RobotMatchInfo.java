package com.robogames.RoboCupMS.Business.Object;

import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Entity.RobotMatch;

/**
 * DTO objekt pro zobrazeni zapasu robota
 */
public class RobotMatchInfo {

    private long matchId;
    private float score;
    private EMatchState state;
    private String playgroundName;
    private int playgroundNumber;
    private Long groupId;
    
    public RobotMatchInfo() {
    }

    public RobotMatchInfo(RobotMatch match) {
        this.matchId = match.getID();
        this.score = match.getScore();
        this.state = match.getState().getName();
        this.playgroundName = match.getPlayground().getName();
        this.playgroundNumber = match.getPlayground().getNumber();
        this.groupId = match.getGroupID();
    }

    public long getMatchId() {
        return matchId;
    }

    public float getScore() {
        return score;
    }

    public EMatchState getState() {
        return state;
    }

    public String getPlaygroundName() {
        return playgroundName;
    }

    public int getPlaygroundNumber() {
        return playgroundNumber;
    }

    public Long getGroupId() {
        return groupId;
    }
}
