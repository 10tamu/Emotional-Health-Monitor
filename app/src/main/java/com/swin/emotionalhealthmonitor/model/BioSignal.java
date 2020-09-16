package com.swin.emotionalhealthmonitor.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class BioSignal extends RealmObject {

    @PrimaryKey @Index
    private long id;
    private String heartRate;
    private String skinConductance;
    @Index
    private Date timestamp;

    public BioSignal(){ }

    public BioSignal(String heartRate, String skinConductance, Date timestamp){
        this.heartRate = heartRate;
        this.skinConductance = skinConductance;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(String heartRate) {
        this.heartRate = heartRate;
    }

    public String getSkinConductance() {
        return skinConductance;
    }

    public void setSkinConductance(String skinConductance) {
        this.skinConductance = skinConductance;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Signal:" + id + " [Heart rate : " + heartRate + "] -- [Skin Conductance: " + skinConductance + "]";
    }
}
