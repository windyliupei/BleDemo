package win.lioil.bluetooth.ble;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.stream.Stream;

import win.lioil.bluetooth.PackageRegister;
import win.lioil.bluetooth.util.Util;

public class BleReceiver {

    private LinkedList<ByteBuffer> mReqBytesList;
    private boolean currentToggle = false;


    public static BleReceiver getInstance(byte[] data) {

        BleReceiver sender = BleReceiverHolder.sBleManager;

        if(!Util.isPing(data)){
            if (sender.mReqBytesList == null){
                sender.mReqBytesList = new LinkedList<>();
                byte count = data[1];
                for (int index =0;index<count;index++){
                    sender.mReqBytesList.add(ByteBuffer.allocate(20));
                }
            }else{
                //是一个反转包儿，证明不是一个业务包儿了
                if(Util.getPkgInfo(data[0]).isPackageToggle()!=sender.currentToggle){
                    sender.mReqBytesList.clear();
                    byte count = data[1];
                    for (int index =0;index<count;index++){
                        sender.mReqBytesList.add(ByteBuffer.allocate(20));
                    }
                    sender.currentToggle = Util.getPkgInfo(data[0]).isPackageToggle();
                }
            }
        }
        return sender;
    }

    private static class BleReceiverHolder {
        private static final BleReceiver sBleManager = new BleReceiver();
    }

    private BleReceiver(){}


    public LinkedList<byte[]> receiveData(byte[] data){

        if(Util.isPing(data)){
            return null;
        }

        LinkedList<byte[]> needSendPkg = new LinkedList<byte[]>();
        byte currentPkgIndex = data[2];
        byte pageCount = data[1];



        //对方ack 应答的回复，包含对方已经收到包儿的信息
        //这种包儿不包含真实业务的payload的
        if (Util.getPkgInfo(data[0]).isMsgType()){
            for (int i = 0; i < mReqBytesList.size(); i++) {
                //如果第n包儿没有收到消息
                if( mReqBytesList.get(i).position()==0){
                    //TODO：先不发丢包儿
                }
            }
        }else{
            for (int i = 0; i < data.length; i++) {
                mReqBytesList.get(currentPkgIndex-1).put( data[i]);
            }
        }

        //需要应答给对方，哪些没收到,需要发一个 msgType 为1的包
        if(Util.getPkgInfo(data[0]).isAckR()){
            //TODO：先不发丢包儿
            //目前告诉对方：我都收到了
            byte[] ackRsp = Util.getAckRsp(Util.getPkgInfo(data[0]).isPackageToggle());
            //needSendPkg.add(ackRsp);
        }

        boolean allReceived = isReceiveAll() ;
        if (allReceived){
            //通知所有订阅者 package 收齐了，可以显示界面了
            PackageRegister.getInstance().notification();
        }

        return needSendPkg;
    }


    private boolean isReceiveAll(){
        Stream<ByteBuffer> stream = mReqBytesList.stream();
        boolean allMatch = stream.allMatch(byteBuff -> byteBuff.position() != 0);
        return allMatch;
    }

    private boolean isReceiveLastPkg(byte[] data) {

        byte count = data[1];
        byte currentPkgIndex = data[2];


        //要看看对方是否发来了最后一包
        boolean isReceiveLastPkg = (count==currentPkgIndex)
                && Util.getPkgInfo(data[0]).isFragmentation()
                //一般业务包被设计为最大127包儿，也就是一个帧，所以Fragmentation 为1时，LastPackage也为1
                && Util.getPkgInfo(data[0]).isLastPackage();
        return isReceiveLastPkg;
    }

    //这个方法里会清空mReqBytesList，所以必须在所有的包儿收到之后才能调用。
    public String exportToJson(){

        StringBuffer sb = new StringBuffer();

            for (int index = 0; index< mReqBytesList.size(); index++){

                ByteBuffer byteBuffer = mReqBytesList.get(index);
                //复位 buffer,开始读
                byteBuffer.position(3);

                byte[] dst = new byte[17];
                byteBuffer.get(dst);

                sb.append(new String(dst));

                //复位 buffer,开始写
                byteBuffer.compact();


        }

        mReqBytesList.clear();

        return sb.toString();

    }

}
