package com.vroom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

/**
 * BluetoothHelper is a class that runs AsynchTask objects for communicating with a Bluetooth device.
 * 
 *  This code is derived from http://programming-android.labs.oreilly.com/ch15.html
 *   
 * @author Neale Petrillo
 * @version 1, 3/3/2011
 * 
 * @see BluetoothHandler
 *
 * @throws none
 */
public class BluetoothHelper {

    private final String TAG = "BluetoothHelper";
    private static final boolean D = true;
    
    public enum State {
	NONE,
	LISTEN,
	CONNECTING,
	CONNECTED;
    }
    
    //Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothTest";
    
    //Unique UUID for this application 
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-100-8000-00805F9F34FB");
    
    //Member fields
    private final BluetoothAdapter mAdapter;
    private final BluetoothHandler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private State mState;
    private Context mContext;
    
    /**
     * Constructor. Prepares a new Bluetooth SPP session.
     * 
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothHelper(Context context, BluetoothHandler handler){
	if(D)Log.d(TAG, "Starting BluetoothHelper().");
	mContext = context;
	mAdapter = BluetoothAdapter.getDefaultAdapter();
	mState = State.NONE;
	mHandler = handler;
	if(D)Log.d(TAG, "Ending BluetoothHelper().");
    }
    //End BluetoothHelper
    
    /**
     * Set the current state of the session
     * 
     * @param state The current connection state
     */
    private synchronized void setState(State state){
	//Set the state
	if(D) Log.d(TAG, "Changing state from " + mState + " to " + state);
	mState = state;
	
	//Pass the new state to the Handler for updating
	mHandler.obtainMessage(BluetoothHandler.MessageType.STATE, -1, state).sendToTarget();
    }
    //End setState
    
    /**
     * Returns the currently stored connection state.
     * 
     */
    public synchronized State getState() {
	return mState;
    }
    //End getState
    
    /**
     * Starts a new communication session.Start AcceptThread to begin a session in listening mode. 
     * 
     * @author Neale Petrillo
     * @version 1, 3/3/2011
     */
    public synchronized void start() {
	if(D) Log.d(TAG, "Starting start().");
	
	//Cancel any thread attempting to make a connection
	if (mConnectThread != null) {
	    mConnectThread.cancel();
	    mConnectThread = null;
	}
	//End if
	
	//Set the state
	setState(State.LISTEN);
	if(D) Log.d(TAG, "Ending start().");
    }
    //End start
    
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * 
     * @author Neale Petrillo
     * @version 1, 3/3/2011
     * 
     * @param device The BluetoothDevice to connect with.
     */
    public synchronized void connect(BluetoothDevice device){
	if(D) Log.d(TAG, "Starting connect().");
	Log.v(TAG,"Attempting to connect to a device. " + device.toString());
	
	
	//Cancel any thread attempting to make a connection
	if(mState == State.CONNECTING){
	    if(mConnectThread != null){
		mConnectThread.cancel();
		mConnectThread = null;
	    }
	    //End if
	}
	//End if
	
	//Cancel any thread currently connected
	if(mConnectedThread != null){
	    mConnectedThread.cancel();
	    mConnectedThread = null;
	}
	//End if
	
	//Start the thread to connect with the given device
	mConnectThread = new ConnectThread(device);
	mConnectThread.start();
	setState(State.CONNECTING);
	if(D) Log.d(TAG, "Ending connect()");
    }
    //End connect
    
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection.
     * 
     * @author Neale Petrillo
     * @version 1, 3/3/2011
     * 
     * @param socket The BluetoothSocket on which the connection was made
     * @param device the BluetoothDevice that has been connected
     */
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
	if(D) Log.d(TAG, "Starting connected()");
	
	Log.v(TAG, "Canceling completed connections.");
	//Cancel the thread that completed the connection
	if(mConnectThread != null){
	    mConnectThread.cancel();
	    mConnectThread = null;
	}
	
	Log.v(TAG,"Canceling currently running connections.");
	//Cancel any thread currently running a connection
	if(mConnectedThread != null){
	    mConnectedThread.cancel();
	    mConnectedThread = null;
	}
	
	Log.v(TAG, "Starting connection thread.");
	//Start the thread to manage the connection and perform transmissions
	mConnectedThread = new ConnectedThread(socket);
	mConnectedThread.start();
	
	Log.v(TAG, "Sending the connected device name back to the UI activity.");
	//Send the name of the connected device back to the UI Activity
	mHandler.obtainMessage(BluetoothHandler.MessageType.DEVICE, -1, device.getName()).sendToTarget();
	setState(State.CONNECTED);
	
	if(D) Log.d(TAG, "Ending connected()");
    }
    //End connected
    
    /**
     * Stop all threads.
     */
    public synchronized void stop(){
	if(D) Log.d(TAG, "Starting stop()");
	
	if(mConnectThread != null) {
	    mConnectThread.cancel();
	    mConnectThread = null;
	}
	
	if(mConnectedThread != null) {
	    mConnectedThread.cancel();
	    mConnectedThread = null;
	}
	
	if(mAcceptThread != null){
	    mAcceptThread.cancel();
	    mAcceptThread = null;
	}
	setState(State.NONE);
	if(D) Log.d(TAG, "Ending stop()");
    }
    //End stop
    
    /**
     * Write to the ConnectedThread in an unsynchronized manner.
     * 
     * @author Neale Petrillo
     * @version 1, 3/3/2011
     * 
     * @param out The bytes to write
     * 
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out){
	if(D) Log.d(TAG, "Starting write().");
	
	Log.v(TAG, "Writing " + out.toString() + " to the connected thread.");
	ConnectedThread r;
	
	//Synchronize a copy of the ConnectedThread
	synchronized (this) {
	    if(mState != State.CONNECTED) {
		return;
	    }
	    else {
		r = mConnectedThread;
	    }
	    //End if/else
	}
	//End synchronized
	
	 //Perform the write unsynchronized
	 r.write(out);
	 if(D) Log.d(TAG, "Ending write().");
    }
    //End write
    
    /**
     * Sends an error message to the handler
     */
    @SuppressWarnings("unused")
    private void sendErrorMessage(int messageId) {
	setState(State.LISTEN);
	mHandler.obtainMessage(BluetoothHandler.MessageType.NOTIFY, -1, mContext.getResources().getString(messageId)).sendToTarget();
    }
    
    /**
     * Thread for listening for incoming connections. 
     * 
     * @author Neale Petrillo
     * @version 1
     */
    private class AcceptThread extends Thread {
	
	//The local server socket
	private final BluetoothServerSocket mmServerSocket;
	
	@SuppressWarnings("unused")
	public AcceptThread() {
	    if(D) Log.d(TAG, "Starting AcceptThread()");
	    BluetoothServerSocket temp = null;
	    
	    //Create a new listening server socket
	    try {
		temp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, SPP_UUID);
	    }
	    catch (IOException e){
		Log.e(TAG, "Error setting up listener thread. " + e.getMessage(), e.getCause());
	    }
	    //End try/catch
	    
	    mmServerSocket = temp;
	    if(D) Log.d(TAG, "Ending AcceptThread()");
	}
	//End AcceptTrhead
	
	public void run() {
	    if(D) Log.d(TAG, "Starting run().");
	    setName("AcceptThread");
	    BluetoothSocket socket = null;
	    
	    
	    //Listen to the server socket if we're not connected
	    while (mState != BluetoothHelper.State.CONNECTED){
		try{
		    Log.v(TAG, "Listening to the server socket...");
		    //This blocking call will only return on a successful connection or an exception
		    socket = mmServerSocket.accept();
		}
		catch (IOException e){
		    Log.e(TAG, "Failed to accept connection. " + e.getMessage(), e.getCause());
		    break;
		}
		//End try/catch
		
		//If a connection was accepted
		if(socket != null){
		    Log.v(TAG, "A connection was accepted!");
		    synchronized (BluetoothHelper.this){
			switch (mState){
			case LISTEN:
			case CONNECTING:
			    //Situation normal. Start the connection
			    connected(socket, socket.getRemoteDevice());
			    break;
			    
			case NONE:
			case CONNECTED:
			    //Either not ready or already connected. Terminate new socket.
			    try {
				socket.close();
			    }
			    catch(IOException e){
				Log.e(TAG, "Could not close unwanted socket. "+e.getMessage(), e.getCause());
			    }
			    //End try/catch
			    break;
			}
			//End switch
		    }
		    //End Synchronized
		}
		//End if
	    }
	    //End While
	    if(D) Log.i(TAG, "Ending run()");
	}
	//End run
	
	public void cancel() {
	    if(D) Log.d(TAG, "Canceling accptThread " + this);
	    try{
		mmServerSocket.close();
	    }
	    catch (IOException e){
		Log.e(TAG, "Unalbe to close server socket. "+e.getMessage(), e.getCause());
	    }
	    //End try/catch
	}
	//End cancel
    }
    //End AcceptThread
    
    /**
     * Thread that runs while attempting to make an outgoing connection with a device.
     * 
     *  It runs straight through; the connection either succeeds or fails.
     *
     * @author Neale Petrillo
     * @version 2, 3/3/2011
     */
    private class ConnectThread extends Thread {
	private final BluetoothSocket mmSocket;
	private final BluetoothDevice mmDevice;
	
	public ConnectThread(BluetoothDevice device){
	    Log.v(TAG, "Starting to create ConnectThread");
	    mmDevice = device;
	    BluetoothSocket temp = null;
	    
	    //Get a BluetoothSocket for a connection with the given BluetoothDevice
	    try {
		Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
	        temp = (BluetoothSocket) m.invoke(device, 1);
	    }
	    catch (Exception e) {
		Log.e(TAG, "Failed to create connected thread. "+e.getMessage(), e.getCause());
	    }
	    //End try/catch
	    mmSocket = temp;
	}
	//End ConnectThread
	
	public void run() {
	    Log.i(TAG, "BEGIN mConnectThread");
	    setName("ConnectedThread");
	    
	    //Always cancel discovery because it will slow down the connection
	    mAdapter.cancelDiscovery();
	    
	    //Make a connection to the BluetoothSocket
	    try{
		//This is a blocking call and will only return on a successful connection or an exception
		mmSocket.connect();
	    }
	    catch(IOException e){
		Log.e(TAG, "Unable to connect. "+e.getMessage(), e.getCause());
		mHandler.obtainMessage(BluetoothHandler.MessageType.NOTIFY, -1, "Error running the connect thread.");
		
		try{
		    mmSocket.close();
		}
		catch(IOException e2){
		    Log.e(TAG, "Unable to close socket during connection faiulre. "+e2.getMessage(),e2.getCause());   
		}
		//End try/catch
		//Start the service over to restart listening mode
		BluetoothHelper.this.start();
		return;
	    }
	    //End try/catch
	    
	   //Reset the ConnectThread because we're done
	    synchronized (BluetoothHelper.this){
		mConnectThread = null;
	    }
	    //End synchronized
	    
	    //Start the connected thread
	    connected(mmSocket, mmDevice);
	}
	//End run
	
	public void cancel() {
	    try{
		mmSocket.close();
	    }
	    catch (IOException e){
		Log.e(TAG, "Closing connect socket failed. "+e.getMessage(), e.getCause());
	    }
	    //End try/catch
	}
	//End cancel
    }
    //End ConnecThread
    
    /**
     * Thread that runs during a connection with a remote device. 
     * It handles all incoming and outgoing transmissions.
     * 
     * @author Neale Petrillo
     * @version 1, 3/3/2011
     */
    private class ConnectedThread extends Thread {
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	
	public ConnectedThread(BluetoothSocket socket){
	    Log.d(TAG, "Create ConnectedThread");
	    mmSocket = socket;
	    InputStream tmpIn = null;
	    OutputStream tmpOut = null;
	    
	    //Get the BluetoothSocket input and output streams
	    try {
		tmpIn = socket.getInputStream();
		tmpOut = socket.getOutputStream();
	    }
	    catch (IOException e){
		Log.e(TAG, "Temp sockets not created. "+e.getMessage(), e.getCause());
	    }
	    //End try/catch
	    
	    mmInStream = tmpIn;
	    mmOutStream = tmpOut;
	}
	//End ConnectedThread
	
	public void run(){
	    Log.i(TAG, "BEGIN mConnectedThread");
	    byte[] buffer = new byte[1024];
	    int bytes;
	    
	    //Keep listening to the InputStream while connected
	    while (true){
		try {
		    //Read from the InputStream
		    bytes = mmInStream.read(buffer);

		    //Send the obtained bytes to the UI Activity
		    mHandler.obtainMessage(BluetoothHandler.MessageType.READ, bytes, buffer).sendToTarget();

		}
		catch (IOException e){
		    Log.e(TAG, "Disconnected. "+e.getMessage(), e.getCause());
		    mHandler.obtainMessage(BluetoothHandler.MessageType.NOTIFY, -1, "Error running the connected thread.");
		    break;
		}
		//End try/catch
	    }
	    //End while
	}
	//End run
	
	/**
	 * Write to the connected OutStream.
	 * 
	 * @param buffer The bytes to write.
	 */
	public void write(byte[] buffer){
	    try{
		mmOutStream.write(buffer);
		
		//Share the sent message back to the UI Activity
		mHandler.obtainMessage(BluetoothHandler.MessageType.WRITE, -1, buffer).sendToTarget();
	    }
	    catch (IOException e){
		Log.e(TAG, "Exception during write. "+e.getMessage(), e.getCause());
	    }
	    //End try/catch
	}
	//End write
	
	public void cancel() {
	    try{
		mmSocket.close();
	    }
	    catch (IOException e){
		Log.e(TAG, "Closing the connect socket failed. "+e.getMessage(), e.getCause());
	    }
	    //End try/catch
	}
	//End cancel
    }
    //End ConnectedThread
}
//EndBluetoothHelper