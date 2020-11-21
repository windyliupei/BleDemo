package win.lioil.bluetooth.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.os.SystemClock;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import win.lioil.bluetooth.SplitPackage;
import win.lioil.bluetooth.util.NonReEnterLock;
import win.lioil.bluetooth.util.Util;

import static win.lioil.bluetooth.ble.BleServerActivity.UUID_CHAR_WRITE_NOTIFY;

public class BleClientSender {

    //As a Client sender, we need:
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mService;

    public BleClientSender(BluetoothGatt bluetoothGatt,BluetoothGattService service){
        mBluetoothGatt = bluetoothGatt;
        mService = service;
    }


    private LinkedList<byte[]> mReqBytes;

    private NonReEnterLock lock = new NonReEnterLock();

    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();



    public void sendMessage(String text) throws Exception {

        //进入锁，这里是非可重入锁，同一个对象也不能在发送过程中，再发送数据。
        lock.lock();

        mReqBytes = SplitPackage.splitByte(text.getBytes());

        if(mReqBytes!=null && mBluetoothGatt!=null){


            final BluetoothGattCharacteristic characteristic = mService.getCharacteristic(UUID_CHAR_WRITE_NOTIFY);
            final int packageCount = mReqBytes.size();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    for (int index = 0; index < packageCount; index++) {

                        //这里只取得数据，并不删除，当response ack后把确认发送成功的再删掉
                        byte[] peekByte = mReqBytes.get(index);

                        characteristic.setValue(peekByte);
                        mBluetoothGatt.writeCharacteristic(characteristic);

                        Log.i("SENDBLE", "写对方分包儿:" + (index + 1) + "/" + packageCount);
                        Log.i("SENDBLE", "分包儿内容:" + new String(peekByte));
                        Log.i("SENDBLE", "分包儿内容:" + Util.bytesToHex(peekByte));

                        //发送太频繁会断开蓝牙
                        SystemClock.sleep(500);
                    }
                }
            };
            //线程池里去搞，不要每次new 一个线程
            singleThreadExecutor.execute(runnable);
        }
        //释放锁
        lock.unlock();
    }

    //TODO：发送之前丢失的包儿时，包头还需重新计算吗？
    private void sendLostMessage() throws Exception {

        lock.lock();

        if(mReqBytes!=null && mBluetoothGatt!=null){

            final BluetoothGattCharacteristic characteristic = mService.getCharacteristic(UUID_CHAR_WRITE_NOTIFY);
            final int packageCount = mReqBytes.size();


            Runnable runnable = new Runnable() {
                @Override
                public void run() {


                    for (int index = 0; index < packageCount; index++) {

                        byte[] peekByte = mReqBytes.get(index);

                        characteristic.setValue(peekByte);
                        mBluetoothGatt.writeCharacteristic(characteristic);
                        Log.i("SENDBLE", "写入服务端分包儿:" + (index + 1) + "/" + packageCount);
                        Log.i("SENDBLE", "分包儿内容:" + new String(peekByte));
                        Log.i("SENDBLE", "分包儿内容:" + Util.bytesToHex(peekByte));

                        SystemClock.sleep(500);
                    }


                }
            };

            singleThreadExecutor.execute(runnable);
        }

        lock.unlock();
    }


    public void processAckData(byte[] ackData){

        if (ackData==null || ackData.length==0){
            return;
        }

        //分析 ack 包儿，找出丢失的包儿。

        //把已经发送的包儿remove
        mReqBytes.remove(0);

        try {
            sendLostMessage();
        } catch (Exception e) {
            Log.e("SENDBLE","Send lost data error");
        }

    }

}
