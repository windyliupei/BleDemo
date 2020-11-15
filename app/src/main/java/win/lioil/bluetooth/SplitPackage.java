package win.lioil.bluetooth;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import win.lioil.bluetooth.util.Util;

public class SplitPackage {

    public static final int packageSize = 18;
    private static byte[] headByte = new byte[2];



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

        //TODO:Check 是否最大15包

        int headLength = headByte.length;

        if (pkgCount > 0) {
            for (int pkgIndex = 1; pkgIndex <= pkgCount; pkgIndex++) {

                calHead(pkgCount, pkgIndex);

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

    private static void calHead(int pkgCount, int pkgIndex) {
        //TODO:
        headByte[0] = 0x40;

        //算第二位
        String hex= Integer.toHexString(pkgCount*16+pkgIndex);
        byte[] hexBytes = Util.hexToBytes(hex);
        headByte[1] = hexBytes[0];
    }


}
