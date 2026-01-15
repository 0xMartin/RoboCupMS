package com.robogames.RoboCupMS.Business.Object;

import java.util.List;

/**
 * DTO representing a group (round-robin) in tournament preview
 */
public class GroupPreviewDTO {

    /**
     * Group name (e.g., "A", "B", "C")
     */
    private String name;

    /**
     * Full group identifier for database
     */
    private String groupId;

    /**
     * List of robots in this group
     */
    private List<RobotPreviewDTO> robots;

    /**
     * List of matches in this group
     */
    private List<MatchPreviewDTO> matches;

    /**
     * Number of robots advancing from this group
     */
    private Integer advancingCount;

    public GroupPreviewDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<RobotPreviewDTO> getRobots() {
        return robots;
    }

    public void setRobots(List<RobotPreviewDTO> robots) {
        this.robots = robots;
    }

    public List<MatchPreviewDTO> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchPreviewDTO> matches) {
        this.matches = matches;
    }

    public Integer getAdvancingCount() {
        return advancingCount;
    }

    public void setAdvancingCount(Integer advancingCount) {
        this.advancingCount = advancingCount;
    }
}
