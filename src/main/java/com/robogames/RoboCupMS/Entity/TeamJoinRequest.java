package com.robogames.RoboCupMS.Entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Zadost o vstup do tymu - uzivatel bez tymu muze pozadat o vstup do tymu
 */
@Entity(name = "team_join_request")
public class TeamJoinRequest {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Uzivatel, ktery zada o vstup do tymu
     */
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserRC user;

    /**
     * Tym, do ktereho uzivatel zada o vstup
     */
    @ManyToOne
    private Team team;

    /**
     * Cas vytvoreni zadosti
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public TeamJoinRequest() {
        this.createdAt = LocalDateTime.now();
    }

    public TeamJoinRequest(UserRC user, Team team) {
        this.user = user;
        this.team = team;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserRC getUser() {
        return user;
    }

    public void setUser(UserRC user) {
        this.user = user;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
