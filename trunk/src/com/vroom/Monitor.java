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
import com.vroom.BluetoothHelper.State;

import com.vroom.DatabaseHelper;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Monitor extends Activity {

    private final String TAG = "Monitor";
    private DatabaseHelper history;
    private BluetoothHelper device;
    /**
     * onCreate is called when the class is first created.
     * It sets the content view, prepares the database, builds the bluetooth helper, and sets up graphing. 
     * 
     * @author Neale Petrillo
     * @version 2, 3/3/2011
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
		device = new BluetoothHelper(this, handler);
		
	
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
	                
	        	//Hide the connect button if the device is connected
	                if(device.getState() == State.CONNECTED || device.getState() == State.CONNECTING){
	                    Log.d(TAG, "Connected to device.");	               

	                    
                    
	                }
	                else {
	                    Toast toast = Toast.makeText(this, "Unable to connect to device "+device.toString()+"\n Try reconnecting.", Toast.LENGTH_LONG);
	                    toast.show();
	                }
	                //End if/else

	                
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
	        	inflater.inflate(R.menu.menu, menu);
	        	
	        	if(device.getState() == State.CONNECTED || device.getState() == State.CONNECTING){
	        	    //Set the connect button invisible and the disconnect button visible
		    		menu.findItem(R.id.connect_device).setVisible(false);
		    		menu.findItem(R.id.disconnect_device).setVisible(true);
	        	}
	        	else{
		    		//Hide the disconnect button and show the connect button
		    		menu.findItem(R.id.connect_device).setVisible(true);
		    		menu.findItem(R.id.disconnect_device).setVisible(false);
	        	}
	        	//End if/else
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
	     * Method called after the menu is first created. Updates the menu according to the state of the device.
	     * 
	     * @author Neale Petrillo
	     * @version 1, 3/9/2011
	     */
	    @Override
	    public boolean onPrepareOptionsMenu(Menu menu){
	    	try {
	    	    
	    	    if(device.getState() == State.CONNECTED || device.getState() == State.CONNECTING){
	    		//Hide the connect button and show the disconnect button
	    		menu.findItem(R.id.connect_device).setVisible(false);
	    		menu.findItem(R.id.disconnect_device).setVisible(true);
	    		return true;
	    	    }
	    	    else{
	    		//Hide the disconnect button and show the connect button
	    		menu.findItem(R.id.connect_device).setVisible(true);
	    		menu.findItem(R.id.disconnect_device).setVisible(false);
	    		return true;
	    	    }
	    	    
	    	}
	    	catch (Exception e){
	    		Log.e(TAG, "Error occured when creating options menu: " + e.getMessage(), e.getCause());
	    		return false;
	    	}
	    	//End try/catch		
	    }//End onPrepareOptionsMenu
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
	        	
	        	case R.id.connect_device:
	        		Log.v(TAG,"Connect Button pressed. Calling DeviceListActivity.java");
	        		Intent i = new Intent(this, DeviceListActivity.class);
	        		startActivityForResult(i, DEVICE_LIST_ACTIVITY_ID);
	        		return true;
	        		
	        	case R.id.disconnect_device:
	        	    	device.stop();
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
	    
	    /**
	     * The handler that gets information back from the BluetoothHandler
	     */
	    private final BluetoothHandler handler = new BluetoothHandler() {
	        @Override
	        public void handleMessage(Message msg) {
	            
	            try{
	        	//Get the message type
	        	int messageType = msg.what;
	        	//Get the view to update
	        	TextView messageView = (TextView)findViewById(R.id.monitor_update);
	        	
	        	//Find the type of message it is
	        	if(messageType == BluetoothHandler.MessageType.STATE.ordinal()){   
	        	    String txt = msg.obj.toString();	
	        	    messageView.append("Changing state to: " + txt + "\n");
	        	    
	        	}else if (messageType == BluetoothHandler.MessageType.DEVICE.ordinal()){
	        	  
	        	    String txt = msg.obj.toString();
	        	    messageView.append("Connected to "+txt+"\n");
	        	    
	        	}else if (messageType == BluetoothHandler.MessageType.NOTIFY.ordinal()){
	        	    
	        	    String txt = msg.obj.toString();
	        	    messageView.append(txt+"\n");
	        	    
	        	}else if (messageType == BluetoothHandler.MessageType.READ.ordinal()){
	       
	                    byte[] readBuf = (byte[]) msg.obj;
	                    // Construct a string from the valid bytes in the buffer
	                    String readMessage = new String(readBuf, 0, msg.arg1);
	                    messageView.append(readMessage+"\n");
	        	    
	        	}else if (messageType == BluetoothHandler.MessageType.WRITE.ordinal()){
	        	    
	        	}else {
	        	    Log.e(TAG, "Unknown message type recieved.");
	        	}//End if/else
	            }
	            catch (Exception e){
	        	Log.e(TAG, "Error managing returned message in handleMessage. "+e.getMessage(), e.getCause());
	            }//End try/catch

	            
	            
	            
	        }//End handleMessage
	    };//End BluetoothHandler
	    
}//End Monitor
