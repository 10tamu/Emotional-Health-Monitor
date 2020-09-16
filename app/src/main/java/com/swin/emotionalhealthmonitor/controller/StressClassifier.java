package com.swin.emotionalhealthmonitor.controller;

import android.content.Context;
import com.swin.emotionalhealthmonitor.R;
import com.swin.emotionalhealthmonitor.model.BioSignal;

import java.io.IOException;
import java.io.InputStream;

import biz.k11i.xgboost.Predictor;
import biz.k11i.xgboost.util.FVec;

public class StressClassifier {

    public static int getStressClassification(Context context, BioSignal bioSignal) {

        try {

            InputStream is = context.getResources().openRawResource(R.raw.stress_classifier);
            Predictor predictor =  new Predictor(is);

            double[] denseArray = {
                    Double.parseDouble(bioSignal.getSkinConductance()),
                    Double.parseDouble(bioSignal.getHeartRate())
            };

            FVec fVecDense = FVec.Transformer.fromArray(denseArray,true );

            double[] prediction = predictor.predict(fVecDense);


            return (int) prediction[0];

        } catch (IOException e) {
            System.out.println("----- EXCEPTION WHILE CLASSIFYING STRESS LEVEL -----");
            e.printStackTrace();

            return -1;
        }
    }
}
