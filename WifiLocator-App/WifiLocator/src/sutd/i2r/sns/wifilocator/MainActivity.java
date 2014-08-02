package sutd.i2r.sns.wifilocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final int Data_Collection_Round = 50;
	private final String[] All_SSID = {"SUTD_Staff", "SUTD_GLAB", "SUTD_Student", "SUTD_Guest", "SUTD_BOT", "SUTD_ILP2", "PD193298CC004Name93290C00017A", "HPCP1525-7dcf88", "ai-hub", "ai-ap1", "", "DENSO WIFI", "FutureLivingLab", "RMIL_Bridge", "linksys_SES_32488", "KV2-119-20-14", "SUTD LT5 Crestron AV"};
	private final int Mode_Distance = 0;
	private final int Mode_Correlation = 1;
	private final String[] place_name = {"CC2-1","CC2-2","space bar","canteen","LT5","LT4"};
	
	private List<List<HashMap<String,Double>>> fingerprint;
	private ImageButton btn_locateme;
	private AnimationDrawable wifi_anim;
	private ImageView wifi_signal;
	private String final_name;
	private TextView place_location;
	private Handler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		wifi_signal = (ImageView) findViewById(R.id.wifi_signal);
		wifi_anim = (AnimationDrawable) wifi_signal.getBackground();
		place_location = (TextView) findViewById(R.id.location_name);
		place_location.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/NanumBrushScript-Regular.ttf"));		
		btn_locateme = (ImageButton) findViewById(R.id.btn_locateme);
		
		mHandler = new Handler(){
			public void handleMessage(Message msg) {  
	        	wifi_anim.stop(); 
	        	btn_locateme.setBackgroundResource(R.drawable.btn_locateme_normal); 
	        	place_location.setText("Located at "+final_name);
	        }
		};
		
		btn_locateme.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				btn_locateme.setBackgroundResource(R.drawable.btn_locateme_selected);
				place_location.setText("wait...");
				wifi_anim.start();
				new Thread(){  
                    public void run() {  
                    	try {
							final_name = new DataProcessing().execute().get();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        mHandler.sendEmptyMessage(0);                        
                    }}.start();
				
			}
			
		});
		
		
		
	}
	
	class DataProcessing extends AsyncTask<Void,Void,String>{
		private WifiManager wifiManager;
		private List<ScanResult> scanResult_list;
		private Handler handler;
		private Runnable runnable;
		private int round_counter;
		private int minimum;
		private int maximum;
		private HashMap<String,Double> result;
		private double point_err;
		private List<Double> room_err;
		private List<Double> room_errs;
		
		@Override
		protected String doInBackground(Void... arg0) {	
			round_counter = 0;
			result = new HashMap<String,Double>();
			
			handler = new Handler(){};
			runnable= new Runnable(){
				public void run() {
					round_counter = round_counter+1;					
					if (round_counter <= Data_Collection_Round) {
						handler.postDelayed(this, 2500);
						collectData();
						
					}
					else{
						handler.removeCallbacks(this);
					}						
				}					
			};
			handler.post(runnable);
			
			return handleData(Mode_Distance);
		}
		
		private void collectData(){
			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			scanResult_list = wifiManager.getScanResults();
						
			if (scanResult_list == null) {
				Toast.makeText(MainActivity.this, "check Wifi connection", Toast.LENGTH_LONG).show();
			}
			else {
				for (int i = 0; i<scanResult_list.size();i++){
					ScanResult scanResult = scanResult_list.get(i);
					String ssid = scanResult.SSID;
					double strength = scanResult.level;	
					result.put(ssid,result.containsKey(ssid)? (result.get(ssid)+strength)/2.0:strength);
			    } 			
		    }	
			
			for (int i = 0; i < All_SSID.length; i++){
				if (!result.containsKey(All_SSID[i])){
					result.put(All_SSID[i], 0.0);
				}
			}
		}
		
		private String handleData(int mode){			
			if (mode == 1){
				room_errs = new ArrayList();				
				//match location using distance between vectors
				for (List<HashMap<String,Double>> room: fingerprint){
					room_err = new ArrayList();
					for (HashMap<String,Double> point: room){
						point_err = 0;
						for (String ssid: point.keySet()){
							point_err = point_err + (point.get(ssid)-result.get(ssid))*(point.get(ssid)-result.get(ssid));
						}
						room_err.add(Math.sqrt(point_err));
					//method 1: get the least error
					double min = 0;
					for (double a: room_err){
						if (a < min)
							min = a;
					}
					room_errs.add(min);	
					//method 2: get the average
//					double sum = 0;
//					for (double a: room_err){
//						sum = sum + a;
//					}
//					room_errs.add(sum/room_err.size());
					}
				}
				
				for (int i = 0 ; i<room_errs.size(); i++){
					if (i == 0){
						minimum = i;
					}
					else{
						if (room_errs.get(i) < room_errs.get(minimum))
							minimum = i;
					}				
				}
				return place_name[minimum];					
			}
			else{
				room_errs = new ArrayList();
			    //match location using correlation of vectors
				for (List<HashMap<String,Double>> room: fingerprint){
					room_err = new ArrayList();
					for (HashMap<String,Double> point: room){
						point_err = 0;
						for (String ssid: point.keySet()){
							point_err = point_err + point.get(ssid)*result.get(ssid);
						}
						room_err.add(point_err);
					//method 1: get the max correlation
					double max = 0;
					for (double a: room_err){
						if (a > max)
							max = a;
					}
					room_errs.add(max);	
					//method 2: get the average
//					double sum = 0;
//					for (double a: room_err){
//						sum = sum + a;
//					}
//					room_errs.add(sum/room_err.size());				
			       }
				}
				
				for (int i = 0 ; i<room_errs.size(); i++){
					if (i == 0){
						maximum = i;
					}
					else{
						if (room_errs.get(i) > room_errs.get(maximum))
							maximum = i;
					}
				
				}
				return place_name[maximum];
		   }			
		}
		
	}
		
}
