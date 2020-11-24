package win.lioil.bluetooth.ble;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class ReceiveDataManager extends ConcurrentHashMap<Integer, ByteBuffer> {


    private ReceiveDataManager(){
        for (Integer index = 1; index<= 127; index++){
            this.put(index,ByteBuffer.allocate(20));
        }
    }
    //静态内部类实现线程安全的单例模式
    private static class ReceiveDataManagerHolder{
        static ReceiveDataManager instance = new ReceiveDataManager();
    }
    public static ReceiveDataManager getInstance(){
        return ReceiveDataManagerHolder.instance;
    }


    public boolean isAllReceived(Integer packageCount){
       for(Integer index = 1;index <= packageCount;index++){
           if (this.get(index).position() <1){
               return false;
           }
        }
       return true;
    }




    @Override
    public void clear(){
        //entrySet().forEach(it->it.getValue().reset());
        for (int index = 1; index<= 127; index++){
            ByteBuffer byteBuffer = this.get(index);
            this.put(index, (ByteBuffer) byteBuffer.clear());
        }
    }

    public String expString(){

        StringBuffer sb = new StringBuffer();
        try{
            for (int index = 1; index<= 127; index++){

                ByteBuffer byteBuffer = this.get(index);
                if (byteBuffer.position()>1){
                    //复位 buffer,开始读
                    byteBuffer.flip();

                    int dstLength = byteBuffer.limit() -3;
                    byte[] dst = new byte[dstLength];
                    for (int dstIndex = 0;dstIndex<dstLength;dstIndex++){
                        dst[dstIndex] = byteBuffer.get(dstIndex+3);
                    }
                    String s = new String(dst);
                    sb.append(s);
                }
                byteBuffer.clear();
            }
        }catch (Exception e){

        }

        return sb.toString();
    }

}
