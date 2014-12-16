package com.example.bluetooth.le;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.example.bluetooth.le.iBeaconClass.iBeacon;
/**
 * 设备管理列表界面类（DeviceManagerActivity）
 * @author yan
 */
public class DeviceManagerActivity extends Activity {
	private List<iBeacon> ibeacons;
	private ListView list;
	private iBeacon[] t_ibeacons;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manager_device);
		getActionBar().setTitle(R.string.app_name_manager);
		list = (ListView) findViewById(R.id.listView1);
		update();//更新显示列表
		// 注册长按点击事件
		list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				//创建删除菜单
				menu.setHeaderTitle("选择操作");
				menu.add(0, 0, 0, "删除");
			}
		});
		// 注册点击事件
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Toast.makeText(getApplicationContext(), "长按删除", Toast.LENGTH_SHORT).show();
			}
		});
	}
	/**
	 * 添加菜单
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.title, menu);
		return true;
	}
	/**
	 * 添加菜单事件
	 * @param item 点击的菜单项
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		int item_id = item.getItemId();
		switch (item_id) {
		case R.id.menu_add:
			//添加按钮
			showDialog();
			update();
			break;
		case R.id.menu_return:
			//返回按钮
			finish();
			break;
		default:
			//其它，日志记录
			Log.d("IBeacon", "CLICK: "+item_id);
			break;
		}
		return true;
	}
	/**
	 * 添加长按事件
	 * @param item 点击的菜单项
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		// info.id得到listview中选择的条目绑定的id
		int id = (int) info.id;
		switch (item.getItemId()) {
		case 0:
			//删除数据
			SQLOperateImpl SQL_Blue = new SQLOperateImpl(getBaseContext());
			SQL_Blue.delete(ibeacons.get(id).bluetoothAddress);
			update();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	/**
	 * 更新数据列表
	 */
	public void update() {
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		SQLOperateImpl SQL_Blue = new SQLOperateImpl(getBaseContext());
		//从数据库中获得IBeacon数据
		ibeacons = SQL_Blue.find();
		DeviceScanActivity.mLeDeviceListAdapter.ibeacons = this.ibeacons;
		//逐一添加数据
		for (int i = 0; i < ibeacons.size(); i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", ibeacons.get(i).name);
			map.put("address", ibeacons.get(i).bluetoothAddress);
			map.put("uuid", ibeacons.get(i).proximityUuid);
			map.put("major_minor",
					ibeacons.get(i).major + "_" + ibeacons.get(i).minor);
			map.put("txPower_rssi", "");
			listItem.add(map);
		}
		// 生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,// 数据源
				R.layout.listitem_device,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "name", "address", "uuid", "major_minor",
						"txPower_rssi" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.device_name, R.id.device_address,
						R.id.device_beacon_uuid, R.id.device_major_minor,
						R.id.device_txPower_rssi });
		// 添加并且显示
		list.setAdapter(listItemAdapter);
	}
	/**
	 * 利用Dialog显示查找到的MAC地址，用来添加设备
	 */
	public void showDialog() {
		final Context context = this;
		int count = DeviceScanActivity.mLeDeviceListAdapter.getRealCount();
		// 定义列表选项
		String[] items = new String[count];
		t_ibeacons = new iBeacon[count];
		for (int m = 0; m < count; m++) {
			t_ibeacons[m] = DeviceScanActivity.mLeDeviceListAdapter
					.getRealDevice(m);
			items[m] = t_ibeacons[m].bluetoothAddress;
		}
		// 创建对话框
		new AlertDialog.Builder(context).setTitle("选择添加的设备MAC地址")// 设置对话框标题
				.setItems(items, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 点击事件
						String buleMacString = t_ibeacons[which].bluetoothAddress;
						for (int i = 0; i < ibeacons.size(); i++) {
							String btAddress = ibeacons.get(i).bluetoothAddress;
							if (btAddress.equals(buleMacString)) {
								//已经添加
								Toast.makeText(context, "已经添加该设备",
										Toast.LENGTH_LONG).show();
								return;
							}
						}
						showDialog_name(which);//显示输入名字
					}
				}).setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 设置对话框[取消]按钮
					}
				}).show();
	}
	/**
	 * 利用Dialog输入新的设备名字
	 */
	public void showDialog_name(final int chose) {
		final Context context = this;
		// 定义1个文本输入框
		final EditText userName = new EditText(this);
		// 创建对话框
		new AlertDialog.Builder(context).setTitle("请输入设备名称：")// 设置对话框标题
				.setIcon(android.R.drawable.ic_dialog_info)// 设置对话框图标
				.setView(userName)// 为对话框添加要显示的组件
				.setPositiveButton("确定", new OnClickListener() {// 设置对话框[确定]按钮
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								SQLOperateImpl SQL_Blue = new SQLOperateImpl(
										getBaseContext());
								if(userName.getText().toString()=="") 
									SQL_Blue.add(t_ibeacons[chose], "未命名");
								else
									SQL_Blue.add(t_ibeacons[chose], userName
										.getText().toString());
								Toast.makeText(context, "添加成功",
										Toast.LENGTH_LONG).show();
								update();//更新列表
							}
						}).setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SQLOperateImpl SQL_Blue = new SQLOperateImpl(
								getBaseContext());
						SQL_Blue.add(t_ibeacons[chose], "未命名");
						Toast.makeText(context, "添加成功", Toast.LENGTH_LONG)
								.show();
						update();//更新列表
					}
				}).show();
	}
}
