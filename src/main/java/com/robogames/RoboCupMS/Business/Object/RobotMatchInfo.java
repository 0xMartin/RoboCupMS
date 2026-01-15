package com.robogames.RoboCupMS.Business.Object;

import java.time.LocalDateTime;

import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Enum.EScoreType;
import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;
import com.robogames.RoboCupMS.Entity.RobotMatch;

/**
 * DTO object for displaying robot match info
 */
public class RobotMatchInfo {

    private long id;
    private Float scoreA;
    private Float scoreB;
    private EMatchState stateName;
    private String playgroundName;
    private int playgroundNumber;
    private ETournamentPhase phaseName;
    private EScoreType scoreTypeName;
    
    // Robot A info
    private Long robotAID;
    private Long robotANumber;
    private String robotAName;
    private String teamAName;
    private ECategory robotACategory;
    
    // Robot B info (null for single-robot matches)
    private Long robotBID;
    private Long robotBNumber;
    private String robotBName;
    private String teamBName;
    private ECategory robotBCategory;
    
    private boolean twoRobotMatch;
    private LocalDateTime timestamp;
    private String group;
    private Integer visualX;
    private Integer visualY;
    private Long disciplineId;
    
    public RobotMatchInfo() {
    }

    public RobotMatchInfo(RobotMatch match) {
        this.id = match.getID();
        this.scoreA = match.getScoreA();
        this.scoreB = match.getScoreB();
        this.stateName = match.getState().getName();
        
        if (match.getPlayground() != null) {
            this.playgroundName = match.getPlayground().getName();
            this.playgroundNumber = match.getPlayground().getNumber();
        }
        
        if (match.getPhase() != null) {
            this.phaseName = match.getPhase().getName();
        }
        
        if (match.getScoreType() != null) {
            this.scoreTypeName = match.getScoreType().getName();
        }
        
        if (match.getRobotA() != null) {
            this.robotAID = match.getRobotA().getID();
            this.robotANumber = match.getRobotA().getNumber();
            this.robotAName = match.getRobotA().getName();
            this.teamAName = match.getRobotA().getTeamRegistration().getTeam().getName();
            this.robotACategory = match.getRobotA().getCategory();
        }
        
        if (match.getRobotB() != null) {
            this.robotBID = match.getRobotB().getID();
            this.robotBNumber = match.getRobotB().getNumber();
            this.robotBName = match.getRobotB().getName();
            this.teamBName = match.getRobotB().getTeamRegistration().getTeam().getName();
            this.robotBCategory = match.getRobotB().getCategory();
        }
        
        this.twoRobotMatch = match.getRobotB() != null;
        this.timestamp = match.getTimestamp();
        this.group = match.getGroup();
        this.visualX = match.getVisualX();
        this.visualY = match.getVisualY();
        
        if (match.getPlayground() != null && match.getPlayground().getDiscipline() != null) {
            this.disciplineId = match.getPlayground().getDiscipline().getID();
        }
    }

    public long getId() {
        return id;
    }

    public Float getScoreA() {
        return scoreA;
    }

    public Float getScoreB() {
        return scoreB;
    }

    public EMatchState getStateName() {
        return stateName;
    }

    public String getPlaygroundName() {
        return playgroundName;
    }

    public int getPlaygroundNumber() {
        return playgroundNumber;
    }

    public ETournamentPhase getPhaseName() {
        return phaseName;
    }

    public EScoreType getScoreTypeName() {
        return scoreTypeName;
    }

    public Long getRobotAID() {
        return robotAID;
    }

    public Long getRobotANumber() {
        return robotANumber;
    }

    public String getRobotAName() {
        return robotAName;
    }

    public String getTeamAName() {
        return teamAName;
    }

    public Long getRobotBID() {
        return robotBID;
    }

    public Long getRobotBNumber() {
        return robotBNumber;
    }

    public String getRobotBName() {
        return robotBName;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public ECategory getRobotACategory() {
        return robotACategory;
    }

    public ECategory getRobotBCategory() {
        return robotBCategory;
    }

    public boolean isTwoRobotMatch() {
        return twoRobotMatch;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getGroup() {
        return group;
    }

    public Integer getVisualX() {
        return visualX;
    }

    public Integer getVisualY() {
        return visualY;
    }

    public Long getDisciplineId() {
        return disciplineId;
    }
}
