package com.example.bluetooth.le;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * SQLite操作助手类（DBOpneHelper）
 * @author yan
 */
public class DBOpneHelper extends SQLiteOpenHelper {
	private static final int VERSION = 1;// 版本
	private static final String DB_NAME = "Blue.db";// 数据库名
	public static final String STUDENT_TABLE = "blue";// 表名
	private static final String CREATE_TABLE = "CREATE TABLE blue (_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, major SMALLINT, minor SMALLINT, proximityUuid VARCHAR, bluetoothAddress VARCHAR)";//数据库表创建语句
	public DBOpneHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}
	/**
	 * 数据库第一次被创建时调用函数OnCreat
	 * 完成新表单的创建
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}
	/**
	 * 版本升级时被调用
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
