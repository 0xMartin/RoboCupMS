package com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Object;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.robogames.RoboCupMS.Entity.Team;

/**
 * Uchovava celkove skore vsech robotu, ktery tym vlastni
 */
public class TeamScore {

    private final List<RobotScore> scores;

    private final Team team;

    /**
     * Uchovava celkove skore vsech robotu, ktery tym vlastni
     * 
     * @param _team   Tym
     * @param _scores List s celkovym skore pro vsechny roboty tymu
     */
    public TeamScore(Team _team, List<RobotScore> _scores) {
        this.team = _team;
        this.scores = _scores;
    }

    @JsonIgnore
    public Team getTeam() {
        return this.team;
    }

    /**
     * Navrati ID tymu
     * 
     * @return ID tymu
     */
    public long getTeamID() {
        if (this.team == null) {
            return Team.NOT_IN_TEAM;
        }
        return this.team.getID();
    }

    /**
     * Navrati jmeno tymu
     * 
     * @return Jmeno tymu
     */
    public String getTeamName() {
        if (this.team == null) {
            return "";
        }
        return this.team.getName();
    }

    /**
     * Navrati jmena vsech clenu tymu
     * 
     * @return Seznam jmen vsech clenu
     */
    public List<UserFullName> getMembers() {
        List<UserFullName> names = new ArrayList<UserFullName>();
        if (this.team != null) {
            this.team.getMembers().forEach((u) -> {
                names.add(new UserFullName(u.getName(), u.getSurname()));
            });
        }
        return names;
    }

    /**
     * Navrati celkove skore vsech robotu tymu
     * 
     * @return Seznam vsech skore
     */
    public List<RobotScore> getScore() {
        return this.scores;
    }

}
