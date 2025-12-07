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
    private List<TeamMemberInfo> teamMembers;
    private String teacherName;
    private String teacherSurname;
    private String teacherContact;

    public RobotProfile() {
    }

    public RobotProfile(String robotName, Long robotNumber, String discipline, ECategory category,
                        String teamName, Long teamId, List<TeamMemberInfo> teamMembers,
                        String teacherName, String teacherSurname, String teacherContact) {
        this.robotName = robotName;
        this.robotNumber = robotNumber;
        this.discipline = discipline;
        this.category = category;
        this.teamName = teamName;
        this.teamId = teamId;
        this.teamMembers = teamMembers;
        this.teacherName = teacherName;
        this.teacherSurname = teacherSurname;
        this.teacherContact = teacherContact;
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
