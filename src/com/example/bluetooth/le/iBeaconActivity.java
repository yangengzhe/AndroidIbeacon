package com.example.bluetooth.le;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bluetooth.le.iBeaconClass.iBeacon;
/**
 * 设备定位界面类（iBeaconActivity）
 * @author yan
 */
public class iBeaconActivity extends Activity {
	private TextView tv1, tv2, tv3, tv4, tv_degree;
	private ProgressBar pro;
	private Boolean connected = true;
	private Button bt1, bt2;
	private int last_rssi = -1, num = 0, now_rssi = 0, time = 0,
			times_degree = 0;
	private int num2 = 0, rssi2 = 0;
	private float degree, start_degree = 0, find_degree = 0,start_degree2 = 0;
	private ImageView image; // 指南针图片
	private float currentDegree = 0f, lastDegree = 0f;
	private Boolean finded = false, signBoolean = false;
	private float con_find = 0;
	private OutputStream fw;
	// 感应器Sensor对象
	private SensorManager mManager = null;
	private Sensor mSensor = null;
	// 创建监听器
	private SensorEventListener mListener = null;
	//记录RSSI值
	private double[][] con_rssi = new double[360][2];
	private double[][] re_rssi = new double[360][2];
	/**
	 * 关闭界面，清理缓存
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DeviceScanActivity.mLeDeviceListAdapter.clear();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gatt_services_characteristics);
		pro = (ProgressBar) findViewById(R.id.progressBar1);
		pro.setVisibility(View.INVISIBLE);
		tv1 = (TextView) findViewById(R.id.device_address);
		tv2 = (TextView) findViewById(R.id.connection_state);
		tv3 = (TextView) findViewById(R.id.data_value);
		tv4 = (TextView) findViewById(R.id.distance);
		tv_degree = (TextView) findViewById(R.id.textView_degree);
		bt1 = (Button) findViewById(R.id.button_title);
		bt2 = (Button) findViewById(R.id.button2);
		bt2.setEnabled(false);
		bt2.setVisibility(View.INVISIBLE);
		image = (ImageView) findViewById(R.id.imageView_compass);
		Intent intent = getIntent();
		String addressString = intent.getStringExtra("a");
		// 查找当前的IBeacon序号
		for (int m = 0; m < DeviceScanActivity.mLeDeviceListAdapter.getRealCount(); m++) {
			String btAddress = DeviceScanActivity.mLeDeviceListAdapter
					.getRealDevice(m).bluetoothAddress;
			if (btAddress.equals(addressString)) {
				num = m;
				break;
			}
		}

		if(DeviceScanActivity.mLeDeviceListAdapter.getCount()>1)
		{
			num2 = num==0?1:0;
		}
		else {
			num2 = num;
		}
		getActionBar().setTitle("室内定位"+" - "+DeviceScanActivity.mLeDeviceListAdapter
				.getRealDevice(num).name);
		// 实时更新数据
		final Handler mHandler = new Handler();
		Runnable myRunnable = new Runnable() {
			public void run() {
				if (DeviceScanActivity.mLeDeviceListAdapter.getRealCount() != 0
						&& num < DeviceScanActivity.mLeDeviceListAdapter
								.getRealCount()) {
					iBeacon miBeacon = DeviceScanActivity.mLeDeviceListAdapter
							.getRealDevice(num);
					iBeacon miBeacon2 = DeviceScanActivity.mLeDeviceListAdapter
							.getRealDevice(num2);
					now_rssi = miBeacon.rssi;
					rssi2 = miBeacon2.rssi;
					if (connected) {
						tv1.setText(miBeacon.bluetoothAddress + "");
						tv2.setText("连接");
						tv3.setText(miBeacon.rssi + "");
						DecimalFormat df = new DecimalFormat("#0.00");
						tv4.setText(df.format(calculateAccuracy(
								miBeacon.txPower, miBeacon.rssi)) + "");
					}
				}
				mHandler.postDelayed(this, 500);
			}
		};
		mHandler.post(myRunnable);
		// 判断是否连接
		final Handler mHandler2 = new Handler();
		mHandler2.post(new Runnable() {
			public void run() {
				if (last_rssi != now_rssi) {
					last_rssi = now_rssi;
					connected = true;
					time = 0;
				} else {
					time++;
				}

				if (time > 4) {
					tv2.setText("未连接");
					tv3.setText("0");
					tv4.setText("--");
					connected = false;
				}
				mHandler2.postDelayed(this, 1000);
			}
		});

		mManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);// 方向
		mListener = new SensorEventListener() {
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}

			public void onSensorChanged(SensorEvent event) {
				degree = event.values[0];
			}
		};
		// 停止按钮
		bt2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pro.setVisibility(View.INVISIBLE);
				stop_button();
			}
		});
		// 开始按钮
		bt1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					SimpleDateFormat   sDateFormat   =   new   SimpleDateFormat("hh");
					String   filenameString   =   sDateFormat.format(new   java.util.Date());  
					sDateFormat   =   new   SimpleDateFormat("mm");
					filenameString   =filenameString +"_"+   sDateFormat.format(new   java.util.Date());
					sDateFormat   =   new   SimpleDateFormat("ss");
					filenameString   =filenameString +"_"+   sDateFormat.format(new   java.util.Date());
					if (Environment.getExternalStorageState().equals(  
					                    Environment.MEDIA_MOUNTED)) {  
					                // 获取SD卡的目录  
						File file=new File(Environment.getExternalStorageDirectory(), "log_"+filenameString+".txt");  
						fw=new FileOutputStream(file);    
//					               fileW.close();  
					           }  

					
					
//					fw = new FileWriter("/data/data/package_name/files" + "/log_"+filenameString+".txt");
//					fw.flush();
//					fw.write(str);
//					fw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Toast.makeText(getApplicationContext(), "原地缓慢旋转一周，当指针回到起始位置时即可", Toast.LENGTH_LONG).show();
				pro.setVisibility(View.VISIBLE);
				signBoolean = false;
				times_degree = 0;
				start_degree = 0;
				finded = false;
				bt2.setEnabled(true);
				bt1.setEnabled(false);
				bt1.setVisibility(View.INVISIBLE);
				bt2.setVisibility(View.VISIBLE);
				mManager.registerListener(mListener, mSensor,
						SensorManager.SENSOR_DELAY_GAME);
				// 判断位置
				final Handler mHandler = new Handler();
				Runnable runnable = new Runnable() {
					public void run() {
						if (signBoolean)
							return;
						if (times_degree == 2)
							start_degree = degree;
						// 转完一周，未找到
						if (times_degree > 30
								
								&& Math.abs(start_degree - degree) < 10
								&& !finded) {
							pro.setVisibility(View.INVISIBLE);
							int temp_count = 0;
							for (int m = 0; m < 360; m++) {
								double start = m;
								double sum_1 = 0;
								double sum_2 = 0;
								int count = 0;
								int count2 = 0;
								int i = 0;
								while (true) {
									if (Math.abs(con_rssi[i][0] - start) < 90) {
										sum_1 += con_rssi[i][1];
										count++;
									} else {
										sum_2 += con_rssi[i][1];
										count2++;
									}
									if (i == times_degree)
										i = 0;
									else
										i++;
									if (count + count2 == times_degree)
										break;
								}
								re_rssi[temp_count][0] = start;
								re_rssi[temp_count][1] = Math.abs(Math.abs(sum_1 / count)-Math.abs(sum_2 / count2));
								temp_count++;
							}

							double max = 0;
							int max_i = 0;
							for (int i = 0; i < temp_count; i++) {
								if (max < re_rssi[i][1]) {
									max = re_rssi[i][1];
									max_i = i;
								}
							}
							currentDegree = (float) (re_rssi[max_i][0] + 45.0 + 180.0);
							con_find = currentDegree-180;
							//置信度
							tv_degree.setText(times_degree + "");
							float confidence = confidence();
							Log.d("222", "位置：" + con_find + "置信值："+confidence);
							try {
								fw.write(("位置：" + con_find + "置信值："+confidence).getBytes());
								
								fw.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
							Toast.makeText(getApplicationContext(),
									"置信度："+confidence + "", Toast.LENGTH_LONG)
									.show();
							finded = true;
							start_degree2 = degree;
							currentDegree = currentDegree - start_degree;
							find_degree = currentDegree;
						} else if (!finded) {// 转圈中
							tv_degree.setText(degree + "," + times_degree + " 当前："+now_rssi +" 另："+rssi2);
							pro.setProgress(times_degree);
							times_degree++;
							con_rssi[times_degree][0] = degree;
							con_rssi[times_degree][1] = now_rssi;
							currentDegree = degree;
							currentDegree = currentDegree - start_degree;
							Log.d("222",times_degree+","+ degree + "," + now_rssi+ "," + rssi2);
							try {
								fw.write((times_degree+","+ degree + "," + now_rssi+ "," + rssi2+"\r\n").getBytes());
//								fw.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {// 找到
//							Log.d("111", lastDegree+" , "+currentDegree);
							if (Math.abs(currentDegree%360) < 10) // 转回位置
							{
								stop_button();
								return;
							}
							currentDegree = degree;
							currentDegree = currentDegree - start_degree2;
							currentDegree = find_degree - currentDegree;
						}

						if (times_degree > 2) {// 处理图片旋转
							RotateAnimation ra = null;

							if (Math.abs(lastDegree - currentDegree) > 300) {
								if (lastDegree > currentDegree)
									lastDegree = lastDegree - 360;
								else
									lastDegree = lastDegree + 360;
							}

							ra = new RotateAnimation(lastDegree, currentDegree,
									Animation.RELATIVE_TO_SELF, 0.5f,
									Animation.RELATIVE_TO_SELF, 0.5f);
							ra.setDuration(100);// 动画持续时间
							ra.setFillAfter(true);
							image.startAnimation(ra);
							lastDegree = currentDegree;
							
						}
						mHandler.postDelayed(this, 300);
					}
				};
				mHandler.post(runnable);

			}
		});

	}

	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mManager.unregisterListener(mListener);
	}

	public void stop_button(){//停止按钮
		signBoolean = true;
		times_degree = 0;
		start_degree = 0;
		finded = false;
		bt1.setEnabled(true);
		bt2.setEnabled(false);
		bt1.setVisibility(View.VISIBLE);
		bt2.setVisibility(View.INVISIBLE);
		currentDegree = 0;
		if (Math.abs(lastDegree - currentDegree) > 300) {
			if (lastDegree > currentDegree)
				lastDegree = lastDegree - 360;
			else
				lastDegree = lastDegree + 360;
		}
		RotateAnimation ra= new RotateAnimation(lastDegree, 0,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		ra.setDuration(100);// 动画持续时间
		ra.setFillAfter(true);
		image.startAnimation(ra);
		lastDegree = 0;
	}
	
	public int getProximity(int txPower, double rssi) {
		return calculateProximity(calculateAccuracy(txPower, rssi));
	}

	public static double calculateAccuracy(int txPower, double rssi) {// 计算距离
		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}

		double ratio = rssi * 1.0 / txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio, 10);
		} else {
			double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
			return accuracy;
		}
	}

	public static int calculateProximity(double accuracy) {
		if (accuracy < 0) {
			return 0;
			// is this correct? does proximity only show unknown when accuracy
			// is negative? I have seen cases where it returns unknown when
			// accuracy is -1;
		}
		if (accuracy < 0.5) {
			return 1;
		}
		// forums say 3.0 is the near/far threshold, but it looks to be based on
		// experience that this is 4.0
		if (accuracy <= 4.0) {
			return 2;
		}
		// if it is > 4.0 meters, call it far
		return 3;

	}
	
	public float confidence ()
	{
		float p = 0;
		double[] t = new double[360];
		for(int i = 0 ; i<times_degree ; i++){
			
			if(Math.abs(con_find - con_rssi[i][0])<=45.0)
			{
				t[i] = -1;
//				Log.d("222", "11");
			}
				
		}
		
		float avg_t = 0,avg_r = 0;
		float sum_t = 0,sum_r = 0;
		for(int i = 0 ; i<times_degree ; i++){
			sum_t += t[i];
			sum_r += con_rssi[i][1];
		}
		avg_t = sum_t / times_degree ;
		avg_r = sum_r / times_degree ;
		Log.d("111", avg_t+","+avg_r);
		Log.d("111", con_find+","+sum_t);
		float sum = 0;
		for(int i = 0 ; i<times_degree ; i++){
			sum += (t[i] -avg_t)*(con_rssi[i][1] -avg_r);
		}
		
		float s_sum_t = 0;
		float s_t = 0;
		for(int i = 0 ; i<times_degree ; i++){
			s_sum_t += ((t[i] -avg_t)*(t[i] -avg_t));
		}
		s_t=(float) Math.sqrt(s_sum_t/times_degree);
		
		float s_sum_r = 0;
		float s_r = 0;
		for(int i = 0 ; i<times_degree ; i++){
			s_sum_r += ((con_rssi[i][1] -avg_r)*(con_rssi[i][1] -avg_r));
		}
		s_r=(float) Math.sqrt(s_sum_r/times_degree);
		
		p = Math.abs(sum/(times_degree*s_t*s_r));
		Log.d("111", p+"="+sum+"/"+times_degree+"*"+s_t+"*"+s_r);
		return p;
	}
	
    public void saveToSDCard(String filename,String content) throws Exception{  
        File file=new File(Environment.getExternalStorageDirectory(), filename);  
       OutputStream out=new FileOutputStream(file);  
        out.write(content.getBytes());  
        out.close();  
    }  


}
