package win.lioil.bluetooth.util;

import android.util.Log;

import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import win.lioil.bluetooth.PackageHead;

public class Util {
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
        String[] binStrArray = new String[]{"0","0","0","0","0","0","0","0"};
        if (packageHead.isMsgType()){
            binStrArray[7] = "1";
        }
        if (packageHead.isReserve2()){
            binStrArray[6] = "1";
        }
        if (packageHead.isReserve1()){
            binStrArray[5] = "1";
        }
        if (packageHead.isLastPackage()){
            binStrArray[4] = "1";
        }
        if (packageHead.isEncP()){
            binStrArray[3] = "1";
        }
        if (packageHead.isFragmentation()){
            binStrArray[2] = "1";
        }
        if (packageHead.isPackageToggle()){
            binStrArray[1] = "1";
        }
        if (packageHead.isAckR()){
            binStrArray[0] = "1";
        }

        StringBuilder sb = new StringBuilder();
        if (binStrArray != null && binStrArray.length > 0) {
            for (int i = 0; i < binStrArray.length; i++) {
                sb.append(binStrArray[i]);
            }
        }
        String totalStr = sb.toString();
        head = (byte) Long.parseLong(totalStr,2);


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
        head.setPackageToggle(packageToggle);
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

        boolean isPing = true;
        for (int index = 4;index<20;index++){
            if(data[index] != (byte) 0x99){
                isPing = false;
                break;
            }
        }
        return isPing;
    }


}
