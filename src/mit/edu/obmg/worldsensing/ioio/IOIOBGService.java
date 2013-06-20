/**
 * Written by Santiago Alfaro and Micah Rye
 */

package mit.edu.obmg.worldsensing.ioio;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import mit.edu.obmg.worldsensing.MainWorldSensing;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class IOIOBGService extends IOIOService{
	private boolean mIOIOConnected = false;
	private DigitalOutput mLED;
	private Handler UIHandler; 
	private Context context;
	public MainWorldSensing activity;
	private final String TAG = "IOIOBG";

	//Sonar
	private final int mSonar_PIN = 40;
	private AnalogInput mSonar_Input;
	float mSonar_Reading;

	//Color
	int	rX = 34;
	int tX = 35;
	Uart mColor_uart = null;
	InputStream mColor_in = null;
	OutputStream mColor_out= null;
	byte[] mColor_buffer = new byte[1];	
	String rdata;
	int data[] = new int[24];
	int data2[] = new int[24];
	String input;
	int[] rgb;
	String inputstring = "";	//Data from PC
	String sensorstring = "";	//Data from sensor
	boolean input_stringcomplete = false;	//is PC Data done?
	boolean sensor_stringcomplete = false;	//is sensor Data done?
	char inchar;


	@Override
	protected IOIOLooper createIOIOLooper() {

		UIHandler = new Handler();

		return new BaseIOIOLooper() {

			@Override
			protected void setup() throws ConnectionLostException,
			InterruptedException {
				mIOIOConnected = true;
				mLED = ioio_.openDigitalOutput(0, true);
				mSonar_Input = ioio_.openAnalogInput(mSonar_PIN);

				Log.i(TAG," :|   Open UART");
				mColor_uart = ioio_.openUart(tX, rX, 38400, Uart.Parity.NONE, Uart.StopBits.ONE);	
				mColor_in = mColor_uart.getInputStream();
				mColor_out = mColor_uart.getOutputStream();	
				Log.d(TAG, "Reading");
				Log.i(TAG," :)   UART OK");
			}


			@Override
			public void loop() throws ConnectionLostException, InterruptedException {
				mSonar_Reading = mSonar_Input.read();

				try {
					inchar = (char)mColor_in.read();
					if ((int)inchar == 13) sensor_stringcomplete = true;
					else sensorstring += inchar;

					if (sensor_stringcomplete){
						Log.i(TAG, "full value "+ sensorstring);
						handleColor(sensorstring);
						sensorstring = "";
						sensor_stringcomplete = false;
					}
					Thread.sleep(100);

				} catch (Exception e) {
					e.printStackTrace();
				}


				//Handler to send data back to Main
				Runnable runnable = new Runnable(){
					public void run(){
						UIHandler.post(new Runnable(){
							public void run(){
								MainWorldSensing.getSonar(mSonar_Reading);
								if (rgb != null) MainWorldSensing.getColor(rgb);
								//Vibrator_Value.setText("Value: "+Vibrator_Pulse);
							}
						});
					}
				};
				new Thread(runnable).start();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					ioio_.disconnect();
				}
			}
		};
	}



	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	public IBinder mBinder = new LocalBinder();

	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	public class LocalBinder extends Binder{

		public IOIOBGService getServerInstance() {
			return IOIOBGService.this;
		}
	}

	public boolean isConnected(){
		return this.mIOIOConnected;
	}

	public void setLED(boolean state) {
		if(mIOIOConnected){
			try{
				mLED.write(state);
			}catch(ConnectionLostException e){
				e.printStackTrace();
				mIOIOConnected = false;
			}
		}
	}

	private int[] handleColor(String sensorstring){
		String[] strArray = sensorstring.split(",");
		rgb = new int [strArray.length];
		for(int i=0; i<strArray.length; i++){
			rgb[i] = Integer.parseInt(strArray[i]);
			Log.i(TAG, "RGB: " + rgb[i]);
		} 
		return rgb;
	}


}