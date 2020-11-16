package win.lioil.bluetooth;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import win.lioil.bluetooth.util.Util;

public class SplitPackage {

    public static final int packageSize = 18;
    private static byte[] headByte = new byte[2];
    //目前支持15个包儿一组
    private static final int packageCount = 15;




    public static Queue<byte[]> splitByte(byte[] wholeData) {
        if (packageSize > 18) {
            Log.e("BL","Be careful: split count beyond 18!!");
        }
        Queue<byte[]> byteQueue = new LinkedList<>();
        int pkgCount;
        if (wholeData.length % packageSize == 0) {
            pkgCount = wholeData.length / packageSize;
        } else {
            pkgCount = Math.round((wholeData.length / packageSize) + 1);
        }

        byte packageToggle = 0x00;
        int pkgToggleCount = 1;

        int headLength = headByte.length;

        if (pkgCount > 0) {
            for (int pkgIndex = 1; pkgIndex <= pkgCount; pkgIndex++) {

                //TODO:
                if(pkgToggleCount>=packageCount){
                    if(packageToggle==0x00){
                        packageToggle = 0x04;
                    }else{
                        packageToggle = 0x00;
                    }
                    //每15包儿重新计数
                    pkgToggleCount = 0;
                }

                calHead(packageToggle,pkgCount, pkgIndex);

                pkgToggleCount++;

                byte[] dataPkg;
                int lastPkgLength;

                //只有一包儿或最后一包的处理
                if (pkgCount == 1 || pkgIndex == pkgCount) {

                    //包儿的长度
                    lastPkgLength = wholeData.length % packageSize == 0 ? packageSize : wholeData.length % packageSize;
                    dataPkg = new byte[lastPkgLength+headLength];
                    System.arraycopy(headByte, 0, dataPkg, 0, headByte.length);
                    System.arraycopy(wholeData, (pkgIndex-1) * packageSize, dataPkg, headByte.length, lastPkgLength);
                } else {

                    dataPkg = new byte[packageSize+headLength];
                    System.arraycopy(headByte, 0, dataPkg, 0, headByte.length);
                    System.arraycopy(wholeData, (pkgIndex-1) * packageSize, dataPkg, headByte.length, packageSize);
                }
                byteQueue.offer(dataPkg);
            }
        }

        return byteQueue;
    }

    //packageToggle : when this package set 0, next package is set 1. Inversely, this package set 1, next set 0
    public static void calHead(byte packageToggle, int pkgCount, int pkgIndex) {

        //算第一位

        //ACK required = 1, ACK not required = 0
        byte ackR = 1;

        //1: this is Finish frame, not have next frame, need ack, 0: have next frame
        byte fragmentation = 0;

        //message type (data, ACK)
        //0x00 – data fragmentation
        //0x01 – acknowledge for received message (ACK)
        byte type = 0x00;

        //最后一包儿
        if (pkgIndex == pkgCount){
            ackR = 0x08;
            fragmentation = 0x02;
        }else{
            ackR = 0;
            fragmentation = 0;
        }
        String first = Integer.toHexString(ackR | packageToggle | fragmentation | type);
        //实际上不会大于2
        if (first.length()<2){
            first+="0";//补个0
        }
        headByte[0] = Util.hexToBytes(first)[0];

        //算第二位
        String hex= Integer.toHexString(pkgCount*16+pkgIndex);
        byte[] hexBytes = Util.hexToBytes(hex);
        headByte[1] = hexBytes[0];


    }


}
