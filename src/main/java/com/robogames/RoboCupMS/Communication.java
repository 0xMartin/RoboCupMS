package com.robogames.RoboCupMS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Zajistuje komunikace mezi objekty v ramci aplikace
 */
public class Communication {

    private static Communication communication = null;

    public static interface CallBack {
        void callBack(Object sender, Object data);
    }

    private List<CallBack> callBackList;

    private Communication() {
        this.callBackList = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Navrati seznam vsech call backu
     * 
     * @return List<CallBack>
     */
    public List<CallBack> getCallBacks() {
        return this.callBackList;
    }

    /**
     * Vsem odesle zpravu
     * 
     * @param sender Objekt
     * @param data   Odesilana data
     */
    public synchronized void sendAll(Object sender, Object data) {
        for (CallBack c : this.callBackList) {
            c.callBack(sender, data);
        }
    }

    /**
     * Navrati instanci
     * 
     * @return RequestListener
     */
    public static Communication getInstance() {
        if (Communication.communication == null) {
            Communication.communication = new Communication();
        }
        return Communication.communication;
    }

}
