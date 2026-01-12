package com.robogames.RoboCupMS.Business.Object;

import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;
import com.robogames.RoboCupMS.Entity.RobotMatch;

/**
 * DTO object for displaying robot match info
 */
public class RobotMatchInfo {

    private long matchId;
    private Float scoreA;
    private Float scoreB;
    private EMatchState state;
    private String playgroundName;
    private int playgroundNumber;
    private ETournamentPhase phase;
    
    // Robot A info
    private Long robotAId;
    private String robotAName;
    
    // Robot B info (null for single-robot matches)
    private Long robotBId;
    private String robotBName;
    
    private boolean isTwoRobotMatch;
    
    public RobotMatchInfo() {
    }

    public RobotMatchInfo(RobotMatch match) {
        this.matchId = match.getID();
        this.scoreA = match.getScoreA();
        this.scoreB = match.getScoreB();
        this.state = match.getState().getName();
        
        if (match.getPlayground() != null) {
            this.playgroundName = match.getPlayground().getName();
            this.playgroundNumber = match.getPlayground().getNumber();
        }
        
        if (match.getPhase() != null) {
            this.phase = match.getPhase().getName();
        }
        
        if (match.getRobotA() != null) {
            this.robotAId = match.getRobotA().getID();
            this.robotAName = match.getRobotA().getName();
        }
        
        if (match.getRobotB() != null) {
            this.robotBId = match.getRobotB().getID();
            this.robotBName = match.getRobotB().getName();
        }
        
        this.isTwoRobotMatch = match.getRobotB() != null;
    }

    public long getMatchId() {
        return matchId;
    }

    public Float getScoreA() {
        return scoreA;
    }

    public Float getScoreB() {
        return scoreB;
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

    public ETournamentPhase getPhase() {
        return phase;
    }

    public Long getRobotAId() {
        return robotAId;
    }

    public String getRobotAName() {
        return robotAName;
    }

    public Long getRobotBId() {
        return robotBId;
    }

    public String getRobotBName() {
        return robotBName;
    }

    public boolean isTwoRobotMatch() {
        return isTwoRobotMatch;
    }
}
