package com.to.aboomy.statusbarsample;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final View viewById = findViewById(R.id.text);
        Log.e("aa", " --- " + viewById.getPivotY());

        int[] location = new int[2];
        viewById.getLocationInWindow(location);
        int x = location[0];
        int y = location[1];
        Log.e("aa", " --- x = " + x + " y " + y);


        viewById.post(new Runnable() {
            @Override
            public void run() {
                Log.e("aa", " --- " + viewById.getTop());

            }
        });

    }
}
