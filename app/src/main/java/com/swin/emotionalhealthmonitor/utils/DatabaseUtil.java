package com.swin.emotionalhealthmonitor.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.swin.emotionalhealthmonitor.model.BioSignal;
import com.swin.emotionalhealthmonitor.model.StressData;

import java.util.Date;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * This class contains methods for reading from and writing to Realm database
 *
 * @author Palak, Kunal Pawar
 */
public class DatabaseUtil {

    private Realm dbInstance;

    /**
     * Initializes the database
     *
     * @param context Current context of the application
     */
    public DatabaseUtil(Context context) {
        Realm.init(context);

        RealmConfiguration dbConfig = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();

        this.dbInstance = Realm.getInstance(dbConfig);
    }


    /**
     * Fetches the last persisted entry from database.
     *
     * @return The latest entry persisted in the database.
     * NULL is returned if no data is found
     */
    public BioSignal fetchLatestSignalData() {

        try {
            final BioSignal latestBioSignal =  this.dbInstance.where(BioSignal.class)
                    .findAllSorted("dateTime")
                    .last();

            return latestBioSignal;

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Exception: no results were found");
            return null;

        } catch (Exception e) {
            System.out.println("Unknown exception occurred");
            e.printStackTrace();
            return null;
        }

    }


    /**
     * Returns the signal data within the specified range of dates (inclusive).
     *
     * @param from The starting date of the range
     * @param to The ending date of the range
     * @return An array of BioSignal objects. NULL is returned if no data is found
     */
    public RealmResults<BioSignal> fetchHistoricalBioSignals(Date from, Date to) {

        try {
            final RealmResults<BioSignal> bioSignals =  this.dbInstance.where(BioSignal.class)
                    .greaterThanOrEqualTo("dateTime", from)
                    .lessThanOrEqualTo("dateTime", to)
                    .findAll();

            return bioSignals;

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Exception: no results were found");
            return null;

        } catch (Exception e) {
            System.out.println("Unknown exception occurred");
            e.printStackTrace();
            return null;
        }
    }


    public RealmResults<BioSignal> fetchAllBioSignals() {

        try {
            final RealmResults<BioSignal> bioSignals =  this.dbInstance.where(BioSignal.class)
                    .findAllAsync();

            return bioSignals;

        } catch (Exception e) {
            System.out.println("Unknown Exception occurred");
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Fetches BioSignal data for the given id
     *
     * @param id Id of the bio signal to fetch
     *
     * @return The bio signal for the given id
     */
    public BioSignal fetchBioSignalById(String id) {
        try {
            return this.dbInstance.where(BioSignal.class)
                    .equalTo("id", id)
                    .findFirst();

        } catch (Exception e) {
            System.out.println("Unknown exception occurred");
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Stores the given BioSignal object in the database
     *
     * @param bioSignalData The signal object to store in the database
     */
    public void storeSignalData(@NonNull BioSignal bioSignalData) {

         this.dbInstance.beginTransaction();

        // Autoincrement primary key:

        // Find max value for primary key
        Number max =  this.dbInstance.where(BioSignal.class).max("id");

        // If max value is null, put 0 else use max value
        long id = (max != null) ? (Long) max + 1 : 0;

        // Set the primary value for the object
        bioSignalData.setId(id);

        this.dbInstance.copyToRealm(bioSignalData);
        this.dbInstance.commitTransaction();

    }


    public void storeStressData(@NonNull StressData stressData) {
        this.dbInstance.beginTransaction();

        // Autoincrement primary key:

        // Find max value for primary key
        Number max = this.dbInstance.where(StressData.class).max("id");

        // If max value is null, put 0 else use max value
        long id = (max != null) ? (Long) max + 1 : 0;

        //Set the primary key value for the object
        stressData.setId(id);

        this.dbInstance.copyToRealm(stressData);
        this.dbInstance.commitTransaction();
    }


    public RealmResults<StressData> fetchAllStressData() {

        try {
            final RealmResults<StressData> stressData = this.dbInstance.where(StressData.class)
                    .findAllAsync();

            return stressData;

        } catch (Exception e) {
            System.out.println("Unknown Exception occurred");
            e.printStackTrace();
            return null;
        }
    }


    public void clearDatabase() {

         this.dbInstance.beginTransaction();
         this.dbInstance.deleteAll();
         this.dbInstance.commitTransaction();

    }
}
