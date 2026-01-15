package com.robogames.RoboCupMS.Business.Object;

import java.util.List;

import com.robogames.RoboCupMS.Business.Enum.ECategory;

/**
 * DTO representing the complete tournament structure preview
 */
public class TournamentPreviewDTO {

    /**
     * Discipline ID
     */
    private Long disciplineId;

    /**
     * Discipline name
     */
    private String disciplineName;

    /**
     * Category
     */
    private ECategory category;

    /**
     * Competition year
     */
    private Integer year;

    /**
     * List of groups (round-robin phase)
     */
    private List<GroupPreviewDTO> groups;

    /**
     * Bracket (elimination phase)
     */
    private BracketPreviewDTO bracket;

    /**
     * Total number of group matches
     */
    private Integer totalGroupMatches;

    /**
     * Total number of bracket matches
     */
    private Integer totalBracketMatches;

    /**
     * Estimated total time in minutes
     */
    private Integer estimatedTimeMinutes;

    /**
     * Number of playgrounds used
     */
    private Integer playgroundCount;

    /**
     * Available playgrounds for this discipline
     */
    private List<PlaygroundInfoDTO> availablePlaygrounds;

    /**
     * Validation messages/warnings
     */
    private List<String> warnings;

    public TournamentPreviewDTO() {
    }

    public Long getDisciplineId() {
        return disciplineId;
    }

    public void setDisciplineId(Long disciplineId) {
        this.disciplineId = disciplineId;
    }

    public String getDisciplineName() {
        return disciplineName;
    }

    public void setDisciplineName(String disciplineName) {
        this.disciplineName = disciplineName;
    }

    public ECategory getCategory() {
        return category;
    }

    public void setCategory(ECategory category) {
        this.category = category;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<GroupPreviewDTO> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupPreviewDTO> groups) {
        this.groups = groups;
    }

    public BracketPreviewDTO getBracket() {
        return bracket;
    }

    public void setBracket(BracketPreviewDTO bracket) {
        this.bracket = bracket;
    }

    public Integer getTotalGroupMatches() {
        return totalGroupMatches;
    }

    public void setTotalGroupMatches(Integer totalGroupMatches) {
        this.totalGroupMatches = totalGroupMatches;
    }

    public Integer getTotalBracketMatches() {
        return totalBracketMatches;
    }

    public void setTotalBracketMatches(Integer totalBracketMatches) {
        this.totalBracketMatches = totalBracketMatches;
    }

    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public Integer getPlaygroundCount() {
        return playgroundCount;
    }

    public void setPlaygroundCount(Integer playgroundCount) {
        this.playgroundCount = playgroundCount;
    }

    public List<PlaygroundInfoDTO> getAvailablePlaygrounds() {
        return availablePlaygrounds;
    }

    public void setAvailablePlaygrounds(List<PlaygroundInfoDTO> availablePlaygrounds) {
        this.availablePlaygrounds = availablePlaygrounds;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    /**
     * Simple playground info DTO
     */
    public static class PlaygroundInfoDTO {
        private Long id;
        private String name;
        private Integer number;

        public PlaygroundInfoDTO() {
        }

        public PlaygroundInfoDTO(Long id, String name, Integer number) {
            this.id = id;
            this.name = name;
            this.number = number;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }
    }
}
