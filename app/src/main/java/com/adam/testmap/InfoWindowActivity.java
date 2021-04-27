package com.adam.testmap;


import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoWindowActivity implements GoogleMap.InfoWindowAdapter {

    private Activity context;
    private  String title="";
    private  String subtitle="";
    public InfoWindowActivity(Activity context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = context.getLayoutInflater().inflate(R.layout.activity_info_window,null,true);

        TextView tvTitle = (TextView) view.findViewById(R.id.title);
        TextView tvSubTitle = (TextView) view.findViewById(R.id.subtitle);

        title=marker.getTitle();
        System.out.println("------" + title + "--------");
        tvTitle.setText(title);
        tvSubTitle.setText(marker.getSnippet());


        return view;
    }
}