package com.swin.emotionalhealthmonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.swin.emotionalhealthmonitor.controller.SetHrMinMax;
import com.swin.emotionalhealthmonitor.controller.SetReadingFrequency;
import com.swin.emotionalhealthmonitor.controller.SetScMinMax;
import com.swin.emotionalhealthmonitor.model.BioSignal;
import com.swin.emotionalhealthmonitor.utils.MySharedPreferences;
import com.swin.emotionalhealthmonitor.model.StressData;
import com.swin.emotionalhealthmonitor.service.foreground.BioSignalReaderService;
import com.swin.emotionalhealthmonitor.utils.DatabaseUtil;
import com.swin.emotionalhealthmonitor.views.CustomMarkerView;

import java.util.ArrayList;
import java.util.List;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;


/**
 * MainActivity
 *
 * @author Palak, Kunal Pawar
 */
public class MainActivity extends AppCompatActivity {

    // TODO: Manage activity lifecycle methods, fix graph issues, make chart xaxis scrollable

    private final int LOW_STRESS_INDICATION = 25;
    private final int MID_STRESS_INDICATION = 50;
    private final int HIGH_STRESS_INDICATION = 100;

    private final String LOW_STRESS_COLOR = "#66ff33";
    private final String MID_STRESS_COLOR = "#ffcc00";
    private final String HIGH_STRESS_COLOR = "#ff471a";

    private LineChart chartHR,chartSC;
    private ProgressBar progStressLevel;
    private TextView txtStressLabel;
    private CustomMarkerView markerViewHR, markerViewSC;
    private Drawable drawableHR, drawableSC;

    private Button btnClearDb;
    private Switch switchMonitoring;
    private ImageView menuIcon;
    private AlertDialog.Builder builder;

    private RealmResults<StressData> stressData;
    private RealmResults<BioSignal> bioSignals;

    private LineDataSet dataSetHR, dataSetSC;
    private List<Entry> entriesHR = new ArrayList<>();
    private List<Entry> entriesSC = new ArrayList<>();

    private MySharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Emotional Health Monitor");

        setContentView(R.layout.activity_main);
        switchMonitoring = findViewById(R.id.switchMonitoring);
        btnClearDb = findViewById(R.id.btnClearDb);

        menuIcon = findViewById(R.id.menuIcon);
        progStressLevel = findViewById(R.id.progStressLevel);
        txtStressLabel = findViewById(R.id.txtStressLabel);
        chartHR = findViewById(R.id.chartHR);
        chartSC = findViewById(R.id.chartSC);

        setButtonClickListeners();
        setSharedPreferenceChangedListener();
    }


    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("------- ACTIVITY RESUMED -------");

        addBioSignalUpdateListener();
        addStressChangeListener();
        initialiseChart();

        markerViewHR = new CustomMarkerView(this, R.layout.custom_marker_view);
        markerViewSC = new CustomMarkerView(this, R.layout.custom_marker_view);

        drawableHR = ContextCompat.getDrawable(this, R.drawable.fade_red);
        drawableSC = ContextCompat.getDrawable(this, R.drawable.fade_yellow);

    }

    @Override
    protected void onStop() {
        super.onStop();
        removeRealmChangeListeners();
        System.out.println("------- ACTIVITY STOPPED -------");
    }


    @Override
    protected void onDestroy() {
        System.out.println("------- ACTIVITY DESTROYED -------");
        releaseResources();
        super.onDestroy();
    }

    private void releaseResources() {
        removeRealmChangeListeners();
        prefs.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefChangeListener);
    }

    /**
     * Sets onclick listeners for the button, switch and vertical menu icon
     */
    private void setButtonClickListeners() {

        // Clear database button listener
        btnClearDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetAll();
            }
        });


        // Monitoring toggle switch listener
        switchMonitoring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if(isChecked) {
                    startService();
                    switchMonitoring.setText("Stop Monitoring");
                }
                else {
                    stopService();
                    switchMonitoring.setText("Start Monitoring");
                }
            }
        });

        // Menu icon listener
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu();
            }
        });
    }


    /**
     * Registers change listener for shared preferences
     */
    private void setSharedPreferenceChangedListener() {
        prefs = new MySharedPreferences(this);

        prefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                if(key.equals(MySharedPreferences.HR_MAX_Y_AXIS)) {

                    chartHR.getAxisLeft().setAxisMaximum(Float.parseFloat(prefs.readData(key)));
                    chartHR.invalidate();
                }

                else if(key.equals(MySharedPreferences.HR_MIN_Y_AXIS)) {

                    chartHR.getAxisLeft().setAxisMinimum(Float.parseFloat(prefs.readData(key)));
                    chartHR.invalidate();
                }

                else if(key.equals(MySharedPreferences.SC_MAX_Y_AXIS)) {

                    chartSC.getAxisLeft().setAxisMaximum(Float.parseFloat(prefs.readData(key)));
                    chartSC.invalidate();

                }

                else if(key.equals(MySharedPreferences.SC_MIN_Y_AXIS)) {

                    chartSC.getAxisLeft().setAxisMinimum(Float.parseFloat(prefs.readData(key)));
                    chartSC.invalidate();
                }

            }
        };

        prefs.getSharedPreferences().registerOnSharedPreferenceChangeListener(prefChangeListener);
    }



    /**
     * Starts the monitoring services
     */
    private void startService() {
        Intent serviceIntent = new Intent(this, BioSignalReaderService.class);
        serviceIntent.putExtra("infoText", "Reading Bio Signal Data");
        ContextCompat.startForegroundService(this, serviceIntent);
        showToast("Monitoring started", Toast.LENGTH_SHORT);
    }


    /**
     * Stops monitoring service
     */
    private void stopService() {
        Intent serviceIntent = new Intent(this, BioSignalReaderService.class);
        stopService(serviceIntent);
        updateProgressBar(0, LOW_STRESS_COLOR);

        if(switchMonitoring.isChecked()) {
            switchMonitoring.setChecked(false);
        }
        
        updateStressLabel("Monitoring Stopped");
        showToast("Monitoring stopped", Toast.LENGTH_SHORT);
    }


    /**
     * Loads the fragment for setting custom Y axis values for HR chart
     *
     * @param fragment The fragment to load
     */
    private void loadSetHrMinMax(SetHrMinMax fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }


    /**
     * Loads the fragment for setting custom Y axis values for SC chart
     *
     * @param fragment The fragment to load
     */
    private void loadSetScMinMax(SetScMinMax fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit(); // save the changes
        
    }


    /**
     * Loads the fragment for setting custom read frequency for the monitoring service
     *
     * @param fragment The fragment to load
     */
    private void loadSetReadingFequency(SetReadingFrequency fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }


    /**
     * Stops the monitoring service, and clears all the chart entries.
     *
     * Also, resets the read position in the csv, which means the csv will be read from
     * the beginning next time the service starts.
     *
     * Finally, clears all the entries in the database as well.
     */
    private void resetAll() {
        if(switchMonitoring.isChecked()) {
            stopService();  // Stop the service
        }

        new MySharedPreferences(this).writeData(MySharedPreferences.SKIP_ROWS, "0");
        new DatabaseUtil(this).clearDatabase();

        chartHR.clear();
        chartSC.clear();
        entriesHR.clear();
        entriesSC.clear();
        initialiseChart();

        String clearDbMsg = "Database cleared";
        String restartServiceMsg = "Please restart monitoring";

        showToast(clearDbMsg, Toast.LENGTH_SHORT);
        showToast(restartServiceMsg, Toast.LENGTH_SHORT);
    }


    /**
     * Shows a toast on screen with the supplied message for the supplied duration.
     *
     * @param text Text to display on the toast
     * @param duration The duration in seconds the toast is visible for
     */
    private void showToast(String text, int duration) {
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }


    /**
     * Adds a new listener for the BioSignal schema in the database.
     * Every time a new BioSignal value is inserted in the database, the listener
     * will update the HR and SC charts
     *
     * @return false if no bioSignal results are are present in database, false otherwise
     */
    private boolean addBioSignalUpdateListener() {
        DatabaseUtil db = new DatabaseUtil(this);
        bioSignals = db.fetchAllBioSignals();

        if(bioSignals == null) {
            return false;
        }

        bioSignals.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<BioSignal>>() {
            @Override
            public void onChange(RealmResults<BioSignal> collection, OrderedCollectionChangeSet changeSet) {
                if(changeSet == null) {
                    return;
                }

                int[] insertions = changeSet.getInsertions();

                for(int index : insertions) {
                    BioSignal bioSignal = collection.get(index);
                    System.out.println("\nNew Biosignal: " + bioSignal);

                    setData(bioSignal);
                }
            }
        });

        return true;
    }



    /**
     * Adds a new listener for the StressData schema in database.
     * Everytime a new StressData value is inserted, the listener
     * will update the progress bar according to the classifiedv
     * value of the stress data.
     *
     * @return false if no stressData results are present in database, false otherwise
     */
    private boolean addStressChangeListener() {
        DatabaseUtil db = new DatabaseUtil(this);
        stressData = db.fetchAllStressData();

        if(stressData == null) {
            return false;
        }

        stressData.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<StressData>>() {
            @Override
            public void onChange(RealmResults<StressData> collection, OrderedCollectionChangeSet changeSet) {
                if(changeSet == null) {
                    return;
                }

                int[] insertions = changeSet.getInsertions();

                for(int index : insertions) {
                    StressData stressData = collection.get(index);
                    int stressLevel = stressData.getStressLevel();
                    System.out.println("\nNew Stress Data: " + stressData);

                    if(stressLevel == BioSignalReaderService.LOW_STRESS) {
                        updateProgressBar(LOW_STRESS_INDICATION, LOW_STRESS_COLOR);
                        updateStressLabel("Stress Level: LOW");
                    }

                    else if(stressLevel == BioSignalReaderService.MID_STRESS) {
                        updateProgressBar(MID_STRESS_INDICATION, MID_STRESS_COLOR);
                        updateStressLabel("Stress Level: MEDIUM");
                    }

                    else {
                        updateProgressBar(HIGH_STRESS_INDICATION, HIGH_STRESS_COLOR);
                        updateStressLabel("StressLevel: HIGH");
                    }
                }
            }
        });

        return true;
    }


    /**
     * Removes the change listeners in realm
     */
    private void removeRealmChangeListeners() {
        if(bioSignals != null) {
            bioSignals.removeAllChangeListeners();
        }

        if(stressData != null) {
            stressData.removeAllChangeListeners();
        }

    }


    /**
     * Shows the settings menu popup
     */
    public void showMenu(){
        builder = new AlertDialog.Builder(MainActivity.this);

        builder.setPositiveButton("Set Heart Rate Y Axis",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadSetHrMinMax(new SetHrMinMax());
                    }
                });

        builder.setNegativeButton("Set Skin Conductance Y Axis",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {
                        loadSetScMinMax(new SetScMinMax());
                    }
                });

        builder.setNeutralButton("Set bio signals reading frequency",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loadSetReadingFequency(new SetReadingFrequency());
                    }
                }
        );
        builder.create().show();
    }


    /**
     * Updates the progress bar according to the supplied value
     * and changes its according to the supplied color value.
     *
     * @param value Progress to set in the progressbar
     * @param color New color of the progressbar
     */
    private void updateProgressBar(int value, String color) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progStressLevel.setProgress(value, true);
        }
        else {
            progStressLevel.setProgress(value);
        }

        progStressLevel.getProgressDrawable()
                .setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
    }


    /**
     * Updates the label that displays the stress level
     * to display the latest classfied stress level
     *
     * @param text The text to display in the label
     */
    private void updateStressLabel(String text) {
        txtStressLabel.setText(text);
    }

    /**
     * Sets data to update graphs
     *
     * @param bioSignal - The Biosignal object
     *
     * @return void
     *
     * @author Palak, Kunal
     */
    private void setData(BioSignal bioSignal) {

        float index = entriesHR.size();

        // If number of entries are 60 or more
        // remove the first entry and decrement the x values
        // for the subsequent entries by 1.
        // Finally, fix the index to 59 to render new values at the end
        //
        // NOTE: Checking only HR entries for size, since both SC and HR update simultaneously
        if(entriesHR.size() >= 60) {

            entriesHR.remove(0);
            entriesSC.remove(0);

            for(int i = 0; i < entriesHR.size(); i++) {

                Entry hrEntry = entriesHR.get(i);
                Entry scEntry = entriesSC.get(i);
                float hrXValue = hrEntry.getX();
                float scXValue = scEntry.getX();
                hrEntry.setX(hrXValue - 1f);
                scEntry.setX(scXValue - 1f);

                entriesHR.set(i, hrEntry);
                entriesSC.set(i, scEntry);
            }

            index = 59f;

        }

        entriesHR.add(new Entry(index, Float.valueOf(bioSignal.getHeartRate())));
        entriesSC.add(new Entry(index, Float.valueOf(bioSignal.getSkinConductance())));

        dataSetHR = new LineDataSet(entriesHR, "Heart Rate"); // add entries to dataset
        dataSetSC = new LineDataSet(entriesSC, "Skin Conductance");

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSetHR); // add the data sets

        ArrayList<ILineDataSet> dataSetsSC = new ArrayList<>();
        dataSetsSC.add(dataSetSC); // add the data sets

        LineData lineDataHR = new LineData(dataSetHR);
        chartHR.setData(lineDataHR);
        chartHR.invalidate(); // refresh

        LineData lineDataSC = new LineData(dataSetSC);
        chartSC.setData(lineDataSC);
        chartSC.invalidate(); // refresh
    }



    /**
     * Initialises graphs
     * @author Palak
     */
    private void initialiseChart(){
        MySharedPreferences prefs = new MySharedPreferences(this);

        String hrMaxY = prefs.readData(MySharedPreferences.HR_MAX_Y_AXIS);
        String hrMinY = prefs.readData(MySharedPreferences.HR_MIN_Y_AXIS);
        String scMaxY = prefs.readData(MySharedPreferences.SC_MAX_Y_AXIS);
        String scMinY = prefs.readData(MySharedPreferences.SC_MIN_Y_AXIS);


        chartHR.setDrawGridBackground(false);
        chartHR.getAxisRight().setEnabled(false);
        chartHR.setMarker(markerViewHR);
        chartHR.setDragEnabled(true);

        chartSC.setDrawGridBackground(false);
        chartSC.getAxisRight().setEnabled(false);
        chartSC.setMarker(markerViewSC);
        chartSC.setDragEnabled(true);

        // Set Heart rate chart X axis
        XAxis xHR = chartHR.getXAxis();
        xHR.setAxisMaximum(60f);
        xHR.setAxisMinimum(0f);
        xHR.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Set Heart rate chart Y axis
        YAxis yHR = chartHR.getAxisLeft();
        yHR.setAxisMaximum(Float.parseFloat(hrMaxY));
        yHR.setAxisMinimum(Float.parseFloat(hrMinY));

        // Set Skin conductance chart X axis
        XAxis xSC = chartSC.getXAxis();
        xSC.setAxisMaximum(60f);
        xSC.setAxisMinimum(0f);
        xSC.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Set Skin conductance chart Y axis
        YAxis ySC = chartSC.getAxisLeft();
        ySC.setAxisMaximum(Float.parseFloat(scMaxY));
        ySC.setAxisMinimum(Float.parseFloat(scMinY));

        yHR.enableGridDashedLine(10f, 10f, 0f);
        xHR.setDrawLimitLinesBehindData(true);
        yHR.setDrawLimitLinesBehindData(true);

        ySC.enableGridDashedLine(10f, 10f, 0f);
        xSC.setDrawLimitLinesBehindData(true);
        ySC.setDrawLimitLinesBehindData(true);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
