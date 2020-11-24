package win.lioil.bluetooth.ble;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class ReceiveDataManager extends ConcurrentHashMap<Integer, ByteBuffer> {


    public boolean isAllReceived(){
       return entrySet().stream().allMatch(it->it.getValue().position()>0);
    }

    @Override
    public void clear(){
        entrySet().forEach(it->it.getValue().reset());
    }

    public String expString(){

        StringBuffer sb = new StringBuffer();
        for (int index = 0; index< 127; index++){

            ByteBuffer byteBuffer = this.get(index);
            //复位 buffer,开始读
            byteBuffer.position(3);

            byte[] dst = new byte[17];
            byteBuffer.get(dst);

            sb.append(new String(dst));

            //复位 buffer,开始写
            byteBuffer.compact();


        }
        return sb.toString();
    }

}
