package com.robogames.RoboCupMS.Business.Object;

/**
 * DTO for writing match scores
 */
public class MatchScoreObj {

    /**
     * Match ID
     */
    private Long matchID;

    /**
     * Score for robot A
     */
    private Float scoreA;

    /**
     * Score for robot B (null for single-robot matches)
     */
    private Float scoreB;

    public MatchScoreObj() {
    }

    public MatchScoreObj(Long matchID, Float scoreA, Float scoreB) {
        this.matchID = matchID;
        this.scoreA = scoreA;
        this.scoreB = scoreB;
    }

    public Long getMatchID() {
        return this.matchID;
    }

    public void setMatchID(Long matchID) {
        this.matchID = matchID;
    }

    public Float getScoreA() {
        return this.scoreA;
    }

    public void setScoreA(Float scoreA) {
        this.scoreA = scoreA;
    }

    public Float getScoreB() {
        return this.scoreB;
    }

    public void setScoreB(Float scoreB) {
        this.scoreB = scoreB;
    }

}
