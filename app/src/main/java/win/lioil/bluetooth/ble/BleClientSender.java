package win.lioil.bluetooth.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.SystemClock;
import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import win.lioil.bluetooth.SplitPackage;
import win.lioil.bluetooth.util.BleLog;
import win.lioil.bluetooth.util.NonReEnterLock;

import static win.lioil.bluetooth.ble.BleServerActivity.UUID_CHAR_WRITE_NOTIFY;
import static win.lioil.bluetooth.util.Util.SENDINTERVAL;

public class BleClientSender {

    //As a Client sender, we need:
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mService;
    private NonReEnterLock lock = new NonReEnterLock();
    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean stopReceive = false;





    public static BleClientSender getInstance(BluetoothGatt bluetoothGatt,BluetoothGattService service) {
        BleClientSender sender = BleClientSenderHolder.sBleManager;
        sender.mService = service;
        sender.mBluetoothGatt = bluetoothGatt;
        return sender;
    }



    private static class BleClientSenderHolder {
        private static final BleClientSender sBleManager = new BleClientSender();
    }

    private BleClientSender(){}



    public void sendMessage(String text) throws InterruptedException {

        LinkedList<byte[]> mReqBytesList = new LinkedList<byte[]>();
        if (text.equals(" ")){
            mReqBytesList = SplitPackage.getPingPkg();
        }else{
            mReqBytesList = SplitPackage.splitByte(text.getBytes());
        }

        SendDataManager.getInstance().reNew(mReqBytesList);

        this.sendMessage(mReqBytesList);
    }

    public void sendMessage(LinkedList<byte[]> reqBytesList) throws InterruptedException {
        //进入锁，这里是非可重入锁，同一个对象也不能在发送过程中，再发送数据。
        lock.lock();
        if(reqBytesList !=null && mBluetoothGatt!=null){


            final BluetoothGattCharacteristic characteristic = mService.getCharacteristic(UUID_CHAR_WRITE_NOTIFY);
            final int packageCount = reqBytesList.size();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    for (int index = 0; index < packageCount; index++) {

                        if (stopReceive){
                            Log.i("STOP","Stop sending...");
                            continue;
                        }

                        //这里只取得数据，并不删除，当response ack后把确认发送成功的再删掉
                        byte[] peekByte = reqBytesList.get(index);

                        characteristic.setValue(peekByte);
                        mBluetoothGatt.writeCharacteristic(characteristic);

                        BleLog.w(index, packageCount,peekByte);

                        if (index+1 == packageCount){
                            TaskCommunicator taskCommunicator = TaskCommunicator.getInstance();
                            taskCommunicator.startTimer();

                        }

                        //发送太频繁会断开蓝牙
                        SystemClock.sleep(SENDINTERVAL);
                    }
                }
            };
            //线程池里去搞，不要每次new 一个线程
            singleThreadExecutor.execute(runnable);
        }
        //释放锁
        lock.unlock();
    }

    public void stopSend(){
        stopReceive = true;
    }

    public void startSend() {
        stopReceive = false;
    }

}
