package com.shopphukienxe.xechungtnhn;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by Zet on 2/25/2018.
 */

public class SearchLocationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

    }
    public void pickMaps(View view){
        Intent intent = new Intent(this,PickMapsActivity.class);
        startActivity(intent);
    }
}
