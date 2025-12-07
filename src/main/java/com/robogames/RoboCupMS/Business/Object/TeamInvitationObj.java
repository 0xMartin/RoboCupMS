package com.robogames.RoboCupMS.Business.Object;

public class TeamInvitationObj {

    private Long id;

    private Long userId;

    private Long teamId;

    private String teamName;

    public TeamInvitationObj() {
    }

    public TeamInvitationObj(Long id, Long userId, Long teamId, String teamName) {
        this.id = id;
        this.userId = userId;
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

}
