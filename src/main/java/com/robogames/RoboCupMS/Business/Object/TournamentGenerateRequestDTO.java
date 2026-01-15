package com.robogames.RoboCupMS.Business.Object;

import com.robogames.RoboCupMS.Business.Enum.ECategory;

/**
 * DTO for requesting tournament structure generation
 */
public class TournamentGenerateRequestDTO {

    /**
     * Discipline ID
     */
    private Long disciplineId;

    /**
     * Category (LOW_AGE_CATEGORY or HIGH_AGE_CATEGORY)
     */
    private ECategory category;

    /**
     * Competition year
     */
    private Integer year;

    /**
     * Total available time in minutes
     */
    private Integer availableTimeMinutes;

    /**
     * Time for one match in minutes
     */
    private Integer matchTimeMinutes;

    /**
     * Number of playgrounds/rings available
     */
    private Integer playgroundCount;

    /**
     * Preferred group size (3-6 robots per group)
     */
    private Integer groupSize;

    /**
     * Number of robots advancing from each group to bracket
     */
    private Integer advancingPerGroup;

    public TournamentGenerateRequestDTO() {
    }

    public Long getDisciplineId() {
        return disciplineId;
    }

    public void setDisciplineId(Long disciplineId) {
        this.disciplineId = disciplineId;
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

    public Integer getAvailableTimeMinutes() {
        return availableTimeMinutes;
    }

    public void setAvailableTimeMinutes(Integer availableTimeMinutes) {
        this.availableTimeMinutes = availableTimeMinutes;
    }

    public Integer getMatchTimeMinutes() {
        return matchTimeMinutes;
    }

    public void setMatchTimeMinutes(Integer matchTimeMinutes) {
        this.matchTimeMinutes = matchTimeMinutes;
    }

    public Integer getPlaygroundCount() {
        return playgroundCount;
    }

    public void setPlaygroundCount(Integer playgroundCount) {
        this.playgroundCount = playgroundCount;
    }

    public Integer getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(Integer groupSize) {
        this.groupSize = groupSize;
    }

    public Integer getAdvancingPerGroup() {
        return advancingPerGroup;
    }

    public void setAdvancingPerGroup(Integer advancingPerGroup) {
        this.advancingPerGroup = advancingPerGroup;
    }
}
