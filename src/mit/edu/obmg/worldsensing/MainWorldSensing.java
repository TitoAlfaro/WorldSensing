/**
 * Written by Santiago Alfaro and Micah Rye
 */

package mit.edu.obmg.worldsensing;

import mit.edu.obmg.worldsensing.ioio.IOIOBGService;
import mit.edu.obmg.worldsensing.ioio.IOIOBGService.LocalBinder;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainWorldSensing extends Activity implements 	OnClickListener, 
OnSeekBarChangeListener{

	private IOIOBGService mIOIOService;
	private boolean mBounded = false;

	private static ToggleButton mLEDdebug;
	private boolean mLEDState;

	//Sonar
	private static SeekBar mSonar_Bar;
	private static TextView mSonar_Value;
	private static int	mSonar_Input;
	private long LastChange;
	private final int Polling_Delay = 150;

	//color
	Drawable shape;
	private View mColorIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startService(new Intent(this,IOIOBGService.class));

		mLEDdebug = (ToggleButton) findViewById(R.id.LEDBtn);
		mLEDdebug.setOnClickListener(this);

		mSonar_Value = (TextView) findViewById(R.id.SonarTitle);
		mSonar_Bar = (SeekBar) findViewById(R.id.SonarBar);
		mSonar_Bar.setOnSeekBarChangeListener(this);
		mSonar_Bar.setProgress(mSonar_Input);

		mColorIndicator = findViewById(R.id.ColorIndicator);
		mLEDdebug.setBackgroundColor(Color.rgb(150, 150, 150));

	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent mIOIO = new Intent(this, IOIOBGService.class);
		bindService(mIOIO, mConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mBounded) {
			unbindService(mConnection);
			mBounded = false;
		}
	}

	@Override
	protected void onDestroy(){
		//stop the service on exit of program
		stopService((new Intent(this, IOIOBGService.class)));
		super.onDestroy(); 
	}

	ServiceConnection mConnection = new ServiceConnection() { 
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBounded = true;
			LocalBinder mLocalBinder = (LocalBinder)service;
			mIOIOService = mLocalBinder.getServerInstance();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBounded = false;
			mIOIOService = null;
		}
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){

		case R.id.LEDBtn:
			if (mLEDState == true){
				mLEDState = false;
				mLEDdebug.setChecked(mLEDState);
				mLEDdebug.setText("Debug Off");
				try{
					mIOIOService.setLED(true);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				mLEDState = true;
				mLEDdebug.setChecked(mLEDState);
				mLEDdebug.setText("Debug On");
				try{
					mIOIOService.setLED(false);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			break;
		}		
	}

	public static void getSonar(float sonar){
		mSonar_Input = (int)(sonar*1000);
		mSonar_Value.setText("Distance: "+mSonar_Input+" in.");
		mSonar_Bar.setProgress(mSonar_Input);
		//System.out.println("getSonar input: "+ sonar);
	}

	public static void getColor(int[] color){
		//String input = color;
		System.out.println("getColor input: "+ color);
		int[] data = new int[3];
		int v = 0;
		/*for(int i=0; i<12;i++){
			if(color[i] == 13) {
				data[v] = Integer.parseInt(input);
				input = "";
				break;
			}
			if(color[i] == 44){
				data[v] = Integer.parseInt(input);
				v++;
				input = "";
				if(v == 3) break;
				continue;
			}*/
			//input += Character.toString((char)color[i]);
			//System.out.println("data: "+ data[v]);
		//}
		
		//System.out.println("input: "+ input);
		//System.out.println("R: "+data[0]+" G: "+data[1]+" B: "+data[2]);
		//mLEDdebug.setBackgroundColor(Color.rgb(data[0], data[1], data[2]));

		return;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(System.currentTimeMillis() - LastChange > Polling_Delay){
			updateState(seekBar);
			LastChange = System.currentTimeMillis();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		LastChange = System.currentTimeMillis();

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateState(seekBar);

	}

	private void updateState(final SeekBar seekbar){
		mSonar_Input = seekbar.getProgress();
	}
}