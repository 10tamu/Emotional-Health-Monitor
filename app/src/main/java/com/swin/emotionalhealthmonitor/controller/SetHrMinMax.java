package com.swin.emotionalhealthmonitor.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.swin.emotionalhealthmonitor.R;
import com.swin.emotionalhealthmonitor.utils.MySharedPreferences;

import androidx.fragment.app.Fragment;

public class SetHrMinMax extends Fragment {


    Button save,cancel;
    View view;
    EditText min,max;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_set_hr_min_max, container, false);
        save = (Button) view.findViewById(R.id.save);
        min = (EditText) view.findViewById(R.id.hr_min);
        max = (EditText) view.findViewById(R.id.hr_max);

        MySharedPreferences obj = new MySharedPreferences(getActivity());

        min.setText(obj.readData(MySharedPreferences.HR_MIN_Y_AXIS));
        max.setText(obj.readData(MySharedPreferences.HR_MAX_Y_AXIS));

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySharedPreferences obj = new MySharedPreferences(getActivity());
                obj.writeData(MySharedPreferences.HR_MAX_Y_AXIS, max.getText().toString());
                obj.writeData(MySharedPreferences.HR_MIN_Y_AXIS, min.getText().toString());

                // Remove fragment
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .remove(SetHrMinMax.this)
                        .commit();

            }
        });
        cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .remove(SetHrMinMax.this)
                        .commit();
            }
        });

        return view;
    }
}