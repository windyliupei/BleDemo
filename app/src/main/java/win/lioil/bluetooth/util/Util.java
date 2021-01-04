package win.lioil.bluetooth.util;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import win.lioil.bluetooth.PackageHead;

public class Util {

    public static final int SENDINTERVAL = 100;
    private static final String TAG = Util.class.getSimpleName();
    public static final Executor EXECUTOR = Executors.newCachedThreadPool();

    public static void mkdirs(String filePath) {
        boolean mk = new File(filePath).mkdirs();
        Log.d(TAG, "mkdirs: " + mk);
    }

    public static String bytesToHex(byte[] buffer) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);
        for (int i = 0; i < buffer.length; i++) {
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        hex = hex.length() % 2 != 0 ? "0" + hex : hex;

        byte[] b = new byte[hex.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;

    }



    public static byte getHead(PackageHead packageHead) {
        byte head = 0x00;
        if (packageHead.isMsgType()){
            head = (byte) (head | 0x01);
        }
        if (packageHead.isReserve2()){
            head = (byte) (head | 0x02);
        }
        if (packageHead.isReserve1()){
            head = (byte) (head | 0x04);
        }
        if (packageHead.isLastPackage()){
            head = (byte) (head | 0x08);
        }
        if (packageHead.isEncP()){
            head = (byte) (head | 0x10);
        }
        if (packageHead.isFragmentation()){
            head = (byte) (head | 0x20);
        }
        if (packageHead.isPackageToggle()){
            head = (byte) (head | 0x40);
        }
        if (packageHead.isAckR()){
            head = (byte) (head | 0x80);
        }
        return head;
    }

    public static PackageHead getPkgInfo(byte head) {


        PackageHead packageHead = new PackageHead();

        packageHead.setAckR((head & 0x80) == 0x80);
        packageHead.setPackageToggle((head & 0x40) == 0x40);
        packageHead.setFragmentation((head & 0x20) == 0x20);
        packageHead.setEncP((head & 0x10) == 0x10);
        packageHead.setLastPackage((head & 0x08) == 0x08);
        packageHead.setReserve1((head & 0x04) == 0x04);
        packageHead.setReserve2((head & 0x02) == 0x02);
        packageHead.setMsgType((head & 0x01) == 0x01);

        return packageHead;
    }




    public static int[] getCountAndIndex(int totalPackage, int arraySize, int currentIndex){

        int totalPart = (int) Math.ceil((double) totalPackage / (double)arraySize);
        //0:count,1:index
        int[] result = new int[2];

        for (int index= 0; index < totalPart; index++){
            if (index == totalPart-1 ){
                if ( currentIndex >= (index*arraySize+1) &&  currentIndex <= totalPackage){
                    result[0] = totalPackage - (index*arraySize+1) +1;
                    result[1] = currentIndex - (index*arraySize+1) +1;
                }
            }else{
                if ( currentIndex >= (index*arraySize+1) &&  currentIndex <= (index*arraySize+arraySize)){
                    result[0] = (index*arraySize+arraySize) - (index*arraySize+1) +1;
                    result[1] = currentIndex - (index*arraySize+1) +1;
                }
            }
        }

        return result;
    }

    //TODO:
    public static byte[] getAckRsp(boolean packageToggle){

        PackageHead head = new PackageHead();
        head.setAckR(false);
        head.setPackageToggle(!packageToggle);
        head.setFragmentation(true);
        head.setEncP(false);
        head.setLastPackage(true);
        head.setReserve1(false);
        head.setReserve2(false);
        head.setMsgType(true);

        byte[] ackRsp = new byte[20];
        ackRsp[0] = Util.getHead(head);

        ackRsp[1] = 0x01;
        ackRsp[2] = 0x01;
        ackRsp[3] = 0x00;

        for (int index = 4;index<20;index++){
            ackRsp[index] = 0x00;
        }

        return ackRsp;
    }

    public static byte[] getPing(){

        PackageHead head = new PackageHead();
        head.setAckR(false);
        head.setPackageToggle(false);
        head.setFragmentation(false);
        head.setEncP(false);
        head.setLastPackage(false);
        head.setReserve1(false);
        head.setReserve2(false);
        head.setMsgType(false);

        byte[] ackRsp = new byte[20];
        ackRsp[0] = Util.getHead(head);

        ackRsp[1] = 0x01;
        ackRsp[2] = 0x01;
        ackRsp[3] = 0x00;

        for (int index = 4;index<20;index++){
            ackRsp[index] = (byte) 0x99;
        }



        return ackRsp;
    }

    public static boolean isPing(byte[] data){

       /* boolean isPing = true;
        for (int index = 4;index<20;index++){
            if(data[index] != (byte) 0x99){
                isPing = false;
                break;
            }
        }
        return isPing;*/
        return false;
    }

    /*
    处理丢包和Ack
    * */
    private static List<Integer> getPkgIndex(byte byteData,int index) {

        //丢包儿数据的byte[]是从 index 为4，开始描述丢包儿信息的
        //index=5:8~1包儿
        //index=6:16~9包儿
        //...

        int offset = (index - 4)*8;

        List<Integer> lst = new LinkedList<>();
        //第8位丢失
        if(((byteData & 0x80) == 0x80)){
            lst.add(8+offset);
        }
        //第7位丢失
        if(((byteData & 0x40) == 0x40)){
            lst.add(7+offset);
        }
        //第6位丢失
        if(((byteData & 0x20) == 0x20)){
            lst.add(6+offset);
        }
        //第5位丢失
        if(((byteData & 0x10) == 0x10)){
            lst.add(5+offset);
        }
        //第4位丢失
        if(((byteData & 0x08) == 0x08)){
            lst.add(4+offset);
        }
        //第3位丢失
        if(((byteData & 0x04) == 0x04)){
            lst.add(3+offset);
        }//第2位丢失
        if(((byteData & 0x02) == 0x02)){
            lst.add(2+offset);
        }
        //第1位丢失
        if(((byteData & 0x01) == 0x01)){
            lst.add(1+offset);
        }
        return lst;
    }




    public static List<Integer> getLostPkgIndex(byte[] wholeLostPkgByte){

        //Remove 掉前四位（20-4），只留丢包的有效数据
        byte[] lostPkgByte = new byte[16];
        System.arraycopy(wholeLostPkgByte,4,lostPkgByte,0,16);

        List<Integer> resultIndex = new ArrayList<>();
        for (int index = 0;index<lostPkgByte.length;index++){
            resultIndex.addAll(Util.getPkgIndex(lostPkgByte[index],index+4));
        }
        return resultIndex;
    }

    public static byte[] getLostPkgByte(List<Integer> lostPkgs){

        byte[] lostBytes = new byte[16];
        byte[] calArray = new byte[]{
                0x01,0x02,0x04,0x08,0x10,0x20,0x40, (byte) 0x80
        };

        //pkgIdx 丢包儿的index 从1开始
        lostPkgs.forEach(pkgIdx->{

            Integer socketLostByteArrayIndex = Double.valueOf(Math.ceil((pkgIdx-8)/8.0)).intValue();
            Integer idx = ((pkgIdx + 8) % 8)==0 ? 8: ((pkgIdx + 8) % 8);
            //System.out.println(pkgIdx);
            lostBytes[socketLostByteArrayIndex] = (byte) (lostBytes[socketLostByteArrayIndex] | calArray[idx-1]);
        });

        return lostBytes;

    }

    public static byte[] getFullLostPkgByte(List<Integer> lostPkgs){

        byte[] wholePck = new byte[20];
        byte[] lostPkgByte = getLostPkgByte(lostPkgs);

        byte[] ackPkgHead = new byte[4];
        ackPkgHead[0] = 0x01;
        ackPkgHead[1] = 0x01;
        ackPkgHead[2] = 0x01;
        ackPkgHead[3] = 0x00;

        System.arraycopy(ackPkgHead,0,wholePck,0,ackPkgHead.length);
        System.arraycopy(lostPkgByte,0,wholePck,4,lostPkgByte.length);

        return wholePck;

    }

}
