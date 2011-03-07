package com.vroom;

import static android.provider.BaseColumns._ID;
import static com.vroom.Constants.TABLE_USERHISTORY;
import static com.vroom.Constants.historyId;
import static com.vroom.Constants.rpm;
import static com.vroom.Constants.temperature;
import static com.vroom.Constants.troubleCode;
import static com.vroom.Constants.voltage;
import static com.vroom.Constants.timestamp;
import static com.vroom.Constants.DEVICE_LIST_ACTIVITY_ID;

import com.vroom.BluetoothHelper;
import com.vroom.BluetoothHandler;

import com.vroom.DatabaseHelper;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class Monitor extends Activity implements OnClickListener{

    private final String TAG = "Monitor";
    private DatabaseHelper history;
    private BluetoothHandler handler;
    private BluetoothHelper device;
    /**
     * onCreate is called when the class is first created.
     * It sets the content view, prepares the database, builds the bluetooth helper, and sets up graphing. 
     * 
     * @author Neale Petrillo
     * @version 1, 3/3/2011
     * 
     * @see DatabaseHelper
     * @see Monitor
     * @see BluetoothHelper
     * 
     * @throws none
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    
	    	//Setup the views
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monitor);
		
		//Setup local variables
		history = new DatabaseHelper(this);
		handler = new BluetoothHandler();
		device = new BluetoothHelper(this, handler);
		
		//Set onClickListener for connect button
                View connectButton = findViewById(R.id.connect_button);
                connectButton.setOnClickListener(this);
	
	}
	//End onCreate

	    /**
	     * UpdateHistory updates one row of the history table. 
	     * 
	     * The UpdateHistory function is designed to take any number of arguments and update a history row as needed. 
	     * Any passed values that are not in use must be given a null value. The timestamp column is omitted because
	     * SQLite automatically updates it.
	     * 
	     * @author Neale Petrillo
	     * @version 1, 2/21/2011
	     * 
	     * @param db The SQLite database we're working with. Type SQLiteDatabase
	     * @param vehicleId The id of the current vehicle. Type String
	     * @param tempe The coolant temperature to be added to the vehicle history. Type int
	     * @param revs The current RPM to be added to the vehicle history. Type int
	     * @param volt The current voltage to be added to the vehicle history. Type float
	     * @param code The current trouble code to be added to the vehicle history. Type Byte
	     * 
	     * @throws none
	     */
	    private void updateHistory ( String vehicleId, int tempe, int revs, double volt, byte code){
		    //Get the writable database
		    Log.v(TAG, "Getting writeable database");
		    SQLiteDatabase db = history.getWritableDatabase();
		    
		try{
		    //Build the insertables
		    Log.v(TAG, "Building the insertable data");
		    ContentValues values = new ContentValues();
		    
		    //Insert values
		    Log.v(TAG, "Inserting values");
		    values.put(historyId, vehicleId);
		    values.put(temperature, tempe);
		    values.put(rpm, revs);
		    values.put(voltage, volt);
		    values.put(troubleCode, code);
		    
		    //Run the query
		    Log.v(TAG, "Running the query");
		    db.insertOrThrow(TABLE_USERHISTORY, null, values);
		    db.close();
		    
		}catch (Exception e){
		    Log.e(TAG, "Unable to update history via updateHistory. "+e.getMessage(), e.getCause());
		}finally {
		    db.close();
		}
		//End try/catch/finally
	    }
	    //End updateHistory
	    
	    
	    
	    /**
	     * getHistory retrieves the entire history stored in memory.
	     * 
	     * The returned cursor is ordered according to the recorded timestamp (most recent items first).
	     * 
	     * @author Neale Petrillo
	     * @version 1, 2/23/2011
	     * 
	     * @param vehicleId The id of the vehicle to be looked up
	     * @return A cursor with the database query results. There is no limit on the returned size.
	     */
	    private Cursor getHistory(String vehicleId){
		
		final String[] FROM = {_ID, historyId, rpm, temperature, troubleCode, voltage,timestamp,};
		final String ORDER_BY = timestamp + " DESC";
		
		
		SQLiteDatabase db = history.getReadableDatabase();
		Cursor cursor = db.query(TABLE_USERHISTORY, FROM, historyId + "=" + vehicleId, null, null, null, ORDER_BY);
		startManagingCursor(cursor);
		return cursor;

	    }
	    //End getHistory
	    
	    /**
	     * Method called when the connection manager button is pressed.
	     *<p>
	     *Starts the DeviceListActivity activity and begins the process of connecting to a Bluetooth device. 
	     *
	     *@author Neale Petrillo
	     *@version 1
	     *
	     *@param view The View that has been clicked.
	     */
	    @Override
	    public void onClick(View v) {
	    	try {
	        	Log.v(TAG,"A button was pressed. Locating button id.");
	        	
	        	//Look for v's id in the list of known buttons. Issue an error message if you can't find it.
	        	switch (v.getId()) {
	        	case R.id.connect_button:
	        		Log.v(TAG,"Connect Button pressed. Calling DeviceListActivity.java");
	        		Intent i = new Intent(this, DeviceListActivity.class);
	        		startActivityForResult(i, DEVICE_LIST_ACTIVITY_ID);
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
	     * Method called when a child activity is called. 
	     * <p>
	     * Most notably this function is called when the DeviceListActivity pairs a device.
	     * 
	     * @author Neale Petrillo
	     * @version 1, 3/6/2011
	     * 
	     * @param requestCode The code with which the child activity finished.
	     * @param resultCode
	     * @param data The data from the child.
	     * 
	     * @see DeviceListActivity
	     */
	     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 Log.v(TAG, "Recieved child activity result. Starting to parse results.");
		 
	         switch (requestCode) {
	         case DEVICE_LIST_ACTIVITY_ID:
	            Log.v(TAG, "Results orriginated form DeviceListActivity. Processing results.");
	            if(resultCode == RESULT_OK){
	        	Log.v(TAG, "The results are ok. Continuing processing.");
	        	//Try to build the device to connect with
	                // Get the device MAC address
	                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	                // Get the BluetoothDevice object
	                BluetoothDevice foreignDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
	                Log.v(TAG, "Trying to connect to the newly built device.");
	                //Pass the new device to the connect function
	                device.connect(foreignDevice);
	        	
	            }
	            else {
	        	Log.e(TAG, "There was an error when returning the result set. There should probably be some kind of error management here.");
	            }
	            //End if/else
	            
	            
	            break;
	            
	         default:
	             Log.e(TAG, "Unable to find correstponding child activity. Killing process.");
	             break;
	         }
	         //End switch
	     }
	     //End onActivityResult
}
