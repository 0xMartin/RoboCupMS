package com.robogames.RoboCupMS.Business.Enum;

/**
 * Competition mode enumeration - determines how a discipline is evaluated
 */
public enum ECompetitionMode {

    /**
     * Tournament mode - uses groups (round-robin) + bracket elimination
     * Used for disciplines like RoboSumo
     */
    TOURNAMENT,

    /**
     * Best score mode - simple ranking by best achieved score
     * Used for disciplines like Line Follower, Robot Cleaner
     */
    BEST_SCORE

}
