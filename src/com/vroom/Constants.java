package com.vroom;

import android.provider.BaseColumns;

public interface Constants extends BaseColumns {
//Start local database columns
    
//Start userCodes table	
	/**
	 * This defines the name of the table containing the list of codes specific to the user defined vehicles.
	 * 
	 * @author Neale Petrillo
	 * @version 1, 2/14/2011
	 */
	public static final String TABLE_USERCODES = "userCodes";
	
	/**
	 * This defines the name of the OBD Code column of the userCodes table
	 * 
	 *  @author Neale Petrillo
	 *  @version 1, 2/19/2011
	 */
	public static final String userCode = "code";
	
	/**
	 * This defines the name of the vehicle id column for the userCodes table.
	 * 
	 * This column acts as a reference to the user defined vehicle table.
	 * 
	 *  @author Neale Petrillo
	 *  @version 1, 2/19/2011
	 */
	public static final String userVehicle = "vehicleId";
	
//End userCodes table
//Start userVehicles table	
	/**
	 * This defines the name of the table containing the list of user vehicles.
	 * 
	 * @author Neale Petrillo
	 * @version 1, 2/14/2011
	 */
	public static final String TABLE_USERVEHICLES = "userVehicles";
	
	/**
	 * This defines the name of the vehicle identifier column
	 * 
	 * @author Neale Petrillo
	 * @version 1, 2/20/0211
	 */
	public static final String vehicleId = "identifier";
	
	/**
	 * Defines the name of the vehicle model column
	 * 
	 * @author Neale Petrillo
	 * @version 1, 3/6/2011
	 */
	public static final String vehicleModel = "model";
	
	/**
	 * Defines the name of the vehicle make column
	 * 
	 * @author Neale Petrillo
	 * @version 1, 3/6/2011
	 */
	public static final String vehicleMake = "make";
	
	/**
	 * Defines the name of the vehicle year column
	 * 
	 * @author Neale Petrillo
	 * @version 1, 3/6/2011
	 */
	public static final String vehicleYear = "year";
//End userVehicles table
//Start userHistory table	
	/**
	 * This defines the name of the table containing the user's vehicle history.
	 * 
	 * @author Neale Petrillo
	 * @version 1, 2/14/2011
	 */
	public static final String TABLE_USERHISTORY = "userHistory";
	
	public static final String historyId = "vehicleId";
	
	public static final String voltage = "voltage";
	
	public static final String temperature = "temperature";
	
	public static final String timestamp = "timestamp";
	
	public static final String rpm = "rpm";
	
	public static final String troubleCode = "troubleCode";
//End userHistory table

//Start external database information 
	public static final String getErrorInfoURL = "http://eclipse.wells.edu/~npetrillo/thesis/returnErrors.php";
//End external database information
	
//Start activity list data
	public static final int DEVICE_LIST_ACTIVITY_ID = 000001;
//End activity list data
	
	
	
}
