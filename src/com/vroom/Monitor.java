package com.vroom;

import static android.provider.BaseColumns._ID;
import static com.vroom.Constants.TABLE_USERHISTORY;
import static com.vroom.Constants.historyId;
import static com.vroom.Constants.rpm;
import static com.vroom.Constants.temperature;
import static com.vroom.Constants.troubleCode;
import static com.vroom.Constants.voltage;
import static com.vroom.Constants.timestamp;

import com.vroom.DatabaseHelper;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Monitor extends Activity {

    private final String TAG = "Monitor";
    private DatabaseHelper history;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monitor);
		
		history = new DatabaseHelper(this);
		
		//Tests
		Byte fFink = (0x0000);
		updateHistory("1234", 45, 4500, 4.8, fFink);
		Cursor thing = getHistory("1234");
		Log.v(TAG, "Got the query, starting to build the string");
		
		StringBuilder builder = new StringBuilder("Saved data:\n");
		
		Log.v(TAG, "Got the string, adding stuff");
		
		while(thing.moveToNext()){
		    Log.v(TAG, "Adding stuff to the string");
		
		    //_ID, temperature, rpm, voltage, troubleCode, historyId, timestamp
		    long id = thing.getLong(0);
		    int temp = thing.getInt(1);
		    int rpm = thing.getInt(2);
		    double volt = thing.getDouble(3);
		    String carId = thing.getString(4);
		    String time = thing.getString(5);
		    
		    builder.append(id).append("::");
		    builder.append(temp).append("::");
		    builder.append(rpm).append("::");
		    builder.append(volt).append("::");
		    builder.append(carId).append("::");
		    builder.append(time).append("::");
		}
		Log.v(TAG,"Printing the string");
		TextView text = (TextView) findViewById(R.id.monitorTest);
		text.setText(builder);
		
	}
	

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
	    }
	    
	    private Cursor getHistory(String vehicleId){
		
		//Open the database
		Log.v(TAG, "Getting the readable database");
		SQLiteDatabase db = history.getReadableDatabase();
		
		try{
			final String[] FROM = {_ID, temperature, rpm, voltage, troubleCode, historyId, timestamp,};
			final String ORDER_BY = timestamp + " DESC";
			
			Log.v(TAG, "Running the Query");
			//Perform a managed query. The Activity will handle closing and re-querying the cursor when needed.
			Cursor cursor = db.query(TABLE_USERHISTORY, FROM, historyId + " = '"+vehicleId+"'", null, null, null, ORDER_BY);
			startManagingCursor(cursor);
			Log.v(TAG, "Returning the query");
			return cursor;     
		}
		finally {
		    db.close();
		}
	    }
	    
	    /**
	     * getLatest gets the last stored data for the specified field.
	     * 
	     * getLatest returns a string or null if the field is not found or if there is no data. 
	     * 
	     * @author Neale Petrillo
	     * @version 1 2/22/2011
	     * 
	     * @param field the name of a field in the userhistory table.
	     * @param vehicleId the vehicle id for looking up
	     * @return string containing the latest entry for the field or null on error or no data found. 
	     */
	    private String getLatest(String field, String vehicleId){
		//Open the database
		SQLiteDatabase db = history.getReadableDatabase();
		
		try{
			final String[] FROM = {field,timestamp,};
			final String ORDER_BY = timestamp + " DESC";
			
			//Perform a managed query. The Activity will handle closing and re-querying the cursor when needed.
			Cursor cursor = db.query(TABLE_USERHISTORY, FROM, historyId + " = '" + vehicleId + "'", null, null, null, ORDER_BY, "1");
			startManagingCursor(cursor);
			
			//Get the single element from the cursor and convert it to a string then return it
			return cursor.getString(0);     
		}
		finally {
		    db.close();
		}
	    }
	    
	    
	    
}
