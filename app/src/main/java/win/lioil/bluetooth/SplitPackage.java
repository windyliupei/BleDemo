package win.lioil.bluetooth;

import java.util.LinkedList;
import java.util.Queue;

import win.lioil.bluetooth.util.Util;

public class SplitPackage {

    //每个小包二的 payload
    public static final int packageSize = 17;
    //头包变成了3个
    private static byte[] headByte = new byte[3];
    //目前支持127个包儿一组
    private static final int packageCount = 127;




    public static LinkedList<byte[]> splitByte(byte[] wholeData) {

        LinkedList<byte[]> byteQueue = new LinkedList<>();
        int pkgWholeCount;
        if (wholeData.length % packageSize == 0) {
            pkgWholeCount = wholeData.length / packageSize;
        } else {
            pkgWholeCount = Math.round((wholeData.length / packageSize) + 1);
        }

        boolean packageToggle = false;
        int pkgToggleCount = 1;

        int headLength = headByte.length;

        if (pkgWholeCount > 0) {
            for (int pkgWholeIndex = 1; pkgWholeIndex <= pkgWholeCount; pkgWholeIndex++) {

                if(pkgToggleCount>=packageCount){
                    packageToggle =!packageToggle;
                    //每127包儿重新计数
                    pkgToggleCount = 0;
                }

                calHead(packageToggle,pkgWholeCount, pkgWholeIndex);

                pkgToggleCount++;

                byte[] dataPkg;
                int lastPkgLength;

                //只有一包儿或最后一包的处理
                if (pkgWholeCount == 1 || pkgWholeIndex == pkgWholeCount) {

                    //包儿的长度
                    lastPkgLength = wholeData.length % packageSize == 0 ? packageSize : wholeData.length % packageSize;
                    dataPkg = new byte[lastPkgLength+headLength];
                    System.arraycopy(headByte, 0, dataPkg, 0, headByte.length);
                    System.arraycopy(wholeData, (pkgWholeIndex-1) * packageSize, dataPkg, headByte.length, lastPkgLength);
                } else {

                    dataPkg = new byte[packageSize+headLength];
                    System.arraycopy(headByte, 0, dataPkg, 0, headByte.length);
                    System.arraycopy(wholeData, (pkgWholeIndex-1) * packageSize, dataPkg, headByte.length, packageSize);
                }
                byteQueue.offer(dataPkg);
            }
        }

        return byteQueue;
    }


    public static void calHead(boolean packageToggle, int pkgWholeCount, int pkgWholeIndex) {
        PackageHead packageHead = new PackageHead();
        //127包儿内的index
        int pkgIndex;
        //127包儿内的count
        int eachPackageCount;
        int[] countAndIndex = Util.getCountAndIndex(pkgWholeCount, packageCount, pkgWholeIndex);
        eachPackageCount = countAndIndex[0];
        pkgIndex = countAndIndex[1];

        //ACK required = 1, ACK not required = 0
        //一般会在第127包需要给设备端发一个ack，以便使设备端检查并返回丢失的包儿
        //这个ack是“要求设备ack”
        packageHead.setAckR(eachPackageCount==pkgIndex);

        //packageToggle : when this package set 0, next package is set 1. Inversely, this package set 1, next set 0
        //包儿的 toggle 0，1 交替
        packageHead.setPackageToggle(packageToggle);

        //每127包儿要发一个“中包儿”的标记位
        //1: this is Finish frame, not have next frame, need ack, 0: have next frame
        packageHead.setFragmentation(eachPackageCount==pkgIndex);

        //不是加密包儿
        packageHead.setEncP(false);

        //是否“业务包”的最后一包儿
        packageHead.setLastPackage(pkgWholeCount==pkgWholeIndex);

        //message type (data, ACK)
        //0x00 – data fragmentation
        //0x01 – acknowledge for received message (ACK)，这个ack是“响应设备ack”
        //数据包儿不需要相应ack
        packageHead.setMsgType(false);
        headByte[0] = Util.getHead(packageHead);

        //算第二位,包儿的count （1~127）
        String countHex= Integer.toHexString(eachPackageCount);
        byte[] hexBytes = Util.hexToBytes(countHex);
        headByte[1] = hexBytes[0];

        //算第三位,包儿的index （1~127）
        String indexHex= Integer.toHexString(pkgIndex);
        headByte[2] = Util.hexToBytes(indexHex)[0];

        System.out.println(Util.bytesToHex(headByte));
    }


}
