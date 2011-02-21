package com.vroom;

//Import the list of constants from the constants class
import static com.vroom.Constants.TABLE_USERCODES;
import static com.vroom.Constants.userCode;
import static com.vroom.Constants.userVehicle;

import static com.vroom.Constants.TABLE_USERHISTORY;
import static com.vroom.Constants.historyId;
import static com.vroom.Constants.voltage;
import static com.vroom.Constants.temperature;
import static com.vroom.Constants.timestamp;

import static com.vroom.Constants.TABLE_USERVEHICLES;
import static com.vroom.Constants.vehicleId;

import static android.provider.BaseColumns._ID;

//Import the other libraries
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * The database helper class builds the database used for various user information.
 *
 * @author Neale Petrillo
 * @version 1, 2/20/2011
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	
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
    			+ historyId + " INTEGER, "
    			+ voltage + " DECIMAL(4), "
    			+ temperature + " DECIMAL(4), "
    			+ timestamp + " STRFTIME);");
    	
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
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    	db.execSQL("DROP DATABASE IF EXISTS " + DATABASE_NAME);
    	onCreate(db);
    }
}
