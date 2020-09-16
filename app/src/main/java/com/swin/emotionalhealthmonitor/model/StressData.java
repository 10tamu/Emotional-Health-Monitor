package com.swin.emotionalhealthmonitor.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class StressData extends RealmObject {

    @PrimaryKey @Index
    private long id;
    @Index
    private int stressLevel;
    @Index
    private Date timestamp;
    private long bioSignalId;   // Foreign key TODO: Find a better way to link

    public StressData() { }

    public StressData(int stressLevel, long bioSignalId) {
        this.stressLevel = stressLevel;
        this.bioSignalId = bioSignalId;
        this.timestamp = new Date(System.currentTimeMillis());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStressLevel() {
        return stressLevel;
    }


    public Date getTimestamp() {
        return timestamp;
    }


    public long getBioSignalId() {
        return bioSignalId;
    }

    public String toString() {
        return "Id: " + this.id + "\n" +
                "Stress level: " + this.stressLevel + "\n" +
                "Bio Signal id: " + this.bioSignalId + "\n";
    }
}
