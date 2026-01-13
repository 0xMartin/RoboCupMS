package com.robogames.RoboCupMS.Business.Object;

import java.time.LocalDateTime;

/**
 * DTO pro zadost o vstup do tymu
 */
public class TeamJoinRequestObj {

    private Long id;

    private Long userId;

    private String userName;

    private String userSurname;

    private String userEmail;

    private Long teamId;

    private String teamName;

    private LocalDateTime createdAt;

    public TeamJoinRequestObj() {
    }

    public TeamJoinRequestObj(Long id, Long userId, String userName, String userSurname, String userEmail,
                              Long teamId, String teamName, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userSurname = userSurname;
        this.userEmail = userEmail;
        this.teamId = teamId;
        this.teamName = teamName;
        this.createdAt = createdAt;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserSurname() {
        return userSurname;
    }

    public void setUserSurname(String userSurname) {
        this.userSurname = userSurname;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
