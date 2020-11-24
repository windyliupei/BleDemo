package win.lioil.bluetooth.util;

import win.lioil.bluetooth.PackageRegister;

public class BleLog {

    public static void w(int index, int packageCount,byte[] peekByte) {
        String pkgType = "包儿类型：";
        if (Util.getPkgInfo(peekByte[0]).isAckR()){
            pkgType += "Ack,";
        }
        if (Util.getPkgInfo(peekByte[0]).isFragmentation()){
            pkgType += "Frag,";
        }
        if (Util.getPkgInfo(peekByte[0]).isLastPackage()){
            pkgType += "Last,";
        }
        if (Util.getPkgInfo(peekByte[0]).isMsgType()){
            pkgType += "Msg,";
        }
        if (pkgType.equals("包儿类型：")){
            pkgType += "正常包儿:";
        }

        pkgType+="Togg:"+Util.getPkgInfo(peekByte[0]).isPackageToggle();

        PackageRegister.getInstance().log("!#############写入对方分包儿############!");
        PackageRegister.getInstance().log("1.&Page&:" + (index + 1) + "/" + packageCount);
        PackageRegister.getInstance().log("2.&&"+pkgType);
        PackageRegister.getInstance().log("3.&分包儿内容&:" + new String(peekByte));
        PackageRegister.getInstance().log("4.&分包儿内容&:" + Util.bytesToHex(peekByte));
    }

    public static void r(byte[] msg) {
        String pkgType = "包儿类型：";
        if (Util.getPkgInfo(msg[0]).isAckR()){
            pkgType += "Ack,";
        }
        if (Util.getPkgInfo(msg[0]).isFragmentation()){
            pkgType += "Frag,";
        }
        if (Util.getPkgInfo(msg[0]).isLastPackage()){
            pkgType += "Last,";
        }
        if (Util.getPkgInfo(msg[0]).isMsgType()){
            pkgType += "Msg,";
        }
        if (pkgType.equals("包儿类型：")){
            pkgType += "正常包儿:";
        }

        pkgType+="Togg:"+Util.getPkgInfo(msg[0]).isPackageToggle();

        byte currentPkgIndex = msg[2];
        byte pageCount = msg[1];

        PackageRegister.getInstance().log("!------------------收到对方分包儿--------------------------!");
        PackageRegister.getInstance().log("1.&Page&:"+currentPkgIndex + "/" + pageCount);
        PackageRegister.getInstance().log("2.&&"+pkgType);
        PackageRegister.getInstance().log("3.&分包儿内容&:" + new String(msg));
        PackageRegister.getInstance().log("4.&分包儿内容&:" + Util.bytesToHex(msg));
    }

}
