package com.robogames.RoboCupMS.Business.Object;

import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;

/**
 * DTO for creating and updating robot matches
 */
public class RobotMatchObj {

    /**
     * ID of the first robot (can be null for scheduled-only matches)
     */
    private Long robotAID;

    /**
     * ID of the second robot (null for single-robot disciplines)
     */
    private Long robotBID;

    /**
     * ID of the playground where the match will take place
     */
    private Long playgroundID;

    /**
     * Tournament phase (PRELIMINARY, SEMIFINAL, FINAL, etc.)
     */
    private ETournamentPhase phase;

    /**
     * ID of the next match in bracket (for tournament progression)
     */
    private Long nextMatchID;

    /**
     * Whether higher score wins (true) or lower score wins (false)
     * If not specified, will be inherited from discipline
     */
    private Boolean highScoreWin;

    /**
     * Group name for grouping matches (e.g., for bracket visualization)
     */
    private String group;

    /**
     * X position for visual representation in bracket/matrix view
     */
    private Integer visualX;

    /**
     * Y position for visual representation in bracket/matrix view
     */
    private Integer visualY;

    /**
     * Competition year this match belongs to
     */
    private Integer competitionYear;

    public RobotMatchObj() {
    }

    public RobotMatchObj(Long robotAID, Long robotBID, Long playgroundID, ETournamentPhase phase,
            Long nextMatchID, Boolean highScoreWin) {
        this.robotAID = robotAID;
        this.robotBID = robotBID;
        this.playgroundID = playgroundID;
        this.phase = phase;
        this.nextMatchID = nextMatchID;
        this.highScoreWin = highScoreWin;
    }

    public Long getRobotAID() {
        return this.robotAID;
    }

    public void setRobotAID(Long robotAID) {
        this.robotAID = robotAID;
    }

    public Long getRobotBID() {
        return this.robotBID;
    }

    public void setRobotBID(Long robotBID) {
        this.robotBID = robotBID;
    }

    public Long getPlaygroundID() {
        return this.playgroundID;
    }

    public void setPlaygroundID(Long playgroundID) {
        this.playgroundID = playgroundID;
    }

    public ETournamentPhase getPhase() {
        return this.phase;
    }

    public void setPhase(ETournamentPhase phase) {
        this.phase = phase;
    }

    public Long getNextMatchID() {
        return this.nextMatchID;
    }

    public void setNextMatchID(Long nextMatchID) {
        this.nextMatchID = nextMatchID;
    }

    public Boolean getHighScoreWin() {
        return this.highScoreWin;
    }

    public void setHighScoreWin(Boolean highScoreWin) {
        this.highScoreWin = highScoreWin;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getVisualX() {
        return this.visualX;
    }

    public void setVisualX(Integer visualX) {
        this.visualX = visualX;
    }

    public Integer getVisualY() {
        return this.visualY;
    }

    public void setVisualY(Integer visualY) {
        this.visualY = visualY;
    }

    public Integer getCompetitionYear() {
        return this.competitionYear;
    }

    public void setCompetitionYear(Integer competitionYear) {
        this.competitionYear = competitionYear;
    }

}
