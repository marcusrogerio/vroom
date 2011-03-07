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
import static com.vroom.Constants.vehicleMake;
import static com.vroom.Constants.vehicleModel;
import static com.vroom.Constants.vehicleYear;

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
	private static final int DATABASE_VERSION = 2;

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
	
	 Log.v(TAG, "Database helper class sucessfully created. ");

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
	try {
	    	Log.v(TAG, "Creating database. UserCodes table being created. ");
	    	
	    	//Create the USERCODES table
	    	db.execSQL("CREATE TABLE " + TABLE_USERCODES + " (" 
	    			+ _ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
	    			+ userCode + " VARCHAR(10), " 
	    			+ userVehicle + " INTEGER);");
	    	
	    	Log.v(TAG, "UserCodes table craeted. Creating UserHistory table. ");
	    	
	    	//Create the USERHISTORY table
	    	db.execSQL("CREATE TABLE " + TABLE_USERHISTORY + " ("
	    			+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	    			+ historyId + " TEXT, "
	    			+ voltage + " FLOAT, "
	    			+ temperature + " INTEGER, "
	    			+ rpm + " INTEGER, "
	    			+ troubleCode + " TEXT, "
	    			+ timestamp + " TIMESTAMP NOT NULL DEFAULT current_timestamp);");
	    	
	    	Log.v(TAG, "UserHistory table created. Creating vehicle table. ");
	    	
	    	//Create the USERVEHICLES table
	    	db.execSQL("CREATE TABLE " + TABLE_USERVEHICLES + " ("
	    			+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
	    			+ vehicleId + " VARCHAR(45), " 
	    			+ vehicleMake + " VARCHAR(45), "
	    			+ vehicleModel + " VARCHAR(45), "
	    			+ vehicleYear + " INT(4));");
	}
	catch(Exception e){
	    Log.e(TAG, "Unable to create tables. "+e.getMessage(), e.getCause());
	    return;
	}
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
    	try {
    	    	Log.v(TAG, "Updating database. Destroying then calling the onCreate method.");
        	db.execSQL("DROP DATABASE IF EXISTS " + DATABASE_NAME);
        	onCreate(db);
        	
    	}catch (Exception e){
    	    Log.e(TAG, "Unable to update database. "+e.getMessage(), e.getCause());
    	    return; 
    	}
    }
}
