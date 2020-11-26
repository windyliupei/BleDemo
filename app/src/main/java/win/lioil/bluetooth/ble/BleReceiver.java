package win.lioil.bluetooth.ble;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.stream.Stream;

import win.lioil.bluetooth.PackageRegister;
import win.lioil.bluetooth.SplitPackage;
import win.lioil.bluetooth.util.Util;

public class BleReceiver {


    private boolean currentToggle = false;
    private volatile boolean stopReceive = false;


    public static BleReceiver getInstance(byte[] data) {

        BleReceiver sender = BleReceiverHolder.sBleManager;

        if(!Util.isPing(data)){
            //是一个反转包儿，证明不是一个业务包儿了
            if(Util.getPkgInfo(data[0]).isPackageToggle()!=sender.currentToggle){
                ReceiveDataManager.getInstance().clear();
                sender.currentToggle = Util.getPkgInfo(data[0]).isPackageToggle();
            }
        }
        return sender;
    }

    private static class BleReceiverHolder {
        private static final BleReceiver sBleManager = new BleReceiver();
    }

    private BleReceiver(){}


    public LinkedList<byte[]> receiveData(byte[] data){

        if (stopReceive){
            return null;
        }

        if(Util.isPing(data)){
            return null;
        }

        LinkedList<byte[]> needSendPkg = new LinkedList<byte[]>();
        Integer currentPkgIndex = Integer.valueOf(data[2]);
        Integer pageCount = Integer.valueOf(data[1]);

        //对方ack 应答的回复，包含对方已经收到包儿的信息
        //这种包儿不包含真实业务的payload的
        if (Util.getPkgInfo(data[0]).isMsgType()){
            //TODO：先不发丢包儿
            //解析那些包儿丢了
            LinkedList<byte[]> lostPkgs = new LinkedList<>();
            needSendPkg = SplitPackage.reCalLostPkg(lostPkgs);
        }else{
            for (int i = 0; i < data.length; i++) {
                ReceiveDataManager.getInstance().get(currentPkgIndex).put(data[i]);
            }
        }

        //需要应答给对方，哪些没收到,需要发一个 msgType 为1的包
        boolean ackR = Util.getPkgInfo(data[0]).isAckR();
        if(ackR){
            //TODO：先不发丢包儿
            //目前告诉对方：我都收到了
            byte[] ackRsp = Util.getAckRsp(Util.getPkgInfo(data[0]).isPackageToggle());
            //needSendPkg.add(ackRsp);

            boolean allReceived = ReceiveDataManager.getInstance().isAllReceived(pageCount) ;
            if (allReceived){
                //通知所有订阅者 package 收齐了，可以显示界面了
                PackageRegister.getInstance().notification();
                TaskCommunicator.getInstance().stopTimer();
            }

        }

        //TODO:现在 Device 不发ack，只能这样写
        //boolean allReceived = ReceiveDataManager.getInstance().isAllReceived(pageCount) ;
        if (isReceiveLastPkg(data)){
            //通知所有订阅者 package 收齐了，可以显示界面了
            PackageRegister.getInstance().notification();
            TaskCommunicator.getInstance().stopTimer();
        }

        return needSendPkg;
    }

    public void stopReceive(){
        stopReceive = true;
        ReceiveDataManager.getInstance().clear();
    }

    public void startReceive(){
        stopReceive = false;
    }


    private boolean isReceiveLastPkg(byte[] data) {

        byte count = data[1];
        byte currentPkgIndex = data[2];


        //要看看对方是否发来了最后一包
        boolean isReceiveLastPkg = (count==currentPkgIndex)
                && Util.getPkgInfo(data[0]).isFragmentation()
                //一般业务包被设计为最大127包儿，也就是一个帧，所以Fragmentation 为1时，LastPackage也为1
                && Util.getPkgInfo(data[0]).isLastPackage();
                //
                //&& Util.getPkgInfo(data[0]).isAckR();
        return isReceiveLastPkg;
    }



}
