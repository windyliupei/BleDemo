package win.lioil.bluetooth.util;

import android.util.Log;

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
        if (packageHead.isMsgType()){
            head+=0x01;
        }
        if (packageHead.isReserve2()){
            head+=0x01<<1;
        }
        if (packageHead.isReserve1()){
            head+=0x01<<2;
        }
        if (packageHead.isLastPackage()){
            head+=0x01<<3;
        }
        if (packageHead.isEncP()){
            head+=0x01<<4;
        }
        if (packageHead.isFragmentation()){
            head+=0x01<<5;
        }
        if (packageHead.isPackageToggle()){
            head+=0x01<<6;
        }
        if (packageHead.isAckR()){
            head+=0x01<<7;
        }
        return head;
    }

    public static PackageHead getPkgInfo(byte head) {

        PackageHead packageHead = new PackageHead();
        if((head&1)==1){
            packageHead.setMsgType(true);
        }
        if((head&(1<<1))==1<<1){
            packageHead.setReserve2(true);
        }
        if((head&(1<<2))==1<<2){
            packageHead.setReserve1(true);
        }
        if((head&(1<<3))==1<<3){
            packageHead.setLastPackage(true);
        }
        if((head&(1<<4))==1<<4){
            packageHead.setEncP(true);
        }
        if((head&(1<<5))==1<<5){
            packageHead.setFragmentation(true);
        }
        if((head&(1<<6))==1<<6){
            packageHead.setPackageToggle(true);
        }
        if((head&(1<<7))==1<<7){
            packageHead.setAckR(true);
        }

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
}
