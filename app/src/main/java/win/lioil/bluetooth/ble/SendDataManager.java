package win.lioil.bluetooth.ble;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class SendDataManager extends ConcurrentHashMap<Integer, byte[]> {


    private SendDataManager(){
    }

    //静态内部类实现线程安全的单例模式
    private static class SendDataManagerHolder{
        static SendDataManager instance = new SendDataManager();
    }
    public static SendDataManager getInstance(){
        return SendDataManager.SendDataManagerHolder.instance;
    }

    public void reNew(LinkedList<byte[]> splitPkgs){
        for (Integer index = 1; index<= splitPkgs.size(); index++){
            this.put(index,splitPkgs.get(index-1));
        }
    }

}
