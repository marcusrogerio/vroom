package com.vroom;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.lang.Exception;


/**
 * Vroom is the main program class of the Vroom app. It checks to make sure Bluetooth is working and builds
 * the buttons accordingly. 
 * 
 * @author Neale Petrillo
 * @version 1 2/9/2011
 **/
public class Vroom extends Activity implements OnClickListener{
	
	/**
	 * TAG defines the class name for debugging purposes
	 * 
	 * @see Vroom
	 * @see onCreate
	 */
	private static final  String TAG = "Vroom";
	
	/**
	 * Vroom class constructor
	 * <p>
	 * onCreate is called when the program is first run. It checks to see if Bluetooth is available then builds
	 * the main buttons accordingly. If Bluetooth is available all buttons are available otherwise the monitor and
	 * repair buttons are unclickable. 
	 * <p>
	 * The method makes sure that the onClick method is called when a button is clicked. Click actions are handled 
	 * by the onClick method. 
	 * 
	 *  @author Neale Petrillo
	 *  @version 1
	 * 	@see Vroom
	 * 	@see onClick
	 * 
	 * 	@param savedInstanceState The previous program state, type Bundle
	 * 	@throws none
	 * 
	 **/
	//Start onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//Start try/catch	
    	try {
    		Log.v(TAG, "Crating main layout");
            super.onCreate(savedInstanceState);
            
            //Set the content view
            setContentView(R.layout.main);
        
            /*
             * Check to see if the device has Bluetooth
             * If it does, create the connection and generate all the buttons
             * If it doesn't, make the buttons non-clickable 
             */
            Log.v(TAG, "Checking to see if Bluetooth is availiable");
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            
            //Start if/else
            if (mBluetoothAdapter != null) {
            	Log.v(TAG, "Bluetooth availiable, building all buttons");
            	
                //Set up click listeners for all the buttons
                View engineMonitorButton = findViewById(R.id.engine_monitor_button);
                engineMonitorButton.setOnClickListener(this);
                
                View repairButton = findViewById(R.id.repair_button);
                repairButton.setOnClickListener(this);
                
                View uploadButton = findViewById(R.id.upload_button);
                uploadButton.setOnClickListener(this);
                
                View exitButton = findViewById(R.id.exit_button);
                exitButton.setOnClickListener(this);
            }
            else
            {
            	Log.e(TAG, "Bluetooth not availiable! Loading reserve buttons");
            	
            	//Alert the user that Bluetooth isn't available
            	Toast.makeText(getApplicationContext(), "Bluetooth not enabled. Major functionality will be lost.", Toast.LENGTH_LONG).show();
                
                //Add click listeners for the remaining buttons
            	View uploadButton = findViewById(R.id.upload_button);
                uploadButton.setOnClickListener(this);
                
                View exitButton = findViewById(R.id.exit_button);
                exitButton.setOnClickListener(this);
                
                //Will be removed later
                View engineMonitorButton = findViewById(R.id.engine_monitor_button);
                engineMonitorButton.setOnClickListener(this);
            }
            //End if/else
    
    	}
    	catch (Exception e){
    		Log.e(TAG, "Major error occured during main screen loading. Closing the program");
    		finish();
    	}
    	//End try/catch
    }
    //End onCreate
    
	/**
	 * Method called when a view is clicked
	 * <p>
	 * onClick is called when a view (button) is clicked. It goes through a list of possible classes to call
	 * and calls the appropriate one based on the view's id.
	 * 
	 *  @author Neale Petrillo
	 *  @version 1
	 * 	@see Vroom
	 * 	@see onCreate
	 * 
	 * 	@param v The view that has been clicked, type View
	 * 	@throws none
	 * 
	 **/
    @Override
	public void onClick(View v) {
    	//Start try/catch
    	try {
        	Log.v(TAG,"A button was clicked; looking for its id");
        	
        	//Look for v's id in the list of known buttons. Issue an error message if you can't find it.
        	//Start switch
        	switch (v.getId()) {
        	case R.id.engine_monitor_button:
        		Log.v(TAG,"Monitor button pressed! Calling the Monitor.java");
        		Intent i = new Intent(this, Monitor.class);
        		startActivity(i);
        		break;
        		
        	case R.id.repair_button:
        		Log.v(TAG,"Repair button pressed! Calling the Repair.java");
        		i = new Intent(this,Repair.class);
        		startActivity(i);
        		break;
        		
        	case R.id.upload_button:
        		Log.v(TAG,"Upload button pressed! Calling the Upload.java");
        		i = new Intent(this, Upload.class);
        		startActivity(i);
        		break;
        	
        	case R.id.exit_button:
        		Log.v(TAG,"Exit button pressed! Calling finish()");
        		finish();
        		break;
        		
        	default:
        		Log.e(TAG,"No button found with " + v.getId());
        	
        	}
        	//End Switch
    		
    	}
    	catch (Exception e){
    		Log.e(TAG,"There was an exception with the onClick function: " + e.getMessage(),e.getCause());
    	}
    	//End try/catch
    }
    //End onClick
    
	/**
	 * Method called to create options menu
	 * <p>
	 * onCreateOptionsMenu when the user presses the menu button. It inflates the menu/settings.xml file. 
	 * 
	 *  @author Neale Petrillo
	 *  @version 1
	 * 	@see Vroom
	 * 	@see menu/settings.xml
	 * 
	 * 	@param menu The menu to inflate, type Menu
	 * 	@return true if menu is inflated otherwise returns false on error
	 * 	@throws none
	 * 
	 **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//Start try/catch
    	try {
        	super.onCreateOptionsMenu(menu);
        	MenuInflater inflater = getMenuInflater();
        	inflater.inflate(R.menu.settings, menu);
        	return true;
    	}
    	catch (Exception e){
    		Log.e(TAG, "Error occured when creating options menu: " + e.getMessage(), e.getCause());
    		return false;
    	}
    	//End try/catch
    }
    //End onCreateOptionsMenu
    
	/**
	 * Method called when options menu item is selected
	 * <p>
	 * onOptionsItemSelected takes a menu item and does the appropriate action based on the selection. Selections
	 * are based on the items listed in menu/settings.xml 
	 * 
	 *  @author Neale Petrillo
	 *  @version 1
	 * 	@see Vroom
	 * 	@see menu/settings.xml
	 * 
	 * 	@param item The item selected, type MenuItem
	 * 	@return true if activity is in the list otherwise returns false
	 * 	@throws none
	 * 
	 **/
    //Start onOptionsItemSelected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	try {
    	  	switch (item.getItemId()) {
        	case R.id.personal_settings:
        		startActivity(new Intent(this, PersonalSettings.class));
        		return true;
        		
        	case R.id.device_settings:
    			startActivity(new Intent(this, DeviceSettings.class));
    			return true;
        		
        	default:
        		Log.v(TAG, "Menu item selected was not found in the onOptionsItemsSelected method. Returning false");
        		return false;
        	}
    	}
    	catch (Exception e){
    		Log.e(TAG,"Error when selecting menu item: " + e.getMessage(), e.getCause());
    		return false;
    	}
  
    }
    //End onOptionsItemSelected
}