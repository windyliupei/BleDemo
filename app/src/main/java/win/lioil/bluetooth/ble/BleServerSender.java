package win.lioil.bluetooth.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.os.SystemClock;
import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import win.lioil.bluetooth.PackageRegister;
import win.lioil.bluetooth.SplitPackage;
import win.lioil.bluetooth.util.NonReEnterLock;
import win.lioil.bluetooth.util.Util;


public class BleServerSender {

    //As a server sender, we need:
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothDevice mDevice;
    private LinkedList<byte[]> mReqBytesList = new LinkedList<byte[]>();
    private NonReEnterLock lock = new NonReEnterLock();
    final private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();


    public static BleServerSender getInstance(BluetoothGattCharacteristic characteristic,
                                              BluetoothGattServer bluetoothGattServer,
                                              BluetoothDevice device, BluetoothGattServerCallback bluetoothGattServerCallback) {
        BleServerSender sender = BleServerSenderHolder.sBleManager;
        sender.mCharacteristic = characteristic;
        sender.mBluetoothGattServer = bluetoothGattServer;
        sender.mDevice = device;

        return sender;
    }

    private static class BleServerSenderHolder {
        private static final BleServerSender sBleManager = new BleServerSender();
    }

    private BleServerSender() {

    }


    public void sendMessage(String text) throws InterruptedException {

        //进入锁，这里是非可重入锁，同一个对象也不能在发送过程中，再发送数据。
        lock.lock();

        mReqBytesList.clear();
        mReqBytesList = SplitPackage.splitByte(text.getBytes());

        if(mBluetoothGattServer!=null && mCharacteristic!=null && mDevice!=null){

            final int packageCount = mReqBytesList.size();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    for (int index = 0; index < packageCount; index++) {

                        //这里只取得数据，并不删除，当response ack后把确认发送成功的再删掉
                        byte[] peekByte = mReqBytesList.get(index);

                        mCharacteristic.setValue(peekByte);
                        mBluetoothGattServer.notifyCharacteristicChanged(mDevice, mCharacteristic, false);

                        //这里偷懒了
                        PackageRegister.getInstance().log("写对方分包儿:" + (index + 1) + "/" + packageCount);
                        PackageRegister.getInstance().log("分包儿内容:" + new String(peekByte));
                        PackageRegister.getInstance().log("分包儿内容:" + Util.bytesToHex(peekByte));

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

    public void sendMessage(LinkedList<byte[]> reqBytesList) throws InterruptedException {

        //进入锁，这里是非可重入锁，同一个对象也不能在发送过程中，再发送数据。
        lock.lock();

        if(reqBytesList !=null && mBluetoothGattServer!=null && mCharacteristic!=null && mDevice!=null){

            final int packageCount = reqBytesList.size();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    for (int index = 0; index < packageCount; index++) {

                        //这里只取得数据，并不删除，当response ack后把确认发送成功的再删掉
                        byte[] peekByte = reqBytesList.get(index);

                        mCharacteristic.setValue(peekByte);
                        mBluetoothGattServer.notifyCharacteristicChanged(mDevice, mCharacteristic, false);

                        //这里偷懒了
                        PackageRegister.getInstance().log("写对方分包儿:" + (index + 1) + "/" + packageCount);
                        PackageRegister.getInstance().log("分包儿内容:" + new String(peekByte));
                        PackageRegister.getInstance().log("分包儿内容:" + Util.bytesToHex(peekByte));
                        /*Log.i("SENDBLE", "写对方分包儿:" + (index + 1) + "/" + packageCount);
                        Log.i("SENDBLE", "分包儿内容:" + new String(peekByte));
                        Log.i("SENDBLE", "分包儿内容:" + Util.bytesToHex(peekByte));*/

                        PackageRegister.getInstance().notification();

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




}
