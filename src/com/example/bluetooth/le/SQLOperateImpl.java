package com.example.bluetooth.le;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.bluetooth.le.iBeaconClass.iBeacon;
/**
 * 对SQLite操作增删改查接口的实现（SQLOperateImpl）
 * @author yan
 */
public class SQLOperateImpl implements SQLOperate {
	private DBOpneHelper dbOpenHelper;
	public SQLOperateImpl(Context context) {
		dbOpenHelper = new DBOpneHelper(context);
	}
	/**
	 * 增，向数据库中插入数据
	 * @param p 要增加的IBeacon数据
	 * @param name 对应的IBeacon名字
	 */
	@Override
	public void add(iBeacon p,String name) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.execSQL("INSERT INTO blue VALUES (NULL, ?,?,?,?,?)", new Object[] {
				name, p.major,p.minor, p.proximityUuid,p.bluetoothAddress });
	}
	/**
	 * 删，从数据库中删除数据
	 * @param mac 要删除数据的MAC地址
	 */
	@Override
	public void delete(String mac) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.delete(DBOpneHelper.STUDENT_TABLE, "bluetoothAddress" + "=?",
				new String[] { mac });
	}
	/**
	 * 更新，向数据库中更新数据
	 * @param p 要更新的IBeacon数据
	 */
	@Override
	public void update(iBeacon p) {
		// TODO UPDATE
	}
	/**
	 * 查，从数据库中查询数据
	 */
	@Override
	public List<iBeacon> find() {
		List<iBeacon> ibeacons = null;
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.query(DBOpneHelper.STUDENT_TABLE, null, null, null,
				null, null, null);
		if (cursor != null) {
			ibeacons = new ArrayList<iBeacon>();
			while (cursor.moveToNext()) {
				iBeacon ibeacon = new iBeacon();
				ibeacon.name = cursor.getString(cursor.getColumnIndex("name"));
				ibeacon.major = cursor.getInt(cursor.getColumnIndex("major"));
				ibeacon.minor = cursor.getInt(cursor.getColumnIndex("minor"));
				ibeacon.proximityUuid = cursor.getString(cursor
						.getColumnIndex("proximityUuid"));
				ibeacon.bluetoothAddress = cursor.getString(cursor
						.getColumnIndex("bluetoothAddress"));
				ibeacons.add(ibeacon);
			}
		}
		return ibeacons;
	}

}
