package com.vroom;

//Import the list of constants from the constants class
import static com.vroom.Constants.TABLE_USERCODES;
import static com.vroom.Constants.userCode;
import static com.vroom.Constants.userVehicle;

import static com.vroom.Constants.TABLE_USERHISTORY;
import static com.vroom.Constants.historyId;
import static com.vroom.Constants.voltage;
import static com.vroom.Constants.temperature;
import static com.vroom.Constants.troubleCode;
import static com.vroom.Constants.rpm;
import static com.vroom.Constants.timestamp;

import static com.vroom.Constants.TABLE_USERVEHICLES;
import static com.vroom.Constants.vehicleId;

import static android.provider.BaseColumns._ID;

//Import the other libraries
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



/**
 * The database helper class builds the database used for various user information.
 *
 * @author Neale Petrillo
 * @version 1, 2/20/2011
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	
    	private static final String TAG = "DatabaseHelper";
	private static final String DATABASE_NAME = "vroomInfo.db";
	private static final int DATABASE_VERSION = 1;

	/**
	 * Constructor for DatabaseHelper
	 * 
	 * Uses the SQLiteOpenHelper to create the database.
	 * 
	 * @author Neale Petrillo
	 * @version 1, 2/14/2011
	 * 
	 * @param context The context in which we're working. Type Context. 
	 * 
	 * @throws none
	 */
    DatabaseHelper (Context context){
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    /**
     * Called when the class is first created. Builds all the tables for the database.
     * 
     * @author Neale Petrillo
     * @version 1, 2011
     * 
     * @param db The database we're working on. Type SQLite Database
     * 
     * @throws none
     */
    @Override
    public void onCreate(SQLiteDatabase db){
    	//Create the USERCODES table
    	db.execSQL("CREATE TABLE " + TABLE_USERCODES + " (" 
    			+ _ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
    			+ userCode + " VARCHAR(10), " 
    			+ userVehicle + " INTEGER);");
    	
    	//Create the USERHISTORY table
    	db.execSQL("CREATE TABLE " + TABLE_USERHISTORY + " ("
    			+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
    			+ historyId + " TEXT, "
    			+ voltage + " FLOAT, "
    			+ temperature + " INTEGER, "
    			+ rpm + " INTEGER, "
    			+ troubleCode + " TEXT, "
    			+ timestamp + " TIMESTAMP DEFAULT NOT NULL current_timestamp);");
    	
    	//Create the USERVEHICLES table
    	db.execSQL("CREATE TABLE " + TABLE_USERVEHICLES + " ("
    			+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
    			+ vehicleId + " VARCHAR(45));");
    }
    
    /**
     * Called when the database is upgraded.
     * 
     * This will have to be modified for later versions. 
     * 
     * @author Neale Petrillo
     * @version 1, 2/15/2011
     * 
     * @param db the database we're working on. Type SQLiteDatabase.
     * @param oldVersion the previous version number of the database. Type int.
     * @param newVersion the new version number of the database. Type int.
     * 
     * @throws none
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    	db.execSQL("DROP DATABASE IF EXISTS " + DATABASE_NAME);
    	onCreate(db);
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
     * @return true on success, false on failure
     * @throws none
     */
    public Boolean updateHistory (SQLiteDatabase db, String vehicleId, int tempe, int revs, float volt, byte code){
	
	try{
	    //Build the sql string
	    String sql = "INSERT INTO " + TABLE_USERHISTORY 
			+ " (" + historyId + ","
			+ voltage + ","
			+ temperature + ","
			+ rpm + ","
			+ troubleCode + ") VALUES ("
			+ vehicleId + ","
			+ volt + ","
			+ tempe + ","
			+ revs + ","
			+ code + ");";
	    
	    //Execute the sql
	    db.execSQL(sql);
	    
	    return true;
	}catch (Exception e){
	    Log.e(TAG, "Unable to update history via updateHistory. "+e.getMessage(), e.getCause());
	    return false;
	}
    }
}
