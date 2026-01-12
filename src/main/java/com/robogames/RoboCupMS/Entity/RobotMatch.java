package com.robogames.RoboCupMS.Entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.robogames.RoboCupMS.Business.Enum.EScoreType;
import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;

/**
 * Entity representing a robot match
 * Supports both single-robot matches (e.g., line follower) and two-robot matches (e.g., sumo)
 */
@Entity(name = "robot_match")
public class RobotMatch {

    /**
     * Match ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * First robot in the match (can be null if match is only scheduled but robots not assigned yet)
     */
    @ManyToOne(optional = true)
    private Robot robotA;

    /**
     * Second robot in the match (null for single-robot disciplines like line follower)
     */
    @ManyToOne(optional = true)
    private Robot robotB;

    /**
     * Score of robot A
     */
    @Column(name = "score_a", nullable = true)
    private Float scoreA;

    /**
     * Score of robot B
     */
    @Column(name = "score_b", nullable = true)
    private Float scoreB;

    /**
     * Score type for this match (inherited from discipline)
     */
    @ManyToOne(optional = true)
    private ScoreType scoreType;

    /**
     * Current state of the match
     */
    @ManyToOne(optional = false)
    private MatchState state;

    /**
     * Playground where the match takes place
     */
    @ManyToOne(optional = false)
    private Playground playground;

    /**
     * Timestamp of last modification
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Tournament phase (preliminary, semifinal, final, etc.)
     */
    @ManyToOne(optional = true)
    private TournamentPhase phase;

    /**
     * Reference to the next match in bracket-style tournaments
     * Winner of this match will be automatically assigned to the next match
     */
    @ManyToOne(optional = true)
    private RobotMatch nextMatch;

    /**
     * Determines how to select the winner based on score
     * true = higher score wins (e.g., sumo wins count)
     * false = lower score wins (e.g., time-based like line follower)
     */
    @Column(name = "high_score_win", nullable = false)
    private Boolean highScoreWin;

    /**
     * Default constructor - creates a scheduled match
     */
    public RobotMatch() {
        this.scoreA = null;
        this.scoreB = null;
        this.highScoreWin = true;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor for creating a match with all parameters
     * 
     * @param robotA       First robot (can be null)
     * @param robotB       Second robot (can be null for single-robot disciplines)
     * @param playground   Playground where the match will be played
     * @param state        Initial match state
     * @param scoreType    Type of scoring
     * @param phase        Tournament phase
     * @param nextMatch    Next match in bracket (can be null)
     * @param highScoreWin Whether higher score wins
     */
    public RobotMatch(Robot robotA, Robot robotB, Playground playground, MatchState state,
            ScoreType scoreType, TournamentPhase phase, RobotMatch nextMatch, boolean highScoreWin) {
        this.robotA = robotA;
        this.robotB = robotB;
        this.playground = playground;
        this.state = state;
        this.scoreType = scoreType;
        this.phase = phase;
        this.nextMatch = nextMatch;
        this.highScoreWin = highScoreWin;
        this.scoreA = null;
        this.scoreB = null;
        this.timestamp = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.timestamp = LocalDateTime.now();
    }

    // ==================== GETTERS ====================

    /**
     * Get the match ID
     * 
     * @return Match ID
     */
    @JsonProperty("id")
    public Long getID() {
        return this.id;
    }

    /**
     * Get robot A's ID (returns null if robot not assigned)
     * 
     * @return Robot A's ID or null
     */
    public Long getRobotAID() {
        return this.robotA != null ? this.robotA.getID() : null;
    }

    /**
     * Get robot B's ID (returns null if robot not assigned)
     * 
     * @return Robot B's ID or null
     */
    public Long getRobotBID() {
        return this.robotB != null ? this.robotB.getID() : null;
    }

    /**
     * Get robot A's number
     * 
     * @return Robot A's number or null
     */
    public Long getRobotANumber() {
        return this.robotA != null ? this.robotA.getNumber() : null;
    }

    /**
     * Get robot B's number
     * 
     * @return Robot B's number or null
     */
    public Long getRobotBNumber() {
        return this.robotB != null ? this.robotB.getNumber() : null;
    }

    /**
     * Get robot A's name
     * 
     * @return Robot A's name or null
     */
    public String getRobotAName() {
        return this.robotA != null ? this.robotA.getName() : null;
    }

    /**
     * Get robot B's name
     * 
     * @return Robot B's name or null
     */
    public String getRobotBName() {
        return this.robotB != null ? this.robotB.getName() : null;
    }

    /**
     * Get team A's ID
     * 
     * @return Team A's ID or null
     */
    public Long getTeamAID() {
        return this.robotA != null ? this.robotA.getTeamRegistration().getTeamID() : null;
    }

    /**
     * Get team B's ID
     * 
     * @return Team B's ID or null
     */
    public Long getTeamBID() {
        return this.robotB != null ? this.robotB.getTeamRegistration().getTeamID() : null;
    }

    /**
     * Get team A's name
     * 
     * @return Team A's name or null
     */
    public String getTeamAName() {
        return this.robotA != null ? this.robotA.getTeamRegistration().getTeam().getName() : null;
    }

    /**
     * Get team B's name
     * 
     * @return Team B's name or null
     */
    public String getTeamBName() {
        return this.robotB != null ? this.robotB.getTeamRegistration().getTeam().getName() : null;
    }

    /**
     * Get score of robot A
     * 
     * @return Score A
     */
    public Float getScoreA() {
        return this.scoreA;
    }

    /**
     * Get score of robot B
     * 
     * @return Score B
     */
    public Float getScoreB() {
        return this.scoreB;
    }

    /**
     * Get the score type
     * 
     * @return Score type or null
     */
    public EScoreType getScoreTypeName() {
        return this.scoreType != null ? this.scoreType.getName() : null;
    }

    /**
     * Get the match state
     * 
     * @return Match state
     */
    public MatchState getState() {
        return this.state;
    }

    /**
     * Get the playground ID
     * 
     * @return Playground ID
     */
    public Long getPlaygroundID() {
        return this.playground.getID();
    }

    /**
     * Get the playground name
     * 
     * @return Playground name
     */
    public String getPlaygroundName() {
        return this.playground.getName();
    }

    /**
     * Get the playground number
     * 
     * @return Playground number
     */
    public int getPlaygroundNumber() {
        return this.playground.getNumber();
    }

    /**
     * Get the last modification timestamp
     * 
     * @return Timestamp
     */
    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    /**
     * Get the tournament phase name
     * 
     * @return Tournament phase name or null
     */
    public ETournamentPhase getPhaseName() {
        return this.phase != null ? this.phase.getName() : null;
    }

    /**
     * Get the next match ID
     * 
     * @return Next match ID or null
     */
    public Long getNextMatchID() {
        return this.nextMatch != null ? this.nextMatch.getID() : null;
    }

    /**
     * Get whether higher score wins
     * 
     * @return true if higher score wins, false if lower score wins
     */
    public Boolean getHighScoreWin() {
        return this.highScoreWin;
    }

    /**
     * Get the discipline name (from playground)
     * 
     * @return Discipline name
     */
    public String getDisciplineName() {
        return this.playground.getDisciplineName();
    }

    /**
     * Get the discipline ID (from playground)
     * 
     * @return Discipline ID
     */
    public Long getDisciplineID() {
        return this.playground.getDisciplineID();
    }

    /**
     * Get category of robot A
     * 
     * @return Category string or null
     */
    public String getCategoryA() {
        return this.robotA != null ? this.robotA.getCategory().toString() : null;
    }

    /**
     * Get category of robot B
     * 
     * @return Category string or null
     */
    public String getCategoryB() {
        return this.robotB != null ? this.robotB.getCategory().toString() : null;
    }

    /**
     * Check if this is a two-robot match
     * 
     * @return true if match has two robots assigned
     */
    public Boolean isTwoRobotMatch() {
        return this.robotB != null;
    }

    /**
     * Check if this match has any robots assigned
     * 
     * @return true if at least one robot is assigned
     */
    public Boolean hasRobots() {
        return this.robotA != null;
    }

    // ==================== JSON IGNORE GETTERS ====================

    /**
     * Get robot A entity
     * 
     * @return Robot A
     */
    @JsonIgnore
    public Robot getRobotA() {
        return this.robotA;
    }

    /**
     * Get robot B entity
     * 
     * @return Robot B
     */
    @JsonIgnore
    public Robot getRobotB() {
        return this.robotB;
    }

    /**
     * Get the playground entity
     * 
     * @return Playground
     */
    @JsonIgnore
    public Playground getPlayground() {
        return this.playground;
    }

    /**
     * Get the score type entity
     * 
     * @return Score type
     */
    @JsonIgnore
    public ScoreType getScoreType() {
        return this.scoreType;
    }

    /**
     * Get the tournament phase entity
     * 
     * @return Tournament phase
     */
    @JsonIgnore
    public TournamentPhase getPhase() {
        return this.phase;
    }

    /**
     * Get the next match entity
     * 
     * @return Next match
     */
    @JsonIgnore
    public RobotMatch getNextMatch() {
        return this.nextMatch;
    }

    // ==================== SETTERS ====================

    /**
     * Set robot A
     * 
     * @param robot Robot A
     */
    public void setRobotA(Robot robot) {
        this.robotA = robot;
    }

    /**
     * Set robot B
     * 
     * @param robot Robot B
     */
    public void setRobotB(Robot robot) {
        this.robotB = robot;
    }

    /**
     * Set score A
     * 
     * @param score Score for robot A
     */
    public void setScoreA(Float score) {
        this.scoreA = score;
    }

    /**
     * Set score B
     * 
     * @param score Score for robot B
     */
    public void setScoreB(Float score) {
        this.scoreB = score;
    }

    /**
     * Set the score type
     * 
     * @param scoreType Score type
     */
    public void setScoreType(ScoreType scoreType) {
        this.scoreType = scoreType;
    }

    /**
     * Set the match state
     * 
     * @param state New match state
     */
    public void setMatchState(MatchState state) {
        this.state = state;
    }

    /**
     * Set the playground
     * 
     * @param playground New playground
     */
    public void setPlayground(Playground playground) {
        this.playground = playground;
    }

    /**
     * Set the tournament phase
     * 
     * @param phase Tournament phase
     */
    public void setPhase(TournamentPhase phase) {
        this.phase = phase;
    }

    /**
     * Set the next match in bracket
     * 
     * @param nextMatch Next match
     */
    public void setNextMatch(RobotMatch nextMatch) {
        this.nextMatch = nextMatch;
    }

    /**
     * Set whether higher score wins
     * 
     * @param highScoreWin true if higher score wins
     */
    public void setHighScoreWin(Boolean highScoreWin) {
        this.highScoreWin = highScoreWin;
    }

    /**
     * Determine the winner of this match based on scores and highScoreWin setting
     * 
     * @return Winner robot, or null if no clear winner (tie, missing scores, or single robot match)
     */
    @JsonIgnore
    public Robot getWinner() {
        if (this.scoreA == null) {
            return null;
        }
        
        // Single robot match - robot A is the "winner" if score is recorded
        if (this.robotB == null) {
            return this.robotA;
        }
        
        if (this.scoreB == null) {
            return null;
        }
        
        // Two robot match - determine winner based on scores
        if (this.scoreA.equals(this.scoreB)) {
            return null; // Tie
        }
        
        if (this.highScoreWin) {
            return this.scoreA > this.scoreB ? this.robotA : this.robotB;
        } else {
            return this.scoreA < this.scoreB ? this.robotA : this.robotB;
        }
    }

    /**
     * Get the winning score
     * 
     * @return Winning score or null if no winner
     */
    public Float getWinnerScore() {
        Robot winner = getWinner();
        if (winner == null) {
            return null;
        }
        if (winner.equals(this.robotA)) {
            return this.scoreA;
        }
        return this.scoreB;
    }

}
