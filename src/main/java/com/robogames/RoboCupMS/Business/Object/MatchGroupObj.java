package com.robogames.RoboCupMS.Business.Object;

public class MatchGroupObj {

    /**
     * Identifikator tvurce zapasove skupiny
     */
    private Long creatorID;

    public MatchGroupObj() {
    }

    public MatchGroupObj(Long creatorID) {
        this.creatorID = creatorID;
    }

    public Long getCreatorID() {
        return this.creatorID;
    }

    public void setCreatorID(Long creatorID) {
        this.creatorID = creatorID;
    }

}
