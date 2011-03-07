package com.vroom;

import static com.vroom.Constants.getErrorInfoURL;
import static com.vroom.Constants.TABLE_USERHISTORY;
import static com.vroom.Constants.timestamp;
import static com.vroom.Constants.troubleCode;
import static com.vroom.Constants.historyId;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class Repair extends Activity{
	private static String TAG = "Repair";
	private DatabaseHelper history;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.repair);
		
		//Get the local database
		history = new DatabaseHelper(this);
	}
	
	/**
	 * Sends a HTTP Get request to the server to lookup a given error code. 
	 * 
	 * @author Neale Petrillo
	 * @version 1, 3/6/2011
	 * 
	 * @param code The error code to be sent to the server.
	 * @param make The make of the user's car
	 * @param model The model of the user's car
	 * @param year The year of the user's car
	 * 
	 * @return jArray A JSON array containing a list of possible solutions to the user's problem. 
	 * 
	 */
	private JSONArray getSolution(String code, String make, String model, String year){
	    Log.v(TAG, "Trying to get the solution to the specified error code.");
	    
	    //Start he result string
	    String result = "";
	    InputStream inStream = null;
	    JSONArray jArray = null;
	    
	    //Build the data to send to the server
	    Log.v(TAG, "Building the required fields to pass.");
	    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    
	    nameValuePairs.add(new BasicNameValuePair("code",code));
	    nameValuePairs.add(new BasicNameValuePair("make",make));
	    nameValuePairs.add(new BasicNameValuePair("model",model));
	    nameValuePairs.add(new BasicNameValuePair("year",year));
	    
	    //Send request to the server
	    try {
		Log.v(TAG, "Trying to send the data to the server.");
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(getErrorInfoURL);
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		Log.v(TAG, "URL set, executing the query.");
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		Log.v(TAG, "Got the response. Getting content to process.");
		inStream = entity.getContent();
	    } catch(Exception e) {
		Log.e(TAG, "Unable to contact server. "+e.getMessage(), e.getCause());
	    }
	    //End try/catch
	    
	    //Convert response to a string
	    try {
		Log.v(TAG, "Trying to convert the response to a sting.");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream,"iso-8859-1"),8);
		StringBuilder builder = new StringBuilder();
		String line = null;
		
		Log.v(TAG, "Adding results to the return string.");
		//Read through the results and add it to the string builder
		while((line = reader.readLine()) != null){
		    builder.append(line + "\n");
		}
		//End while
		result = builder.toString();
		inStream.close();
		
	    }catch (Exception e){
		Log.e(TAG, "Unable to parse returned data. "+e.getMessage(), e.getCause());
	    }
	    //End try/catch
	    
	    //Convert the result to a JSONArray
	    try {
		Log.v(TAG, "Trying to convert the results into JSON.");
		jArray = new JSONArray(result);
	    }catch(JSONException e){
		Log.e(TAG, "Unable to convert to JSON. "+e.getMessage(), e.getCause());
	    }
	    //End try/catch
	    Log.v(TAG, "Returning results.");
	    return jArray; 
	}
	//End getSolution
	
	/**
	 * Retrieves the last error code from the local database. 
	 * 
	 * @author Neale Petrillo
	 * @version 1, 3/6/2011
	 * 
	 * @param vehicleId The id of the current vehicle.  
	 * 
	 * @return code A string defining the latest error code in the database. 
	 */
	private String getCode(String vehicleId) {
	   Log.v(TAG, "Getting most recent error code.");
	   SQLiteDatabase db = history.getReadableDatabase();
	   String toReturn = "";
	   
	    try {
		Log.v(TAG, "Trying to build the return info.");
		Cursor cursor = db.query(TABLE_USERHISTORY, new String[] {troubleCode}, historyId + " = " + vehicleId, null, null, null, timestamp + " DESC");
		startManagingCursor(cursor);
		
		Log.v(TAG, "Parsing the reutnred contents.");
		//If there's something to return, return it.
		if(cursor.getCount() > 0 ){
		    cursor.moveToFirst();
		    toReturn = cursor.getString(0);
		}
		Log.v(TAG, "Returning results.");
		//End if	
		return toReturn;
		
	    }catch (Exception e){
		Log.e(TAG, "Unable to get local history. Returning null. "+e.getMessage(), e.getCause());
		return null;
	    }
	    //End try/catch
	}
	//End getCode
}
