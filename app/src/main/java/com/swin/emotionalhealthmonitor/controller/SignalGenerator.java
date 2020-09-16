//package com.swin.emotionalhealthmonitor.controller;
//
//import android.annotation.SuppressLint;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.graphics.DashPathEffect;
//import android.graphics.drawable.Drawable;
//import android.widget.SeekBar;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//import androidx.work.Constraints;
//import androidx.work.Data;
//import androidx.work.PeriodicWorkRequest;
//import androidx.work.WorkManager;
//
//import com.github.mikephil.charting.charts.LineChart;
//import com.github.mikephil.charting.components.Legend;
//import com.github.mikephil.charting.components.LimitLine;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.components.YAxis;
//import com.github.mikephil.charting.data.Entry;
//import com.github.mikephil.charting.data.LineData;
//import com.github.mikephil.charting.data.LineDataSet;
//import com.github.mikephil.charting.formatter.IFillFormatter;
//import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
//import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
//import com.github.mikephil.charting.utils.Utils;
//import com.opencsv.CSVReader;
//import com.swin.emotionalhealthmonitor.R;
//import com.swin.emotionalhealthmonitor.views.MyMarkerView;
//
//import java.util.ArrayList;
//import java.util.concurrent.TimeUnit;
//
//public class SignalGenerator extends AppCompatActivity{
//
//    private LineChart chartHR;
//    private LineChart chartSC;
//    private WorkManager objWorkManager;
//    private PeriodicWorkRequest.Builder builder;
//    private PeriodicWorkRequest workRequest;
//    private CSVReader reader;
//    private SharedPreferences sp;
//    private SignalGenerator signalGenerator;
//    private String ROW_COUNTER = "Counter";
//    private int counter = 0;
//    private SeekBar seekBarX, seekBarY;
//    private  Drawable drawableHR, drawableSC;
//
//
//
//
//    public void initializeUI(LineChart chartHR, LineChart chartSC, MyMarkerView mvHR, MyMarkerView mvSC, Drawable drawableHR, Drawable drawableSC){
//        //TODO : Blutooth progress, add menu item
//
////        seekBarX = findViewById(R.id.seekBar1);
//
////        seekBarY = findViewById(R.id.seekBar2);
//
//        {   // // Chart Style // //
////            chartHR = findViewById(R.id.chart1);
////            chartSC = findViewById(R.id.chart2);
//
//            // background color
//            chartHR.setBackgroundColor(Color.WHITE);
//            chartSC.setBackgroundColor(Color.WHITE);
//
//            // disable description text
//            chartHR.getDescription().setEnabled(false);
//            chartSC.getDescription().setEnabled(false);
//
//            // enable touch gestures
//            chartHR.setTouchEnabled(true);
//            chartSC.setTouchEnabled(true);
//
//            // set listeners
//            chartHR.setDrawGridBackground(false);
//            chartSC.setDrawGridBackground(false);
//
//            // create marker to display box when values are selected
//
//
//
//            //Set the marker to the chartHR
//            mvHR.setChartView(chartHR);
//            mvSC.setChartView(chartSC);
//            chartHR.setMarker(mvHR);
//            chartSC.setMarker(mvSC);
//
//            // enable scaling and dragging
////            chartHR.setDragEnabled(true);
//            chartHR.setScaleEnabled(true);
////            chartSC.setDragEnabled(true);
//            chartSC.setScaleEnabled(true);
//
//            // force pinch zoom along both axis
//            chartHR.setPinchZoom(true);
//            chartSC.setPinchZoom(true);
//        }
//
//        XAxis xAxisHR,xAxisSC;
//        {   // // X-Axis Style // //
//            xAxisHR = chartHR.getXAxis();
//            xAxisSC = chartSC.getXAxis();
//
//            // vertical grid lines
//            xAxisHR.enableGridDashedLine(10f, 10f, 0f);
//            xAxisSC.enableGridDashedLine(10f, 10f, 0f);
//
//            // axis range
//            xAxisHR.setAxisMaximum(60f);
//            xAxisSC.setAxisMaximum(60f);
//            xAxisHR.setPosition(XAxis.XAxisPosition.BOTTOM);
//            xAxisSC.setPosition(XAxis.XAxisPosition.BOTTOM);
//            xAxisHR.setAxisMinimum(0f);
//            xAxisSC.setAxisMinimum(0f);
//        }
//
//        YAxis yAxisHR,yAxisSC;
//        {   // // Y-Axis Style // //
//            yAxisHR = chartHR.getAxisLeft();
//            yAxisSC = chartSC.getAxisLeft();
//
//            // disable dual axis (only use LEFT axis)
//            chartHR.getAxisRight().setEnabled(false);
//            chartSC.getAxisRight().setEnabled(false);
//
//            // horizontal grid lines
//            yAxisHR.enableGridDashedLine(10f, 10f, 0f);
//            yAxisSC.enableGridDashedLine(10f, 10f, 0f);
//
//            // axis range
//            yAxisHR.setAxisMaximum(85f);
//            // yAxis.setAx
//            yAxisSC.setAxisMaximum(13f);
//            yAxisHR.setAxisMinimum(65f);
//            yAxisSC.setAxisMinimum(7f);
//
//        }
//        {   // // Create Limit Lines // //
//            LimitLine llXAxisHR = new LimitLine(9f, "Index 10");
//            LimitLine llXAxisSC = new LimitLine(9f, "Index 10");
//
//            llXAxisHR.setLineWidth(4f);
//            llXAxisSC.setLineWidth(4f);
//
//            llXAxisHR.enableDashedLine(10f, 10f, 0f);
//            llXAxisSC.enableDashedLine(10f, 10f, 0f);
//
//            llXAxisHR.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//            llXAxisSC.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//
//            llXAxisHR.setTextSize(10f);
//            llXAxisSC.setTextSize(10f);
//
//            LimitLine ll1HR = new LimitLine(150f, "Upper Limit");
//            LimitLine ll1SC = new LimitLine(150f, "Upper Limit");
//
//            ll1HR.setLineWidth(4f);
//            ll1SC.setLineWidth(4f);
//            ll1HR.enableDashedLine(10f, 10f, 0f);
//            ll1SC.enableDashedLine( 10f,10f, 0f);
//            ll1HR.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
//            ll1SC.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
//            ll1HR.setTextSize(10f);
//            ll1SC.setTextSize(10f);
//
//            LimitLine ll2HR = new LimitLine(-30f, "Lower Limit");
//            LimitLine ll2SC = new LimitLine( -30f,"Lower Limit");
//            ll2HR.setLineWidth(4f);
//            ll2SC.setLineWidth(4f);
//            ll2HR.enableDashedLine(10f, 10f, 0f);
//            ll2SC.enableDashedLine( 10f,10f,0f );
//            ll2HR.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//            ll2SC.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//            ll2HR.setTextSize(10f);
//            ll2SC.setTextSize(10f);
//
//            // draw limit lines behind data instead of on top
//            yAxisHR.setDrawLimitLinesBehindData(true);
//            yAxisSC.setDrawLimitLinesBehindData(true);
//            xAxisHR.setDrawLimitLinesBehindData(true);
//            yAxisSC.setDrawGridLinesBehindData(true);
//
//            // add limit lines
//            yAxisHR.addLimitLine(ll1HR);
//            yAxisHR.addLimitLine(ll2HR);
//            yAxisSC.addLimitLine(ll1SC);
//            yAxisSC.addLimitLine(ll2SC);
//        }
//
//        // add data
////        seekBarX.setProgress(70);
////        seekBarY.setProgress(180);
//        setData(100, drawableHR, drawableSC);
//
////        chartHR.animateX(30000);
////        chartSC.animateX(30000);
//
//        // get the legend (only possible after setting data)
//        Legend lHR = chartHR.getLegend();
//        Legend lSC= chartSC.getLegend();
//        // draw legend entries as lines
//        lHR.setForm(Legend.LegendForm.LINE);
//        lSC.setForm(Legend.LegendForm.LINE);
//
////        handler.post(runnableCode);
//    }
//
////    public void hehe(){
////    }
//
//    // Starting execution for work manager
//    public void initialieWorkManager(){
//        System.out.println("Called");
//        objWorkManager = WorkManager.getInstance();
//
////        sp = getSharedPreferences(ROW_COUNTER, Context.MODE_PRIVATE);
////        SharedPreferences.Editor editor = sp.edit();
////        editor.putInt("counter", 0);
////        editor.apply();
//
//        builder = new PeriodicWorkRequest.Builder(PeriodicWorkManager.class, 30, TimeUnit.SECONDS).setInputData(createInputDataForUri()).addTag("periodicWorkRequest");
//        builder.setConstraints(Constraints.NONE);
//        workRequest = builder.build();
//        objWorkManager.enqueue(workRequest);
//    }
//
//
//
//    // Binds data
//    @SuppressLint("RestrictedApi")
//    private Data createInputDataForUri() {
//        Data.Builder builder = new Data.Builder();
//        builder.putInt(ROW_COUNTER, counter);
//        builder.build();
//        return builder.build();
//    }
//
//    private void setData(int count, Drawable drawableHR, Drawable drawableSC) {
//
//        objWorkManager = WorkManager.getInstance();
//        builder.setConstraints(Constraints.NONE);
//        workRequest = builder.build();
//        objWorkManager.enqueue(workRequest);
//    }
//
//    public void setGraph( ArrayList<Entry> valuesHR,  ArrayList<Entry> valuesSC){
//        LineDataSet set1;
//        LineDataSet set2;
//
//        if (chartHR.getData() != null &&
//                chartHR.getData().getDataSetCount() > 0 && chartSC.getData() != null && chartSC.getData().getDataSetCount() >0) {
//
//            System.out.println(" ----------------------------------Somewhere inside in this stupid world trying to escape........");
//
//            set1 = (LineDataSet) chartHR.getData().getDataSetByIndex(0);
//            set2 = (LineDataSet) chartSC.getData().getDataSetByIndex(0);
//            set1.setValues(valuesHR);
//            set2.setValues(valuesSC);
//            set1.notifyDataSetChanged();
//            set2.notifyDataSetChanged();
//            chartHR.getData().notifyDataChanged();
//            chartSC.getData().notifyDataChanged();
//            chartHR.notifyDataSetChanged();
//            chartSC.notifyDataSetChanged();
//        } else {
//            // create a dataset and give it a type
//
//            set1 = new LineDataSet(valuesHR, "DataSet 1");
//            set2 = new LineDataSet( valuesSC, "DataSet 2");
//            set1.setDrawIcons(false);
//            set2.setDrawIcons(false);
//            // draw dashed line
//            set1.enableDashedLine(10f, 5f, 0f);
//            set2.enableDashedLine( 10f,5f,0f );
//
//            // black lines and points
//            set1.setColor(Color.BLACK);
//            set2.setColor(Color.BLACK);
//
//            set1.setCircleColor(Color.BLACK);
//            set2.setCircleColor(Color.BLACK);
//
//            // line thickness and point size
//            set1.setLineWidth(1f);
//            set2.setLineWidth(1f);
//            set1.setCircleRadius(3f);
//            set2.setCircleRadius(3f);
//
//            // draw points as solid circles
//            set1.setDrawCircleHole(false);
//            set2.setDrawCircleHole(false);
//
//            // customize legend entry
//            set1.setFormLineWidth(1f);
//            set2.setFormLineWidth(1f);
//
//            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
//            set2.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
//
//            set1.setFormSize(15.f);
//            set2.setFormSize(15.f);
//
//            // text size of values
//            set1.setValueTextSize(9f);
//            set2.setValueTextSize(9f);
//
//            // draw selection line as dashed
//            set1.enableDashedHighlightLine(10f, 5f, 0f);
//            set2.enableDashedHighlightLine(10f, 5f, 0f);
//
//            // set the filled area
//            set1.setDrawFilled(true);
//            set2.setDrawFilled(true);
//
//            set1.setFillFormatter(new IFillFormatter() {
//                @Override
//                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
//                    return chartHR.getAxisLeft().getAxisMinimum();
//                }
//            });
//            set2.setFillFormatter(new IFillFormatter() {
//                @Override
//                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
//                    return chartSC.getAxisLeft().getAxisMinimum();
//                }
//            });
//
//            // set color of filled area
//            if (Utils.getSDKInt() >= 18) {
//                // drawables only supported on api level 18 and above
//                // TODO: change color from here...maybe
//                drawableHR = ContextCompat.getDrawable(this, R.drawable.fade_red);
//                drawableSC = ContextCompat.getDrawable(this, R.drawable.fade_yellow);
//                set1.setFillDrawable(drawableHR);
//                set2.setFillDrawable(drawableSC);
//            } else {
//                set1.setFillColor(Color.BLACK);
//                set2.setFillColor(Color.BLACK);
//            }
//
//            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//            ArrayList<ILineDataSet> dataSetsSC = new ArrayList<>();
//
//            dataSets.add(set1); // add the data sets
//            dataSetsSC.add(set2);
//
//            // create a data object with the data sets
//            LineData data = new LineData(dataSets);
//            LineData dataSC = new LineData(dataSetsSC);
//
//            // set data
//            chartHR.setData(data);
//            chartSC.setData(dataSC);
//        }
//    }
//}
