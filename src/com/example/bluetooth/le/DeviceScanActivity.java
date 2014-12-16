package com.example.bluetooth.le;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.example.bluetooth.le.iBeaconClass.iBeacon;
/**
 * 主界面类（DeviceScanActivity）
 * @author yan
 */
public class DeviceScanActivity extends ListActivity {
    public static LeDeviceListAdapter mLeDeviceListAdapter;
    /**搜索BLE终端*/
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    // 每次扫描间隔时间（毫秒）.
    private static final long SCAN_PERIOD = 250;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.app_name_main );
        mHandler = new Handler();
        //判断是否支持蓝牙功能
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        //初始化一个蓝牙容器
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //判断是否成功初始化蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //开启蓝牙
        mBluetoothAdapter.enable();
        //日志记录据库已存数据数量
        SQLOperateImpl SQL_Blue = new SQLOperateImpl(getBaseContext());  
        Log.d("IBeacon","SQLite Size: "+ SQL_Blue.find().size());
    }
    /**
	 * 添加菜单
	 */
    public boolean onCreateOptionsMenu(Menu menu) {      
    	getMenuInflater().inflate(R.menu.title_main, menu);        
    	return true; 
    	}
    /**
	 * 添加菜单事件
	 * @param item 点击的菜单项
	 */
	public boolean onOptionsItemSelected(MenuItem item)  
	    {  
	        int item_id = item.getItemId();  
	        switch(item_id)  
	        {  
	        case R.id.menu_add:  
	        	//点击添加按钮
	        	Intent intent = new Intent(getApplicationContext(),DeviceManagerActivity.class);
				startActivity(intent);
	            break;  
	        default:  
	        	//其它，日志记录
	        	Log.d("IBeacon", "CLICK: "+item_id);
	            break;  
	        }  
	        return true;  
	    }
	/**
	 * 启动时，开始扫描
	 */
    @Override
    protected void onResume() {
        super.onResume();
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }
    /**
	 * 结束时，关闭扫描
	 */
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }
    /**
	 * 扫描IBeacon
	 * @param enable 是否开启（true 开启，false 关闭）
	 */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
        	//每隔SCAN_PERIOD时间关闭并开启一次
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    Toast.makeText(getApplicationContext(), "stop", Toast.LENGTH_LONG).show();
                    invalidateOptionsMenu();
                    scanLeDevice(true);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
    /**
	 * 扫描结果回调函数
	 */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        	final iBeacon ibeacon = iBeaconClass.fromScanData(device,rssi,scanRecord);
        	runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	//添加新设备，并更新界面
                    mLeDeviceListAdapter.addDevice(ibeacon);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };


}