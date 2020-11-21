package win.lioil.bluetooth.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.os.SystemClock;
import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import win.lioil.bluetooth.SplitPackage;
import win.lioil.bluetooth.util.NonReEnterLock;
import win.lioil.bluetooth.util.Util;


public class BleServerSender {

    //As a server sender, we need:
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothDevice mClientdevice;


    private LinkedList<byte[]> mReqBytes;

    private NonReEnterLock lock = new NonReEnterLock();

    final private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    public BleServerSender(BluetoothGattCharacteristic characteristic,
                           BluetoothGattServer bluetoothGattServer,BluetoothDevice clientdevice) {
        mBluetoothGattServer = bluetoothGattServer;
        mCharacteristic = characteristic;
        mClientdevice = clientdevice;
    }


    public void sendMessage(String text) throws Exception {

        //进入锁，这里是非可重入锁，同一个对象也不能在发送过程中，再发送数据。
        lock.lock();

        mReqBytes = SplitPackage.splitByte(text.getBytes());

        if(mBluetoothGattServer!=null && mCharacteristic!=null && mClientdevice!=null){

            final int packageCount = mReqBytes.size();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    for (int index = 0; index < packageCount; index++) {

                        //这里只取得数据，并不删除，当response ack后把确认发送成功的再删掉
                        byte[] peekByte = mReqBytes.get(index);

                        mCharacteristic.setValue(peekByte);
                        mBluetoothGattServer.notifyCharacteristicChanged(mClientdevice, mCharacteristic, false);

                        Log.i("SENDBLE", "写对方分包儿:" + (index + 1) + "/" + packageCount);
                        Log.i("SENDBLE", "分包儿内容:" + new String(peekByte));
                        Log.i("SENDBLE", "分包儿内容:" + Util.bytesToHex(peekByte));

                        //发送太频繁会断开蓝牙
                        SystemClock.sleep(100);
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

        if(mBluetoothGattServer!=null && mCharacteristic!=null){

            final int packageCount = mReqBytes.size();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    for (int index = 0; index < packageCount; index++) {

                        //这里只取得数据，并不删除，当response ack后把确认发送成功的再删掉
                        byte[] peekByte = mReqBytes.get(index);

                        mCharacteristic.setValue(peekByte);
                        mBluetoothGattServer.notifyCharacteristicChanged(mClientdevice, mCharacteristic, false);

                        Log.i("SENDBLE", "写对方分包儿:" + (index + 1) + "/" + packageCount);
                        Log.i("SENDBLE", "分包儿内容:" + new String(peekByte));
                        Log.i("SENDBLE", "分包儿内容:" + Util.bytesToHex(peekByte));
                        //发送太频繁会断开蓝牙
                        SystemClock.sleep(500);
                    }
                }
            };

            singleThreadExecutor.execute(runnable);
        }

        lock.unlock();
    }


    //TODO：
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
