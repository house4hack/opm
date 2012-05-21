package za.co.house4hack.opm;

import java.io.IOException;
import java.util.Date;

import org.microbridge.server.Server;
import org.microbridge.server.Client;
import org.microbridge.server.ServerListener;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Arduino {
	static String TAG = "Arduino";

	private int voltage;
	private Context mcontext;
	private static Server server = null;
	private double calibration = 50;


	public boolean setupArduino(Context context) {
		// Create TCP server
		mcontext = context;

		if (server == null) {
			try
			{
				server = new Server(7297);
	
		         server.addListener(new ServerListener() {
	
		             @Override
		             public void onServerStopped(Server server) {
		                Log.d(TAG, "server stopped");
		             }
	
		             @Override
		             public void onServerStarted(Server server) {
		                Log.d(TAG, "server started");
	
		             }
	
						@Override
						public void onReceive(org.microbridge.server.Client client, byte[] data)
						{
							int t = data[0];
							
			            	Log.d(TAG,"message received:"+t);
							
							if (data.length<2) return;
							
							voltage = (data[0] & 0xff) | ((data[1] & 0xff) << 8);
						};
	
		             @Override
		             public void onClientDisconnect(Server server, Client client) {
		                Log.d(TAG, "accessory disconnected");
	
		             }
	
					@Override
					public void onClientConnect(Server server, Client client) {
						// TODO Auto-generated method stub
		            	// Toast.makeText(mcontext, "Server connected", Toast.LENGTH_SHORT).show();
	                    Log.d(TAG,"Client connected"); 
						
					}
	
	
		          });
					server.start();
				return true;
			} catch (IOException e)
			{
				Log.e("microbridge", "Unable to start TCP server", e);
				
			}
			return false;
		} else {
			return true;
		}
		
	}
	
	public void refreshReadings(){
		try
		{
			voltage = -1; 
			server.send(new byte[] { (byte)'R', (byte)' ' });
		} catch (IOException e)
		{
			Log.e("microbridge", "problem sending TCP message", e);
			Toast.makeText(mcontext, e.getMessage(), Toast.LENGTH_SHORT).show();
		}	
	}
	
	public int readVoltage(){
	   	refreshReadings();
	   	Date startTime = new Date();
	   	while(voltage == -1){
	   		try {
				Thread.sleep(10);
				Date now = new Date();
				if(now.getTime() - startTime.getTime() > 3000) break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   		
	   	}
		return voltage;	   	
	}

	public void stop() {
         if(server != null) server.stop();	
         server = null;
	}

	public String lightOn() {
		try
		{
			server.send(new byte[] { (byte)'L', (byte)'1' });
			return "Success";
		} catch (IOException e)
		{
			Log.e("microbridge", "problem sending TCP message", e);
			return "Error:"+e.getMessage();
		}		}

	public String lightOff() {
		try
		{
			server.send(new byte[] { (byte)'L', (byte)'0' });
			return "Success";
		} catch (IOException e)
		{
			Log.e("microbridge", "problem sending TCP message", e);
			return "Error:"+e.getMessage();
		}	
	}

}
