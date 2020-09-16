
package com.swin.emotionalhealthmonitor.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.swin.emotionalhealthmonitor.R;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Palak
 */

@SuppressLint("ViewConstructor")
public class CustomMarkerView extends MarkerView {

    private final TextView txtContent;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        txtContent = findViewById(R.id.txtContent);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;

            txtContent.setText(Utils.formatNumber(ce.getHigh(), 0, true));
        } else {

            txtContent.setText(Utils.formatNumber(e.getY(), 0, true));
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
