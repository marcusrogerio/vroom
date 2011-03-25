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
import static com.vroom.DeviceSettings.OPT_MAC_DEF;

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
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class Monitor extends Activity {

    //Debug information
    private final String TAG = "Monitor";
    
    /**
     * The database helper controls the connection between monitor and the database.
     */
    private DatabaseHelper history;
    /**
     * Bluetooth helper controls the Bluetooth communication
     */
    private BluetoothHelper device;
    
    /**
     * A string used to store output buffer.
     */
    private String outPutString;
    
    /**
     * Array adapter to push the outPutString
     */
    private ArrayAdapter<String> mConversationArrayAdapter;
    
    /**
     * View associated with he output
     */
    private ListView mConversationView;
    
    /**
     * List of control commands to send to the device.
     */
    private String controlCommands[] = {"E1","S0","@2"};
    
    /** 
     * List of OBD commands to send to the device.
     * <p>
     * Paired with obdTitles for easier processing. The order of the commands must match.
     */
    private String obdCommands[] = {"0105", "010C", "03" };
    
    /**
     * List of OBD titles for commands.
     * <p>
     * Paired with obdCommands for easier processing. The order of the commands must match.
     */
    private enum obdTitles {
	TEMPERATURE, 
	RPM, 
	TROUBLECODE;
    }
    
    private int oControlNumber = -1;
    private int runNumber;
    private String vehicleSerial;

    
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
		
		final Object savedData = getLastNonConfigurationInstance();
		
	        // Initialize the array adapter for the conversation thread
	        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
	        
	        //If there's saved conversation from a previous state load it
	        if(savedData != null){
	            String tmpArray[] = (String[])savedData;
	            
	            for(int i=0; i<tmpArray.length; i++)
	            {
	        	mConversationArrayAdapter.add(tmpArray[i]);
	            }//End for
	        }//End if
	        
	        mConversationView = (ListView) findViewById(R.id.monitor_update);
	        mConversationView.setAdapter(mConversationArrayAdapter);
		
		//Setup local variables
		history = new DatabaseHelper(this);
		device = new BluetoothHelper(this, handler);
		outPutString = "";
		runNumber = 0;
		vehicleSerial="";
	
	}
	//End onCreate
	    
	    private void updateRPM(String vehicleId, int revs){
		//Get the writable database
		Log.v(TAG, "Getting writeable database");
		SQLiteDatabase db = history.getWritableDatabase();
		
		try{
		    ContentValues values = new ContentValues();
		    values.put(historyId, vehicleId);
		    values.put(rpm, revs);
		    db.insertOrThrow(TABLE_USERHISTORY, null, values);
		    db.close();			    
		}
		catch (Exception e){
		    Log.e(TAG, "Unable to update history via updateRPM. "+e.getMessage(), e.getCause());
		}
		finally {
		    db.close();
		}
	    }//End updateRPM
	    
	    private void updateTEMP(String vehicleId, int tempe){
		//Get the writable database
		Log.v(TAG, "Getting writeable database");
		SQLiteDatabase db = history.getWritableDatabase();
		
		try {
		    ContentValues values = new ContentValues();
		    values.put(historyId, vehicleId);
		    values.put(temperature, tempe);
		    db.insertOrThrow(TABLE_USERHISTORY, null, values);
		    db.close();			    
		}
		catch (Exception e) {
		    Log.e(TAG, "Unable to update history via updateTEMP. "+e.getMessage(), e.getCause());
		    
		}
		finally {
		    db.close();
		}
	    }//End updateTEMP
	    
	    private void updateCode(String vehicleId, String code){
		//Get the writable database
		Log.v(TAG, "Getting writeable database");
		SQLiteDatabase db = history.getWritableDatabase();
		
		try{
		    ContentValues values = new ContentValues();
		    values.put(historyId, vehicleId);
		    values.put(troubleCode, code);
		    db.insertOrThrow(TABLE_USERHISTORY, null, values);
		    db.close();	
		}
		catch(Exception e){
		    Log.e(TAG, "Unable to update history via updateVOLT. "+e.toString(), e.getCause());
		}
		finally {
		    db.close();
		}
	    }
	    
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
	     * Method called when a child activity returns. 
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
	                
	                //Store the address in the settings
	                getPreferences(MODE_PRIVATE).edit().putString(OPT_MAC_DEF, address).commit();
	                
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
	      
	    
	    /**
	     * The handler that gets information back from the BluetoothHandler.
	     * <p>
	     * Determines message type then processes the results either through publishing a message or calling the process results function.
	     * 
	     * @author Neale Petrillo
	     * @version 4, 3/20/2011
	     */
	    private final BluetoothHandler handler = new BluetoothHandler() {
	        @Override
	        public void handleMessage(Message msg) {
	            
	            try{
	        	//Get the message type
	        	int messageType = msg.what;
	        	
	        	//Find the type of message it is
	        	if(messageType == BluetoothHandler.MessageType.STATE.ordinal()){   
	        	    try{
	        		String txt = msg.obj.toString();	
		        	mConversationArrayAdapter.add("Changing state to: "+txt + "\n");
	        	    }
	        	    catch(Exception e){
	        		Log.e(TAG, "Error notifying monitor UI. "+e.getMessage(), e.getCause());
	        		mConversationArrayAdapter.add("Error running the state routine.");
	        	    }
	        	    
	        	}else if (messageType == BluetoothHandler.MessageType.DEVICE.ordinal()){
	        	  
	        	    try{
	        		String txt = msg.obj.toString();
	        		mConversationArrayAdapter.add("Connected to "+txt + "\n");
	        		//Write something to get the process started
	        		device.write("\r".getBytes());
	        	    }
	        	    catch(Exception e){
	        		Log.e(TAG, "Exception running the device output. "+e.toString(), e.getCause());
	        		mConversationArrayAdapter.add("Error running the device routine.");
	        	    }
	        	    //End try/catch
	        	    
	        	}else if (messageType == BluetoothHandler.MessageType.NOTIFY.ordinal()){
	        	    try{
	        		String txt = msg.obj.toString();
		        	mConversationArrayAdapter.add(txt + "\n");
	        	    }
	        	    catch(Exception e){
	        		Log.e(TAG, "Exception while sending notify command. "+e.toString());
	        		mConversationArrayAdapter.add("Error running the notify routine.");
	        	    }
	        	    //End try/catch
	        	    
	        	}else if (messageType == BluetoothHandler.MessageType.READ.ordinal()){
	        	    //-------------------------------------------------------------------------------------------------------------------- Read Message
	        	    try{
		                    byte[] readBuf = (byte[]) msg.obj;
			               
		                    // Construct a string from the valid bytes in the buffer
		                    String readMessage = new String(readBuf, 0, msg.arg1);
		                    
		                    mConversationArrayAdapter.add("Recieved: "+readMessage);
		                    
		                    /*
		                     * They message will come in who knows what form with each part separated by \r
		                     * The standby character (>) comes in an irregular form with the \r first (\r>). This may not be true for all devices.
		                     * If the message does not end with a \r> or a >\r then we need to keep building the response. 
		                     * After we receive the character then we know the that we've found the end of the response and the device is waiting. 
		                     */
		                    outPutString  = outPutString + readMessage;
		                    
		                    if(readMessage.endsWith(">\r")||readMessage.endsWith("\r>")){
			                
		                	//Split the string into individual messages
			               String readMessages[] = outPutString.split("\\r");
			                   
			              //Call the handling function on each message.
			              for(int i = 0; i<readMessages.length; i++){
			        	  
			        	  //If the current message is not null or blank then we can handle it.
			        	  if(readMessages[i].length() > 0 && readMessages[i] != " "){
			        	      mConversationArrayAdapter.add("Handeling: "+readMessages[i]);
			        	      handleResponse(readMessages[i]);
			        	  }
			        	  //End if
			              }		
			              //End for
			              
			              //Reset the buffer
			              outPutString = "";
		                    }
		                    //End if
	        	    }
	        	    catch (Exception e){
	        		Log.e(TAG, "Error running the send/recieve routine. "+e.toString(),e.getCause());
	        		mConversationArrayAdapter.add("Error running the read routine.");
	        	    }
	        	    //End try/catch
	        	    
	        	}else if (messageType == BluetoothHandler.MessageType.WRITE.ordinal()){
	        	  //-------------------------------------------------------------------------------------------------------------------- Write Message
	        	    try{
	        		mConversationArrayAdapter.add("Writing: " + msg.obj.toString());
	        	    }
	        	    catch (Exception e){
	        		Log.e(TAG, "Error running the output routine. "+e.toString(),e.getCause());
	        		mConversationArrayAdapter.add("Error running the write routine.");
	        	    }
	        	    //End try/catch
	        	}else {
	        	    Log.e(TAG, "Unknown message type recieved.");
	        	}//End if/else
	            }
	            catch (Exception e){
	        	Log.e(TAG, "Error managing returned message in handleMessage. "+e.toString(), e.getCause());
	            }//End try/catch
	            
	        }//End handleMessage
	    };//End BluetoothHandler
	    
	    /**
	     * Sends the at command corresponding with ordinal value i.
	     * 
	     * @author Neale Petrillo
	     * @version 1, 3/10/2011
	     * 
	     * @param elmDevice The device to send the message to.
	     * @param i The AT command to send
	     */
	    private void sendAtCommand(BluetoothHelper elmDevice, int i){
		if(i < controlCommands.length && i >-1){
		    try{
			mConversationArrayAdapter.add("Sending AT command.");
			elmDevice.write(("AT"+controlCommands[i]+(char)0x000D).getBytes());
		    }
		    catch (Exception e){
			Log.e(TAG, "Unable to setup adapter.");
			mConversationArrayAdapter.add("Error sending AT command.");
		    }
		    //End try/catch
		}
		//End if		
	    }
	    //End sendAtCommand
	    
	    /**
	     * Sends the OBDII command corresponding to the ordinal value i.
	     * 
	     * @author Neale Petrillo
	     * @version 1, 3/10/2011
	     * 
	     * @param device The device to send the command to
	     * @param i The ordinal value of the command to send
	     */
	    private void sendOBDCommand(BluetoothHelper elmDevice, int i){
		try {
			if(i < obdCommands.length){
			    mConversationArrayAdapter.add("Sending OBD command.");
			    elmDevice.write((obdCommands[i] + (char)0x000D).getBytes());
			}
		}
		catch (Exception e){
		    Log.e(TAG, "Error sending OBD command. "+e.toString(), e.getCause());
		    mConversationArrayAdapter.add("Error sending OBD command.");
		}

	    }//End sendOBDCommand
	    
	    /**
	     * Handles a response from the device. 
	     * <p>
	     * Called by the handleMessage function whenever a newline character is detected (end of ELM device response). 
	     * Checks response for completeness and updates the history. 
	     * 
	     * @author Neale Petrillo
	     * @version 1, 3/13/2011
	     * 
	     * @param response Series of bytes received as an ELM response. 
	     */
	    private void handleResponse(String response){
		try {
		    Log.d(TAG, "Trying to handle ELM response");
		    
		    //Convert the response to a string of characters
		    String stringedResponse = response.trim();
		    
		    /** 
		     * The ELM chip will send a '>' when it is ready to receive. If this is the case then we can send the next OBD command or setup the device.
		     * 
		     * If we're just receiving a response then we need to process it. 
		     * 
		     * ELM OBD responses start with a 4. The next three bits identify the request that asked for the data. The rest of the bits are the requested data.
		     */
		    
		    //Check to see if the first character is a control character. If it does then it's ok to send the next code.
		    if(stringedResponse.equals(">")){
			
			//If it's the first run or all the ELM commands haven't been gone through we need to run them.
			if(runNumber < controlCommands.length){
			    sendAtCommand(device, runNumber);
			    runNumber = runNumber + 1;
			}
			else if(runNumber == controlCommands.length){
			    //Get the vehicle serial number
			    device.write("09025\r".getBytes());
			    runNumber = runNumber + 1;
			}
			else {
			    //Send the next code
			    oControlNumber = (oControlNumber + 1) % obdCommands.length;
			    sendOBDCommand(device, oControlNumber);			    
			}//End if/else
		    }
		    else if(stringedResponse.startsWith("4")){
			
			String resultCode = new String(stringedResponse.substring(1, 4));
			resultCode = "0"+resultCode;
			String resultData = stringedResponse.substring(4);
			
			//Determine the returned result
			if (resultCode.equals("0902")){
			    Log.d(TAG, "ELM response determined to be vehilce id. Converting and storing.");
			    vehicleSerial = resultData;
			    mConversationArrayAdapter.add("Vehicle ID: "+resultData);
			}
			else if(resultCode.equals(obdCommands[obdTitles.RPM.ordinal()])){
			    Log.d(TAG, "ELM response determined to be RPM. Converting and storing.");
			    
			    int rpm = Integer.parseInt(resultData, 16);
			    rpm = rpm / 4;
			    updateRPM(vehicleSerial, rpm);
			    mConversationArrayAdapter.add("RPM: "+rpm);
			}
			else if(resultCode.equals(obdCommands[obdTitles.TEMPERATURE.ordinal()])){
			    Log.d(TAG, "ELM response determined to be temerpature. Converting and storing.");
			    
			    int tempe = Integer.parseInt(resultData, 16);
			    tempe = tempe - 40;
			    updateTEMP(vehicleSerial, tempe);
			    mConversationArrayAdapter.add("TEMPERATURE: "+tempe);
			    
			}
			else if(resultCode.equals(obdCommands[obdTitles.TROUBLECODE.ordinal()])){
			    Log.d(TAG, "ELM response determined to be trouble code lookup. Converting and storing.");
			    
			    //Split the returned code into the three parts
			    String codes[] = {"","",""};
			    resultData = resultCode.substring(2) + resultData;
			    codes[0] = resultData.substring(0, 4);
			    codes[1] = resultData.substring(4, 8);
			    codes[2] = resultData.substring(8);
			    
			    for(int i = 0; i<3; i++){
				if(!codes[i].equals("0000")){
				    //Decode code
				    
				    
				}//End if
			    }//End for
			    updateCode(vehicleSerial, "0000");
			    mConversationArrayAdapter.add("CODE");
			    
			}
			//End if/else
		    }
		    else if(stringedResponse.equals("OK")){
			mConversationArrayAdapter.add("AT Command Sent Succesfully");
			
		    }
		    else if(stringedResponse.equals("0 DATA")){
			mConversationArrayAdapter.add("No Data For Command. Trying the next one.");
		    }
		    else if(stringedResponse.equals("UNABLE TO CONNECT")){
			mConversationArrayAdapter.add("Unable To Connect With ECU. Check Adapter And Reconnect.");
		    }
		    else if(stringedResponse.equals("?")){
			mConversationArrayAdapter.add("Unable to interpret last command. ");
		    }
		    else if(stringedResponse.equals("SEARCHING...")){
			mConversationArrayAdapter.add("Searching for data...");
		    }
		    else{
			mConversationArrayAdapter.add("Unknown Response recieved.");
		    }//End if/else
		}
		catch (Exception e){
		    Log.e(TAG, "Error while handling elm response. "+e.toString(), e.getCause());
		    
		}//End try/catch
	    }//End handleResponse
	    
	    /**
	     * Called when the monitor class is paused. Saves the current data displayed and stops the Bluetooth listeners.
	     * 
	     * @author Neale Petrillo
	     * @version 1, 3/21/2011
	     */
	    @Override
	    protected void onPause(){
		Log.d(TAG, "Pausing monitor.");
		super.onPause();
		
		//Stop the device to save battery power.
		device.stop();
		
		//Save the current data
	    }//End onPause
	    
	    /**
	     * Called when the monitor class is stopped. Stops the device.
	     * 
	     * @author Neale Petrillo
	     * @version 1, 3/21/2011
	     */
	    @Override
	    protected void onStop(){
		Log.d(TAG, "Stopping monitor.");
		super.onStop();
		
		//Stop the device to save battery power.
		device.stop();
	    }//End onStop
	    
	    /**
	     * Method called to save the current state of things during a runtime shift.
	     * 
	     * @author Neale Petrillo
	     * @version 1, 3/21/2011
	     */
	    @SuppressWarnings("null")
	    @Override
	    public Object onRetainNonConfigurationInstance(){
		String mConversationArray[] = null;
		
		
		//Get all the string objects in the conversation adapter
		for(int i = 0; i<mConversationArrayAdapter.getCount(); i++){
		    mConversationArray[i] = mConversationArrayAdapter.getItem(i);
		}//End for
	
		return mConversationArray;
	    }
		/**
		 * Method called to create the options menu
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
	     * Method called after the menu is first created. 
	     * <p>
	     * Updates the menu according to the state of the device. If a device is connected a disconnect button is displayed. 
	     * If a device isn't connected a connect button is displayed.
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
	    	    //End if/else
	    	    
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
	        	}//End switch
	    	}
	    	catch (Exception e){
	    		Log.e(TAG,"Error when selecting menu item: " + e.getMessage(), e.getCause());
	    		return false;
	    	}//End try/catch
	  
	    }
	    //End onOptionsItemSelected
}//End Monitor
