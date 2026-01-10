package com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Object;

import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Entity.RobotMatch;

/**
 * DTO objekt pro zobrazeni naplanovaneho zapasu na verejne strance
 */
public class ScheduledMatchInfo {

    private long matchId;
    private long robotId;
    private long robotNumber;
    private String robotName;
    private long teamId;
    private String teamName;
    private long disciplineId;
    private String disciplineName;
    private ECategory category;
    private long playgroundId;
    private int playgroundNumber;
    private String playgroundName;
    private EMatchState matchState;
    private long groupId;

    public ScheduledMatchInfo() {
    }

    public ScheduledMatchInfo(RobotMatch match) {
        this.matchId = match.getID();
        this.robotId = match.getRobotID();
        this.robotNumber = match.getRobotNumber();
        this.robotName = match.getRobotName();
        this.teamId = match.getRobot().getTeamRegistration().getTeamID();
        this.teamName = match.getRobot().getTeamRegistration().getTeam().getName();
        this.disciplineId = match.getRobot().getDiscipline().getID();
        this.disciplineName = match.getRobot().getDiscipline().getName();
        this.category = match.getRobot().getCategory();
        this.playgroundId = match.getPlaygroundID();
        this.playgroundNumber = match.getPlayground().getNumber();
        this.playgroundName = match.getPlayground().getName();
        this.matchState = match.getState().getName();
        this.groupId = match.getGroupID();
    }

    public long getMatchId() {
        return matchId;
    }

    public long getRobotId() {
        return robotId;
    }

    public long getRobotNumber() {
        return robotNumber;
    }

    public String getRobotName() {
        return robotName;
    }

    public long getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
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

    public long getGroupId() {
        return groupId;
    }
}
