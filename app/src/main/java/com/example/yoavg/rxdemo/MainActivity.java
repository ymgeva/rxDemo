package com.example.yoavg.rxdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.example.yoavg.rxdemo.examples.BackpreasureObservablesActivity;
import com.example.yoavg.rxdemo.examples.CombiningObservablesActivity;
import com.example.yoavg.rxdemo.examples.ErrorHandlingActivity;
import com.example.yoavg.rxdemo.examples.FilterObservablesActivity;
import com.example.yoavg.rxdemo.examples.SimpleObservablesActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);
        for (int i=0; i<buttons.getChildCount();i++) {
            View button = buttons.getChildAt(i);
            button.setOnClickListener(this);
        }
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

    @Override
    public void onClick(View v) {
        Object tagObj = v.getTag();
        if (tagObj == null || !(tagObj instanceof String)) {
            return;
        }
        String tag = (String)tagObj;
        Intent intent = null;

        if (tag.equals("simple")) {
            intent = new Intent(this,SimpleObservablesActivity.class);
        }
        else if (tag.equals("filters")) {
            intent = new Intent(this, FilterObservablesActivity.class);
        }

        else if (tag.equals("backpressure")) {
            intent = new Intent(this, BackpreasureObservablesActivity.class);
        }

        else if (tag.equals("errors")) {
            intent = new Intent(this, ErrorHandlingActivity.class);
        }

        else if (tag.equals("combine")) {
            intent = new Intent(this, CombiningObservablesActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
