package com.vroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class Vroom extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Set up click listeners for all the buttons
        View engineMonitorButton = findViewById(R.id.engine_monitor_button);
        engineMonitorButton.setOnClickListener(this);
        
        View repairButton = findViewById(R.id.repair_button);
        repairButton.setOnClickListener(this);
        
        View uploadButton = findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(this);
        
        View settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(this);
        
        View exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);
        
        
    }
    
    
    public void onClick(View v) {
    	switch (v.getId()) {
    	case R.id.engine_monitor_button:
    		Intent i = new Intent(this, Monitor.class);
    		startActivity(i);
    		break;
    		
    	case R.id.repair_button:
    		i = new Intent(this,Repair.class);
    		startActivity(i);
    		break;
    		
    	case R.id.upload_button:
    		i = new Intent(this, Upload.class);
    		startActivity(i);
    		break;
    	
    	case R.id.exit_button:
    		finish();
    		break;
    	
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.settings, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.settings:
    		startActivity(new Intent(this, Settings.class));
    		return true;
    	}
    	return false;
    }
}