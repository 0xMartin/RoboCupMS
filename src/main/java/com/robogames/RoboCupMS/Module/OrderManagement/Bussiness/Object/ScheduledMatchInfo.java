package com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Object;

import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;
import com.robogames.RoboCupMS.Entity.RobotMatch;

/**
 * DTO object for displaying scheduled match on public page
 * Supports both single-robot matches and two-robot matches
 */
public class ScheduledMatchInfo {

    private long matchId;
    
    // Robot A info
    private Long robotAId;
    private Long robotANumber;
    private String robotAName;
    private Long teamAId;
    private String teamAName;
    
    // Robot B info (null for single-robot matches)
    private Long robotBId;
    private Long robotBNumber;
    private String robotBName;
    private Long teamBId;
    private String teamBName;
    
    // Match info
    private long disciplineId;
    private String disciplineName;
    private ECategory category;
    private long playgroundId;
    private int playgroundNumber;
    private String playgroundName;
    private EMatchState matchState;
    private ETournamentPhase phase;
    private boolean isTwoRobotMatch;

    public ScheduledMatchInfo() {
    }

    public ScheduledMatchInfo(RobotMatch match) {
        this.matchId = match.getID();
        
        // Robot A
        if (match.getRobotA() != null) {
            this.robotAId = match.getRobotA().getID();
            this.robotANumber = match.getRobotA().getNumber();
            this.robotAName = match.getRobotA().getName();
            this.teamAId = match.getRobotA().getTeamRegistration().getTeamID();
            this.teamAName = match.getRobotA().getTeamRegistration().getTeam().getName();
            
            // Get discipline and category from robot A
            this.disciplineId = match.getRobotA().getDiscipline().getID();
            this.disciplineName = match.getRobotA().getDiscipline().getName();
            this.category = match.getRobotA().getCategory();
        }
        
        // Robot B (for two-robot matches)
        if (match.getRobotB() != null) {
            this.robotBId = match.getRobotB().getID();
            this.robotBNumber = match.getRobotB().getNumber();
            this.robotBName = match.getRobotB().getName();
            this.teamBId = match.getRobotB().getTeamRegistration().getTeamID();
            this.teamBName = match.getRobotB().getTeamRegistration().getTeam().getName();
        }
        
        this.isTwoRobotMatch = match.getRobotB() != null;
        
        // Playground
        if (match.getPlayground() != null) {
            this.playgroundId = match.getPlayground().getID();
            this.playgroundNumber = match.getPlayground().getNumber();
            this.playgroundName = match.getPlayground().getName();
        }
        
        this.matchState = match.getState().getName();
        
        // Phase
        if (match.getPhase() != null) {
            this.phase = match.getPhase().getName();
        }
    }

    public long getMatchId() {
        return matchId;
    }

    public Long getRobotAId() {
        return robotAId;
    }

    public Long getRobotANumber() {
        return robotANumber;
    }

    public String getRobotAName() {
        return robotAName;
    }

    public Long getTeamAId() {
        return teamAId;
    }

    public String getTeamAName() {
        return teamAName;
    }

    public Long getRobotBId() {
        return robotBId;
    }

    public Long getRobotBNumber() {
        return robotBNumber;
    }

    public String getRobotBName() {
        return robotBName;
    }

    public Long getTeamBId() {
        return teamBId;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public long getDisciplineId() {
        return disciplineId;
    }

    public String getDisciplineName() {
        return disciplineName;
    }

    public ECategory getCategory() {
        return category;
    }

    public long getPlaygroundId() {
        return playgroundId;
    }

    public int getPlaygroundNumber() {
        return playgroundNumber;
    }

    public String getPlaygroundName() {
        return playgroundName;
    }

    public EMatchState getMatchState() {
        return matchState;
    }

    public ETournamentPhase getPhase() {
        return phase;
    }

    public boolean isTwoRobotMatch() {
        return isTwoRobotMatch;
    }
}
