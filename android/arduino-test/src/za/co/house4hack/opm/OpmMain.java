package za.co.house4hack.opm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class OpmMain extends Activity {
    private Arduino arduino;


	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        arduino = new Arduino();
        arduino.setupArduino(this);
        
        
        
        
    }
    
    
    public void readVoltage(View v){
    	TextView tv = (TextView) findViewById(R.id.textview);
    	int voltage = arduino.readVoltage();
    	tv.setText("Voltage : "+ voltage);
    	
    }
    
    
    public void switchLED(View v){
    	arduino.lightOn();
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	arduino.lightOff();
    }
}