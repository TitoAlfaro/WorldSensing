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
import android.net.wifi.WifiManager;
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

	//Sonar
	private final int mSonar_PIN = 40;
	private AnalogInput mSonar_Input;
	float mSonar_Reading;

	//Color
	int	rX = 34;
	int tX = 35;
	private Uart mColor_uart = null;
	private InputStream mColor_in = null;
	private OutputStream mColor_out= null;
	byte[] mColor_buffer;
	int[] rdata;
	int data[] = new int[24];
	int data2[] = new int[24];

	Boolean imBusy = false; 

	@Override
	protected IOIOLooper createIOIOLooper() {

		UIHandler = new Handler();

		return new BaseIOIOLooper() {

			@Override
			protected void setup() throws ConnectionLostException,
			InterruptedException {
				mIOIOConnected = true;

				System.out.println(" :|   Open UART");

				mLED = ioio_.openDigitalOutput(0, true);
				mSonar_Input = ioio_.openAnalogInput(mSonar_PIN);
				mColor_uart = ioio_.openUart(tX, rX, 38400, Uart.Parity.NONE, Uart.StopBits.ONE);	
				mColor_in = mColor_uart.getInputStream();
				mColor_out = mColor_uart.getOutputStream();		

				System.out.println(" :)   UART OK");
			}
			
			/*public  void Write(byte[] data){
				try {
					out_.write(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				logData("write:", data);
			}*/
			
			public  int[] Read(int len) {

				int nTimeCount = 0;

				//wait data available to receive
				try {
					while (mColor_in.available() < len) {
						//Log.i("data available:", Integer.toString(mColor_in.available()));	
						nTimeCount++;
						Thread.sleep(160);

						/*if (nTimeCount == 500) {//5 seconds timeout
							System.out.println("timeout 5 second");
							return null;
						}*/
					}

					mColor_buffer = new byte[len];	
					
					for(int i=0; i<12; i++){
						data[i]= (byte) mColor_in.read();
						if(data[i] == 13){
							for(int j=0; j<12; j++){
								data2[j]= (byte) mColor_in.read();
								System.out.println("data2: "+data2[j]);
							}
							
						}

						//System.out.println("data1: "+data[i]);
					}
					mColor_in.read(mColor_buffer);
					//String input = new String(mColor_buffer);

					//System.out.println("colorBuffer: "+input);
					//System.out.println("colorIn: "+mColor_in);

				} catch (Exception e) {
					System.out.println("Error data2: "+data2);
				}				
				return data2;	
			}			
			
			@Override
			public void loop() throws ConnectionLostException, InterruptedException {
				mSonar_Reading = mSonar_Input.read();

				try {

					rdata=Read(24);

					if (rdata == null)
						throw new Exception("received data null"); 

				} catch (Exception e) {
					e.printStackTrace();
				}


				//Handler to send data back to Main
				Runnable runnable = new Runnable(){
					public void run(){
						UIHandler.post(new Runnable(){
							public void run(){
								MainWorldSensing.getSonar(mSonar_Reading);
								if (rdata != null)
									MainWorldSensing.getColor(rdata);
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
}