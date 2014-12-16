package com.example.bluetooth.le;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.bluetooth.le.iBeaconClass.iBeacon;
/**
 * 显示搜索结果IBeacon的自定义List列表类（LeDeviceListAdapter）
 * @author yan
 */
public class LeDeviceListAdapter extends BaseAdapter {
	private ArrayList<iBeacon> mLeDevices;//搜索到的设备
	private ArrayList<iBeacon> mShowLeDevices;//待显示的设备
	private LayoutInflater mInflator;
	private Activity mContext;
	public List<iBeacon> ibeacons;//数据库中已添加的设备
	public Boolean open = false;
	public LeDeviceListAdapter(Activity c) {
		super();
		mContext = c;
		mLeDevices = new ArrayList<iBeacon>();
		mShowLeDevices = new ArrayList<iBeacon>();
		mInflator = mContext.getLayoutInflater();
		SQLOperateImpl SQL_Blue = new SQLOperateImpl(mContext);  
		ibeacons = SQL_Blue.find();
	}
    /**
	 * 添加设备（addDevice）
	 * @param device 待添加的IBeacon数据
	 */
	public void addDevice(iBeacon device) {	
		if(device==null)
			return;
		int count = 0;
		String buleMacString = device.bluetoothAddress;
		//添加、更新待显示列表mShowLeDevices的数据
		for (count = 0; count < ibeacons.size(); count++) {
			String btAddress = ibeacons.get(count).bluetoothAddress;
			if(btAddress.equals(buleMacString)){
				int i;
				for(i=0;i<mShowLeDevices.size();i++){
					String btAddress2 = mShowLeDevices.get(i).bluetoothAddress;
					if(btAddress2.equals(device.bluetoothAddress)){
						//如果已经存在则更新数据
						device.name = ibeacons.get(count).name;
						mShowLeDevices.set(i, device);
						break;
					}
				}
				if(i==mShowLeDevices.size())
				mShowLeDevices.add(device);
				break;
			}
		}
		//添加、更新搜索到的mLeDevices数据
		for(int i=0;i<mLeDevices.size();i++){
			String btAddress = mLeDevices.get(i).bluetoothAddress;
			if(btAddress.equals(device.bluetoothAddress)){
				//如果已经存在则更新数据
				mLeDevices.set(i, device);
				return;
			}
		}
		mLeDevices.add(device);
	}
	/**
	 * 获得显示的设备（getDevice）、获得搜索的设备（getRealDevice）
	 * @param position 待提取的IBeacon数据位置
	 */
	public iBeacon getDevice(int position) {
		return mShowLeDevices.get(position);
	}
	public iBeacon getRealDevice(int position) {
		return mLeDevices.get(position);
	}
	/**
	 * 清空缓存
	 */
	public void clear() {
		mShowLeDevices.clear();
		mLeDevices.clear();
	}
	/**
	 * 获得显示的设备数量（getCount）、获得搜索的设备数量（getRealCount）
	 */
	@Override
	public int getCount() {
		return mShowLeDevices.size();
	}
	public int getRealCount() {
		return mLeDevices.size();
	}
	/**
	 * 获得显示的设备（getItem）、获得搜索的设备（getRealItem）
	 * @param i 待提取的数据位置
	 */
	@Override
	public Object getItem(int i) {
		return mShowLeDevices.get(i);
	}
	public Object getRealItem(int i) {
		return mLeDevices.get(i);
	}
	/**
	 * 获得项目ID
	 * @param i 待提取的数据位置
	 */
	@Override
	public long getItemId(int i) {
		return i;
	}
	/**
	 * 获得视图
	 * @param i 当前位置
	 * @param view 视图内容
	 * @param viewGroup 视图组
	 */
	@Override
	public View getView(final int i, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		iBeacon device = mShowLeDevices.get(i);
		if (view == null) {
			//初始化空视图
			view = mInflator.inflate(R.layout.listitem_device, null);
			viewHolder = new ViewHolder();
			viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
			viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
			viewHolder.deviceUUID= (TextView)view.findViewById(R.id.device_beacon_uuid);
			viewHolder.deviceMajor_Minor=(TextView)view.findViewById(R.id.device_major_minor);
			viewHolder.devicetxPower_RSSI=(TextView)view.findViewById(R.id.device_txPower_rssi);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		//设置设备名称
		final String deviceName = device.name;
		if (deviceName != null && deviceName.length() > 0)
			viewHolder.deviceName.setText(deviceName);
		else
			viewHolder.deviceName.setText(R.string.unknown_device);
		//设置各文本框内容
		viewHolder.deviceAddress.setText(device.bluetoothAddress);
		viewHolder.deviceUUID.setText(device.proximityUuid);
		viewHolder.deviceMajor_Minor.setText("major:"+device.major+",minor:"+device.minor);
		viewHolder.devicetxPower_RSSI.setText("txPower:"+device.txPower+",rssi:"+device.rssi);
		//注册点击的监听事件
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//切换到新的页面
				Intent intent = new Intent(mContext,iBeaconActivity.class);
				if(i<mLeDevices.size())
				{
					//传送当前选择的序列ID
					String btAddress = mLeDevices.get(i).bluetoothAddress;
					intent.putExtra("a",btAddress);
					mContext.startActivity(intent);
				}
			}
		});
		return view;
	}
	class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView deviceUUID;
		TextView deviceMajor_Minor;
		TextView devicetxPower_RSSI;
	}
}
