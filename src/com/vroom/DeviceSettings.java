package com.vroom;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.lang.Exception;

/**
 * PersonalSettings creates the personal settings directory under the options menu. 
 * <p>
 * The class and its methods are responsible for holding, updating, and providing the various device settings
 * for the program
 * 
 * @author Neale Petrillo
 * @version 1.0 2/9/2011
 *
 */

public class DeviceSettings extends PreferenceActivity {
	/** 
	 * TAG defines the class for debuggin purposes
	 * 
	 * @see PersonalSettings
	 */
	private static final  String TAG = "DeviceSettings";
	
	/**
	 * OPT_MAC is the key for the device_mac setting in xml/device_settings.xml
	 * 
	 * @see DeviceSettings
	 */
	private static final String OPT_MAC = "device_mac";
	
	/**
	 * OPT_MAC_DEF is the default value for the device_mac setting in xml/device_settings.xml if the value cannot be found.
	 * 
	 * @see DeviceSettings
	 */
	private static final String OPT_MAC_DEF = "";
	
	/**
	 * OPT_CONNECT is the key for the device_connect settings in xml/device_settings.xml
	 * 
	 * @see DeviceSettings
	 */
	private static final String OPT_CONNECT = "device_connect";
	
	/**
	 * OPT_CONNECT_DEF is the default value for the device_connect setting in xml/device_settings.xml if the value cannot be found.
	 */
	private static final boolean OPT_CONNECT_DEF = false;
	
	/**
	 * onCreate is called when the DeviceSettins button is clicked in the menu dialog. 
	 * <p>
	 * onCreate creates the DeviceSettings page using the buttons stored in the xml/device_settings.xml file.
	 * 
	 * @author Neale Petrillo
	 * @version 1.0 2/9/2011
	 * 
	 * @param savedInstanceState The previous instance state, type  Bundle
	 * @throws none
	 */
	//Start onCreate
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try{
			Log.v(TAG,"Trying to create the DeviceSettings page");
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.device_settings);
		}
		catch (Exception e){
			Log.e(TAG,"There was an error creating DeviceSettings. Exiting. "+e.getMessage(),e.getCause());
			finish();
		}
	}	
	//End onCreate
	
	/**
	 * getDeviceMac() returns the MAC address of the OBD-II interpreter on file.
	 * 
	 * @author Neale Petrillo
	 * @version 1.0 2/9/2011
	 * 
	 * @param context The context in which we're working, type Context
	 * @return String containing the MAC stored address
	 * @throws none 
	 */
	//Start getDeviceMac()
	public static String getDeviceMac(Context context){
		try {
			Log.v(TAG, "Trying to get the MAC address on file");
			return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_MAC,OPT_MAC_DEF);
		}
		catch (Exception e){
			Log.e(TAG, "Error while trying to get the stored MAC address. Exiting. "+e.getMessage(),e.getCause());
			return "";
		}
	}
	//End getDeviceMac()
	
	/**
	 * getConnect() returns true if the user wants to try to connect to the stored device or false otherwise. 
	 * 
	 * @author Neale Petrillo
	 * @version 1.0 2/9/2011
	 * 
	 * @param context The context in which we're working, type Context
	 * @return true if the user wants to automatically connect or false otherwise (or on failure)
	 * @throws none
	 */
	//Start getConnect()
	public static boolean getConnect(Context context){
		try{
			Log.v(TAG, "Trying to determine if the user wants to automatically connect");
			return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_CONNECT,OPT_CONNECT_DEF);
		}
		catch (Exception e){
			Log.e(TAG, "Error while trying to see if the user wants to automatically connect. Exiting. "+e.getMessage(),e.getCause());
			return false;
		}
	}
}
