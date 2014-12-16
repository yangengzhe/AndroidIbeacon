package com.example.bluetooth.le;
import android.bluetooth.BluetoothDevice;
/**
 * 对IBeacon的数据存储和基本操作方法（iBeaconClass）
 * @author yan
 */
public class iBeaconClass {
	/**
	 * 对IBeacon进行数据存储（IBeacon）
	 * @author yan
	 */
    static public  class iBeacon{
    	public String name;
    	public int major;
    	public int minor;
    	public String proximityUuid;
    	public String bluetoothAddress;
    	public int txPower;
    	public int rssi;
    }
    /**
	 * 根据得到的数据进行分析，解析出IBeacon（fromScanData）
	 * @param device 设备地址和名字
	 * @param rssi RSSI
	 * @param scanData 待分析的数据
	 */
    public static iBeacon fromScanData(BluetoothDevice device, int rssi,byte[] scanData) {
    	int startByte = 2;
		boolean patternFound = false;
		while (startByte <= 5) {
			if (((int)scanData[startByte+2] & 0xff) == 0x02 &&
				((int)scanData[startByte+3] & 0xff) == 0x15) {			
				// 这是一个IBeacon	
				patternFound = true;
				break;
			}
			else if (((int)scanData[startByte] & 0xff) == 0x2d &&
					((int)scanData[startByte+1] & 0xff) == 0x24 &&
					((int)scanData[startByte+2] & 0xff) == 0xbf &&
					((int)scanData[startByte+3] & 0xff) == 0x16) {
                iBeacon iBeacon = new iBeacon();
				iBeacon.major = 0;
				iBeacon.minor = 0;
				iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
				iBeacon.txPower = -55;
				return iBeacon;
			}
            else if (((int)scanData[startByte] & 0xff) == 0xad &&
                     ((int)scanData[startByte+1] & 0xff) == 0x77 &&
                     ((int)scanData[startByte+2] & 0xff) == 0x00 &&
                     ((int)scanData[startByte+3] & 0xff) == 0xc6) {
                   
                    iBeacon iBeacon = new iBeacon();
                    iBeacon.major = 0;
                    iBeacon.minor = 0;
                    iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
                    iBeacon.txPower = -55;
                    return iBeacon;
            }
			startByte++;
		}
		if (patternFound == false) {
			// 没有找到IBeacon
			return null;
		}
		//整理并记录IBeacon数据
		iBeacon iBeacon = new iBeacon();
		iBeacon.major = (scanData[startByte+20] & 0xff) * 0x100 + (scanData[startByte+21] & 0xff);
		iBeacon.minor = (scanData[startByte+22] & 0xff) * 0x100 + (scanData[startByte+23] & 0xff);
		iBeacon.txPower = (int)scanData[startByte+24];
		iBeacon.rssi = rssi;
		byte[] proximityUuidBytes = new byte[16];
		System.arraycopy(scanData, startByte+4, proximityUuidBytes, 0, 16); 
		String hexString = bytesToHexString(proximityUuidBytes);
		StringBuilder sb = new StringBuilder();
		sb.append(hexString.substring(0,8));
		sb.append("-");
		sb.append(hexString.substring(8,12));
		sb.append("-");
		sb.append(hexString.substring(12,16));
		sb.append("-");
		sb.append(hexString.substring(16,20));
		sb.append("-");
		sb.append(hexString.substring(20,32));
		iBeacon.proximityUuid = sb.toString();
        if (device != null) {
            iBeacon.bluetoothAddress = device.getAddress();
            iBeacon.name = device.getName();
        }
		return iBeacon;
	}
    /**
	 * 字节Byte转换成十六进制字符串（bytesToHexString）
	 * @param src 待转换字节
	 */
    private static String bytesToHexString(byte[] src){  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < src.length; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString();  
    }  
}
