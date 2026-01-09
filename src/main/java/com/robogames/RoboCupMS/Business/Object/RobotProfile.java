package com.robogames.RoboCupMS.Business.Object;

import java.util.List;
import com.robogames.RoboCupMS.Business.Enum.ECategory;

public class RobotProfile {

    private String robotName;
    private Long robotNumber;
    private String discipline;
    private ECategory category;
    private String teamName;
    private Long teamId;
    private Long leaderId;
    private String leaderName;
    private String leaderSurname;
    private List<TeamMemberInfo> teamMembers;
    private String teacherName;
    private String teacherSurname;
    private String teacherContact;
    private Boolean confirmed;

    public RobotProfile() {
    }

    public RobotProfile(String robotName, Long robotNumber, String discipline, ECategory category,
            String teamName, Long teamId, Long leaderId, String leaderName, String leaderSurname,
            List<TeamMemberInfo> teamMembers, String teacherName, String teacherSurname, String teacherContact,
            Boolean confirmed) {
        this.robotName = robotName;
        this.robotNumber = robotNumber;
        this.discipline = discipline;
        this.category = category;
        this.teamName = teamName;
        this.teamId = teamId;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.leaderSurname = leaderSurname;
        this.teamMembers = teamMembers;
        this.teacherName = teacherName;
        this.teacherSurname = teacherSurname;
        this.teacherContact = teacherContact;
        this.confirmed = confirmed;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public Long getRobotNumber() {
        return robotNumber;
    }

    public void setRobotNumber(Long robotNumber) {
        this.robotNumber = robotNumber;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public ECategory getCategory() {
        return category;
    }

    public void setCategory(ECategory category) {
        this.category = category;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getLeaderSurname() {
        return leaderSurname;
    }

    public void setLeaderSurname(String leaderSurname) {
        this.leaderSurname = leaderSurname;
    }

    public List<TeamMemberInfo> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<TeamMemberInfo> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherSurname() {
        return teacherSurname;
    }

    public void setTeacherSurname(String teacherSurname) {
        this.teacherSurname = teacherSurname;
    }

    public String getTeacherContact() {
        return teacherContact;
    }

    public void setTeacherContact(String teacherContact) {
        this.teacherContact = teacherContact;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public static class TeamMemberInfo {
        private String name;
        private String surname;

        public TeamMemberInfo() {
        }

        public TeamMemberInfo(String name, String surname) {
            this.name = name;
            this.surname = surname;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }
    }
}
