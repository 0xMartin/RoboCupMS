package com.robogames.RoboCupMS.Business.Object;

import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;

/**
 * DTO representing a single match in tournament preview
 */
public class MatchPreviewDTO {

    /**
     * Temporary match ID (for frontend identification)
     */
    private String tempId;

    /**
     * First robot (can be null for bracket matches)
     */
    private RobotPreviewDTO robotA;

    /**
     * Second robot (can be null for bracket matches)
     */
    private RobotPreviewDTO robotB;

    /**
     * Tournament phase
     */
    private ETournamentPhase phase;

    /**
     * Group name
     */
    private String group;

    /**
     * X position for visual representation
     */
    private Integer visualX;

    /**
     * Y position for visual representation
     */
    private Integer visualY;

    /**
     * Reference to the next match temp ID (for bracket progression)
     */
    private String nextMatchTempId;

    /**
     * Playground ID to use
     */
    private Long playgroundId;

    /**
     * Round name (e.g., "Semifinal", "Final", "Group A Round 1")
     */
    private String roundName;

    /**
     * Match order within the round
     */
    private Integer matchOrder;

    /**
     * Whether this match is a bye (automatic advancement)
     */
    private Boolean isBye = false;

    public MatchPreviewDTO() {
    }

    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    public RobotPreviewDTO getRobotA() {
        return robotA;
    }

    public void setRobotA(RobotPreviewDTO robotA) {
        this.robotA = robotA;
    }

    public RobotPreviewDTO getRobotB() {
        return robotB;
    }

    public void setRobotB(RobotPreviewDTO robotB) {
        this.robotB = robotB;
    }

    public ETournamentPhase getPhase() {
        return phase;
    }

    public void setPhase(ETournamentPhase phase) {
        this.phase = phase;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getVisualX() {
        return visualX;
    }

    public void setVisualX(Integer visualX) {
        this.visualX = visualX;
    }

    public Integer getVisualY() {
        return visualY;
    }

    public void setVisualY(Integer visualY) {
        this.visualY = visualY;
    }

    public String getNextMatchTempId() {
        return nextMatchTempId;
    }

    public void setNextMatchTempId(String nextMatchTempId) {
        this.nextMatchTempId = nextMatchTempId;
    }

    public Long getPlaygroundId() {
        return playgroundId;
    }

    public void setPlaygroundId(Long playgroundId) {
        this.playgroundId = playgroundId;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public Integer getMatchOrder() {
        return matchOrder;
    }

    public void setMatchOrder(Integer matchOrder) {
        this.matchOrder = matchOrder;
    }

    public Boolean getIsBye() {
        return isBye;
    }

    public void setIsBye(Boolean isBye) {
        this.isBye = isBye;
    }
}
