package win.lioil.bluetooth.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.SystemClock;
import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import win.lioil.bluetooth.PackageRegister;
import win.lioil.bluetooth.SplitPackage;
import win.lioil.bluetooth.util.NonReEnterLock;
import win.lioil.bluetooth.util.Util;

import static win.lioil.bluetooth.ble.BleServerActivity.UUID_CHAR_WRITE_NOTIFY;

public class BleClientSender {

    //As a Client sender, we need:
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mService;
    private LinkedList<byte[]> mReqBytesList = new LinkedList<byte[]>();
    private NonReEnterLock lock = new NonReEnterLock();
    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();


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


        //进入锁，这里是非可重入锁，同一个对象也不能在发送过程中，再发送数据。
        lock.lock();

        mReqBytesList.clear();

        if (text.equals(" ")){
            mReqBytesList = SplitPackage.getPingPkg();
        }else{
            mReqBytesList = SplitPackage.splitByte(text.getBytes());
        }



        if(mReqBytesList !=null && mBluetoothGatt!=null){


            final BluetoothGattCharacteristic characteristic = mService.getCharacteristic(UUID_CHAR_WRITE_NOTIFY);
            final int packageCount = mReqBytesList.size();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    for (int index = 0; index < packageCount; index++) {

                        //这里只取得数据，并不删除，当response ack后把确认发送成功的再删掉
                        byte[] peekByte = mReqBytesList.get(index);

                        characteristic.setValue(peekByte);
                        mBluetoothGatt.writeCharacteristic(characteristic);

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
        if(reqBytesList !=null && mBluetoothGatt!=null){


            final BluetoothGattCharacteristic characteristic = mService.getCharacteristic(UUID_CHAR_WRITE_NOTIFY);
            final int packageCount = reqBytesList.size();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    for (int index = 0; index < packageCount; index++) {

                        //这里只取得数据，并不删除，当response ack后把确认发送成功的再删掉
                        byte[] peekByte = reqBytesList.get(index);

                        characteristic.setValue(peekByte);
                        mBluetoothGatt.writeCharacteristic(characteristic);

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


    //移除对方已经确认收到的包儿
    public void removeSendPkg(){

    }

}
