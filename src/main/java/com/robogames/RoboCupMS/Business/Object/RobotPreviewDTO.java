package com.robogames.RoboCupMS.Business.Object;

/**
 * DTO representing a single robot in tournament preview
 */
public class RobotPreviewDTO {

    /**
     * Robot ID (null for placeholder positions)
     */
    private Long id;

    /**
     * Robot name
     */
    private String name;

    /**
     * Robot number
     */
    private Long number;

    /**
     * Team name
     */
    private String teamName;

    public RobotPreviewDTO() {
    }

    public RobotPreviewDTO(Long id, String name, Long number, String teamName) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.teamName = teamName;
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

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
