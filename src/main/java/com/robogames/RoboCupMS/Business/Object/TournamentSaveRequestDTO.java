package com.robogames.RoboCupMS.Business.Object;

import java.util.List;

import com.robogames.RoboCupMS.Business.Enum.ECategory;

/**
 * DTO for saving tournament structure (sent from frontend after possible edits)
 */
public class TournamentSaveRequestDTO {

    /**
     * Discipline ID
     */
    private Long disciplineId;

    /**
     * Category
     */
    private ECategory category;

    /**
     * Competition year
     */
    private Integer year;

    /**
     * Groups to create
     */
    private List<GroupPreviewDTO> groups;

    /**
     * Bracket to create
     */
    private BracketPreviewDTO bracket;

    public TournamentSaveRequestDTO() {
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
}
