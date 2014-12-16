package com.example.bluetooth.le;
import java.util.List;  
import com.example.bluetooth.le.iBeaconClass.iBeacon;
/**
 * 对SQLite操作增删改查接口（SQLOperate）
 * @author yan
 */
public interface SQLOperate {  
    public void add(iBeacon p,String name);  
    public void delete(String mac);  
    public void update(iBeacon p);  
    public List<iBeacon> find();  
}  