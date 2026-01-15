package com.robogames.RoboCupMS.Business.Object;

import java.util.List;

/**
 * DTO representing bracket (elimination) stage in tournament preview
 */
public class BracketPreviewDTO {

    /**
     * Bracket identifier for database
     */
    private String bracketId;

    /**
     * List of rounds in the bracket
     */
    private List<BracketRoundDTO> rounds;

    /**
     * Total number of participants in bracket
     */
    private Integer participantCount;

    /**
     * Number of byes (automatic advancements) in first round
     */
    private Integer byeCount = 0;

    public BracketPreviewDTO() {
    }

    public String getBracketId() {
        return bracketId;
    }

    public void setBracketId(String bracketId) {
        this.bracketId = bracketId;
    }

    public List<BracketRoundDTO> getRounds() {
        return rounds;
    }

    public void setRounds(List<BracketRoundDTO> rounds) {
        this.rounds = rounds;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    public Integer getByeCount() {
        return byeCount;
    }

    public void setByeCount(Integer byeCount) {
        this.byeCount = byeCount;
    }

    /**
     * Inner class representing a single round in the bracket
     */
    public static class BracketRoundDTO {

        /**
         * Round name (e.g., "Quarterfinal", "Semifinal", "Final")
         */
        private String name;

        /**
         * Round number (1 = first round, etc.)
         */
        private Integer roundNumber;

        /**
         * Matches in this round
         */
        private List<MatchPreviewDTO> matches;

        public BracketRoundDTO() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getRoundNumber() {
            return roundNumber;
        }

        public void setRoundNumber(Integer roundNumber) {
            this.roundNumber = roundNumber;
        }

        public List<MatchPreviewDTO> getMatches() {
            return matches;
        }

        public void setMatches(List<MatchPreviewDTO> matches) {
            this.matches = matches;
        }
    }
}
