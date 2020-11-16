package win.lioil.bluetooth;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;


import win.lioil.bluetooth.util.Util;

public class MergePackage {

    public static MergePackage getInstance() {
        return MergePackageHolder.sBleManager;
    }

    private static class MergePackageHolder {
        private static final MergePackage sBleManager = new MergePackage();
    }

    //按 Unicode编码，一个英文占2个字节，初始1M，远大于一个中包： 127
    ByteBuffer buffer = ByteBuffer.allocate(1024*1024);

    volatile boolean receiveLastPackage;
    public boolean isReceiveLastPackage() {
        return receiveLastPackage;
    }

    public synchronized void setReceiveLastPackage(boolean receiveLastPackage) {
        //TODO：给 Android 界面发通知,if true
        this.receiveLastPackage = receiveLastPackage;
        if (receiveLastPackage){
            PackageRegister.getInstance().notification();
        }
    }



    public boolean appendPackage(byte[] littlePkg){

        if (!isReceiveLastPackage()){
            //去掉前两位
            for (int i = 2; i < littlePkg.length; i++) {
                if (littlePkg[i] != 0x00) {
                    buffer.put(littlePkg[i]);
                }
            }
        }

        checkLastPackage(littlePkg);

        return true;
    }

    public String exportToJson(){
        if (this.isReceiveLastPackage()){
            Charset charset = Charset.forName("utf-8");

            //复位 buffer,开始读
            buffer.flip();
            String result = charset.decode(buffer).toString();
            //复位 buffer,开始写
            buffer.compact();
            setReceiveLastPackage(false);


            return result;
        }
        return "";
    }




    private void checkLastPackage(byte[] littlePkg){

        String hex = Util.bytesToHex(littlePkg);
        String totalHex = hex.substring(2,3);
        String currentHex = hex.substring(3,4);

        int total = Integer.parseInt(totalHex, 16);
        int current = Integer.parseInt(currentHex,16);

        //如果当前是第一个包，而且BufferByte 不是起始位置，说明之前的包儿没传完，又来了新包儿
        //数据乱了
        if (current ==1  && buffer.position()>SplitPackage.packageSize){
            //复位 buffer
            buffer.clear();
            setReceiveLastPackage(false);
        }
        //TODO:还得判断以地位是否 ack｜fragmentation
        //1010 or 1110
        setReceiveLastPackage(current==total
                && buffer.position()>1);
    }





}
