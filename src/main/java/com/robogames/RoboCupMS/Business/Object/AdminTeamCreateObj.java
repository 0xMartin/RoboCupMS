package com.robogames.RoboCupMS.Business.Object;

import java.util.List;

/**
 * Objekt pro vytvoreni tymu adminem
 */
public class AdminTeamCreateObj {

    /**
     * Jmeno tymu
     */
    private String name;

    /**
     * ID vedouciho tymu
     */
    private Long leaderId;

    /**
     * Seznam ID uzivatelu, kteri budou soucasti tymu
     */
    private List<Long> memberIds;

    public AdminTeamCreateObj() {
    }

    public AdminTeamCreateObj(String name, Long leaderId, List<Long> memberIds) {
        this.name = name;
        this.leaderId = leaderId;
        this.memberIds = memberIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }
}
