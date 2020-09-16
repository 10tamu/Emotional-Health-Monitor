package com.swin.emotionalhealthmonitor.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.swin.emotionalhealthmonitor.R;
import com.swin.emotionalhealthmonitor.utils.MySharedPreferences;

public class SetReadingFrequency extends Fragment {
    Button save, cancel;
    View view;
    EditText txtReadFrequency;

    /**
     * Instantiates this view in the calling fragment
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_read_frequency_change, container, false);
        txtReadFrequency = view.findViewById(R.id.readFrequency);
        save = view.findViewById(R.id.readFrequencySave);
        cancel = view.findViewById(R.id.readFrequencyCancel);

        final MySharedPreferences prefs = new MySharedPreferences(getContext());
        txtReadFrequency.setText(prefs.readData(MySharedPreferences.READ_FREQUENCY));

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.writeData(
                        MySharedPreferences.READ_FREQUENCY,
                        txtReadFrequency.getText().toString());

                // Remove fragment
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .remove(SetReadingFrequency.this)
                        .commit();
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Remove fragment
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .remove(SetReadingFrequency.this)
                        .commit();
            }
        });

        return view;
    }
}
