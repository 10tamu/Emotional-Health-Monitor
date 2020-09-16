package com.swin.emotionalhealthmonitor.service.foreground;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.swin.emotionalhealthmonitor.MainActivity;
import com.swin.emotionalhealthmonitor.R;
import com.swin.emotionalhealthmonitor.controller.DataReader;
import com.swin.emotionalhealthmonitor.controller.StressClassifier;
import com.swin.emotionalhealthmonitor.model.BioSignal;
import com.swin.emotionalhealthmonitor.utils.MySharedPreferences;
import com.swin.emotionalhealthmonitor.model.StressData;
import com.swin.emotionalhealthmonitor.utils.DatabaseUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;


/**
 * Foreground service class. A service to run a periodic task to read bio signals
 * and analyse the stress classified stress levels.
 *
 * @author Kunal Pawar
 */
public class BioSignalReaderService extends Service {

    public static final int LOW_STRESS = 0;
    public static final int MID_STRESS = 1;
    public static final int HIGH_STRESS = 2;

    // The amount of recent stress levels to use for analysis
    private final int RECENT_STRESS_LEVELS_AMOUNT = 4;

    private final static String SERVICE_CHANNEL_ID = "Bio Signal Monitor Service Channel";
    private final static String STRESS_ALERT_CHANNEL_ID = "High Alert Channel";

    private Handler readHandler;
    private Runnable reader;
    private LinkedList<Integer> recentStressLevels;
    private Integer notificationId;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("------- FOREGROUND SERVICE CREATED -------");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        recentStressLevels = new LinkedList<>();

        String info = intent.getStringExtra("infoText");
        createNotificationChannel(SERVICE_CHANNEL_ID);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                .setContentTitle("Emotional Health Monitor")
                .setContentText(info)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        System.out.println("------- FOREGROUND SERVICE STARTED -------");

        createPeriodicTask();

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        readHandler.removeCallbacks(reader);
        System.out.println("------- FOREGROUND SERVICE STOPPED -------");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void createPeriodicTask() {
        System.out.println("\n\nCreating Periodic task...");
        readHandler = new Handler();

        reader = new Runnable() {
            @Override
            public void run() {
                String intervalPref =
                        new MySharedPreferences(BioSignalReaderService.this)
                        .readData(MySharedPreferences.READ_FREQUENCY);

                long interval = (long) (Float.parseFloat(intervalPref) * 1000f);

                Date time = new Date(System.currentTimeMillis());
                System.out.println("Current time: " + new SimpleDateFormat("HH:mm:ss")
                        .format(time));

                // Read bio signals and analyse stress levels
                analyseStressLevel(readBioSignals());

                readHandler.postDelayed(this, interval);
            }
        };

        readHandler.post(reader);
    }


    /**
     * Uses the csv reader to get body signal data from the CSV.
     * The skiRow value in the shared preferences is updated every time a row of data is read.
     * Finally, it saves the read data values in the database.
     *
     * @return The BioSignal object that was constructed from the data read
     */
    private BioSignal readBioSignals() {
        System.out.println("Reading body signals...\n");

        DataReader dataReader = new DataReader();
        MySharedPreferences prefs = new MySharedPreferences(getApplicationContext());

        Integer skipRows = Integer.parseInt(prefs.readData(MySharedPreferences.SKIP_ROWS));

        // Read the csv data and update skipRows value in shared preferences
        BioSignal bioSignal = dataReader.readCSV(getApplicationContext(), skipRows);

        // Reset skipRows to 0 if all data has been read (bioSignal is null)
        // and read the first row
        if(bioSignal == null) {
            System.out.println("---------- ALL CSV RECORDS READ ----------");
            System.out.println("---------- RESETTING READER ----------");

            skipRows = 0;
            bioSignal = dataReader.readCSV(getApplicationContext(), skipRows);
        }

        prefs.writeData(MySharedPreferences.SKIP_ROWS, (++skipRows).toString());    // Update skip rows in preferences

        saveSignalData(bioSignal);  // Save read data to database

        return bioSignal;
    }


    private void analyseStressLevel(BioSignal bioSignal) {
        StressData stressData = getStressData(bioSignal);
        int highStressCount = 0;
        int highStressThreshold = RECENT_STRESS_LEVELS_AMOUNT / 2;

        // Update recent stress levels
        if(recentStressLevels.size() < RECENT_STRESS_LEVELS_AMOUNT) {
            recentStressLevels.addLast(stressData.getStressLevel());
        }
        else {
            recentStressLevels.removeFirst();
            recentStressLevels.set(0, stressData.getStressLevel());
        }

        // If enough recent stress levels are available then
        // calculate to check if 50% of them are high stress
        if(recentStressLevels.size() == RECENT_STRESS_LEVELS_AMOUNT) {
            // Check number of high stress data
            for(int stressLevel : recentStressLevels) {
                if(stressLevel == HIGH_STRESS) {
                    highStressCount ++;
                }
            }

            if(highStressCount >= highStressThreshold) {
                String time = new SimpleDateFormat("KK:mm:ss a")
                        .format(new Date(System.currentTimeMillis()));

                alert("High stress detected at " + time);  // Show notification
                System.out.println("** HIGH STRESS DETECTED **");
                System.out.println("Recent stress levels\n" + recentStressLevels);
                recentStressLevels.clear();
            }
        }
    }


    /**
     * Saves the Bio signal data to database.
     *
     * @param bioSignal Data to be written to the database
     */
    private void saveSignalData(BioSignal bioSignal) {
        new DatabaseUtil(getApplicationContext()).storeSignalData(bioSignal);
    }


    /**
     * Calls the predictor to get the stress level based on the given bio signal.
     *
     * @param bioSignal Bio signal to analyse for stress level
     *
     * @return The stress level value returned by the predictor
     */
    private StressData getStressData(BioSignal bioSignal) {
        int stressLevel = StressClassifier.getStressClassification(this, bioSignal);

        StressData stressData = new StressData(stressLevel, bioSignal.getId());
        saveStressLevel(stressData);

        return stressData;
    }


    /**
     * Saves stress data to database
     * @param stressData
     */
    private void saveStressLevel(StressData stressData) {
        new DatabaseUtil(this).storeStressData(stressData);
    }


    /**
     * Shows a notification with the given message.
     *
     * @param message The message to display on the notification
     *
     * @return The notification id for this notification
     */
    public int alert(String message) {
        notificationId = (notificationId == null) ? new Random().nextInt() : notificationId;
        createNotificationChannel(STRESS_ALERT_CHANNEL_ID);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent
                .getActivity(getApplicationContext(), 0, intent, 0);


        Notification notification = new NotificationCompat
                .Builder(this, STRESS_ALERT_CHANNEL_ID)
                .setSmallIcon(R.drawable.star)
                .setContentTitle("Emotional Health Monitor")
                .setContentText(message)
                .setContentIntent(pIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(notificationId, notification);

        return notificationId;
    }


    /**
     * Creates a notification channel for displaying notifications.
     * This is only required for Android versions 26(Oreo) and above.
     */
    private void createNotificationChannel(String channelId) {

        int importance = (channelId.equals(STRESS_ALERT_CHANNEL_ID)) ?
                NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_DEFAULT;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel (
                    channelId,
                    "Bio Signal Monitor Channel",
                    importance
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
