package com.robogames.RoboCupMS.Entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity(name = "team_invitation")
public class TeamInvitation {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserRC user;

    @ManyToOne
    private Team team;

    public TeamInvitation() {
    }

    public TeamInvitation(Long id, UserRC user, Team team) {
        this.id = id;
        this.user = user;
        this.team = team;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public UserRC getUser() {
        return user;
    }

    public void setUser(UserRC user) {
        this.user = user;
    }

}
