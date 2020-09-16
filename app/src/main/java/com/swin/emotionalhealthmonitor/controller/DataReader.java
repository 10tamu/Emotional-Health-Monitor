package com.swin.emotionalhealthmonitor.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.opencsv.CSVReader;
import com.swin.emotionalhealthmonitor.R;
import com.swin.emotionalhealthmonitor.model.BioSignal;
import com.swin.emotionalhealthmonitor.utils.MySharedPreferences;

/**
 * DataReader.java
 *
 * @author maheshx, Palak
 */
// Reads csv data and passes to UI thread
public class DataReader {

    private CSVReader reader;
    String heartRate, skinConductance;
    private BioSignal bioSignal; // Remove when do demo
    private InputStream is;

    /**
     * Reads a single row of data from the CSV file and converts the data to BioSignal object.
     * The number of lines specified in the skipRow parameter are skipped along with the header.
     * Returns null if skipRows is more than the number of records in the file
     *
     * @param context $msg - Activity Context
     * @param skipRows $msg - Number of rows to skip
     *                 
     * @return A BioSignal object created from the read data.
     * Null if skipRows has a value that is greater than the number of records in the file.
     * 
     * @author Palak, Kunal Pawar
     */
    public BioSignal readCSV(Context context, int skipRows) {

        try {

            is = context.getResources().openRawResource(R.raw.experimental_data1);
            Reader rd = new InputStreamReader(is);
            reader = new CSVReader(rd);
            reader.skip(skipRows + 1);  // Also skip the header
            String[] nextLine = reader.readNext();

            if(nextLine != null) {

                System.out.println("HR - SC " + nextLine[1] + " - " + nextLine[0]);
                heartRate = nextLine[1];
                skinConductance = nextLine[0];

                bioSignal = new BioSignal(heartRate, skinConductance, new Date());

                return bioSignal;
            }

            else {
                return null;
            }


        } catch(IOException e){
            System.out.println(e.getMessage());
        }
        return bioSignal;
    }


}
