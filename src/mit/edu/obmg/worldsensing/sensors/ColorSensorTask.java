package mit.edu.obmg.worldsensing.sensors;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import mit.edu.obmg.worldsensing.ioio.IOIOBGService;

import android.R.integer;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.SeekBar;
import android.widget.TextView;

public class ColorSensorTask extends AsyncTask<Integer, Void, int[]> {

	private InputStream mColor_in = null;
	//private OutputStream mColor_out= null;
	byte[] mColor_buffer;
	int[] rdata;
	
	private IOIOBGService activity;
	private Context context;

	/*
	 * constructor
	 */
	/*public ColorSensorTask(){
    	super();
    	this.activity = activity;
    	this.context = this.activity.getApplicationContext();

	}*/
	
	@Override
	protected int[] doInBackground(Integer... params) {

		int len = params[0];
		/*public  void Write(byte[] data){
		try {
			out_.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logData("write:", data);
	}*/

		int nTimeCount = 0;

		//wait data available to receive
		try {
			while (mColor_in.available() < len) {
				//Log.i("data available:", Integer.toString(mColor_in.available()));	
				nTimeCount++;
				Thread.sleep(20);

				if (nTimeCount == 500) {//5 seconds timeout
					System.out.println("timeout 5 second");
					return null;
				}
			}

			mColor_buffer = new byte[len];	
			int data[] = new int [len];
			int data2[] = new int [len];

			mColor_in.read(mColor_buffer);
			String input = new String(mColor_buffer);
			System.out.println("colorBuffer: "+input);

			for(int i=0; i<12; i++){
				data[i]= (byte) mColor_in.read();
				if(data[i] == 13){
					for(int j=0; j<12; j++){
						data2[j]= (byte) mColor_in.read();
						//System.out.println("data2: "+data2[j]);
					}

				}

				//System.out.println("data1: "+data[i]);
			}

			return data2;
		} catch (Exception e) {
			return null;
		}				
	}
	protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Long result) {
        //showDialog("Downloaded " + result + " bytes");
    }
}

