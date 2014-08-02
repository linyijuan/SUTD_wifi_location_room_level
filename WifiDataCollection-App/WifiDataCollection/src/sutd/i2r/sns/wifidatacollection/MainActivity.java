package sutd.i2r.sns.wifidatacollection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends Activity {
	private WifiManager wifiManager;
	private List<ScanResult> list;
	private List<HashMap<String,String>> record;
	private EditText filename;
	private Button btn_start;
	private ListView record_list;
	private int counter = 0;
	private SimpleDateFormat sDateFormat;
	private Runnable runnable;
	private Handler handler;
	private int round_counter;
	private int total_counter;
	private ListAdapter mAdapter;
	private List<String> ssid_record;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
		filename = (EditText)findViewById(R.id.file_name);
		btn_start = (Button) findViewById(R.id.btn_start);
		record_list = (ListView)findViewById(R.id.record_list);
		
		sDateFormat =  new SimpleDateFormat("MM-dd hh:mm:ss");
		
		record = new ArrayList<>();
		ssid_record = new ArrayList<>();
		
		HashMap init_item = new HashMap<String,String>();
		init_item.put("time", "");
		init_item.put("filename", "");
		record.add(init_item);
		mAdapter = new ListAdapter(this,record);
		record_list.setAdapter(mAdapter);
		
		total_counter = 0;
		
		btn_start.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
			    round_counter = 0;
			    total_counter ++;
			    HashMap item_record = new HashMap<String,String>();
			    item_record.put("time", sDateFormat.format(new java.util.Date()));
			    item_record.put("filename", filename.getEditableText().toString()+".txt");
			    if (record.get(0).get("filename").equals("")){record.remove(0);}
			    record.add(item_record);
			    btn_start.setText("writing data...");
				handler = new Handler(){};
				runnable= new Runnable(){
					public void run() {
						round_counter = round_counter+1;
						action(filename.getEditableText().toString(),round_counter,total_counter);
						if (round_counter <=100) {
							handler.postDelayed(this, 2500);
						}
						else{
							handler.removeCallbacks(this);
							mAdapter.notifyDataSetChanged();
							btn_start.setText("Start");
						}						
					}					
				};
				handler.post(runnable);
								
			}
		});
		
		

	}
	
	private void action(String name,int round_counter,int total_counter){
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		list = wifiManager.getScanResults();
		String wifiInfo = "";
		if (list == null) {
			Toast.makeText(MainActivity.this, "check Wifi connection", Toast.LENGTH_LONG).show();
		}else {
			for (int i = 0; i<list.size();i++){
				ScanResult scanResult = list.get(i);
				String bssid = scanResult.BSSID;
				String ssid = scanResult.SSID;
				int strength = scanResult.level;				
				wifiInfo = wifiInfo + String.format("%16s,%25s,%20s,%5d\n", sDateFormat.format(new java.util.Date()),bssid,ssid,strength);					    
			}
		}		
		writeToFile(wifiInfo, name);
	}
	
	private void writeToFile(String data,String fileName){
		File file = null;
		OutputStream output = null;
		byte buffer[] = null;			
	    try{ 
	    	File path = Environment.getExternalStorageDirectory();
			file = new File(path,fileName+".txt");
			if(!file.exists())
				file.createNewFile();
			output = new FileOutputStream(file, true);
			buffer = data.getBytes();
			output.write(buffer);
			output.flush();
			output.close();  	   
	        }catch(Exception e){ 	           
	        	e.printStackTrace(); 
	        } 
	}
	
	class ListAdapter extends BaseAdapter {
		private List<HashMap<String,String>> list;
		private LayoutInflater inflater;
		private TextView time;
		private TextView name;
		
		public ListAdapter(Context context, List<HashMap<String,String>> list) {
			this.inflater = LayoutInflater.from(context);
			this.list = list;
		 }
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			view = inflater.inflate(R.layout.list_item, null);
			name = (TextView) view.findViewById(R.id.item_name);
			time = (TextView) view.findViewById(R.id.item_time);
			name.setText(list.get(position).get("filename"));
			time.setText(list.get(position).get("time"));			
			return view;
		}

	}
	

}
