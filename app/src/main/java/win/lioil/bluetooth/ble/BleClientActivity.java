package win.lioil.bluetooth.ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.UUID;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.IPackageNotification;
import win.lioil.bluetooth.MergePackage;
import win.lioil.bluetooth.MockRequestPackages;
import win.lioil.bluetooth.PackageRegister;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.util.Util;

import static win.lioil.bluetooth.ble.BleServerActivity.UUID_CHAR_WRITE_NOTIFY;

import static win.lioil.bluetooth.ble.BleServerActivity.UUID_SERVICE;

/**
 * BLE客户端(主机/中心设备/Central)
 */
public class BleClientActivity extends Activity implements IPackageNotification {
    private static final String TAG = BleClientActivity.class.getSimpleName();
    private EditText mWriteET;
    private TextView mTips;
    private BleDevAdapter mBleDevAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean isConnected = false;

    private boolean packageToggle;

    // 与服务端连接的Callback
    public BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothDevice dev = gatt.getDevice();
            Log.i(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", dev.getName(), dev.getAddress(), status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true;
                gatt.discoverServices(); //启动服务发现
                logTv(String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status), dev));

            } else {
                isConnected = false;
                closeConn();
                logTv(String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status), dev));

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, String.format("onServicesDiscovered:%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), status));
            if (status == BluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                // 遍历获取BLE服务Services/Characteristics/Descriptors的全部UUID
                for (BluetoothGattService service : gatt.getServices()) {
                    StringBuilder allUUIDs = new StringBuilder("UUIDs={\nS=" + service.getUuid().toString());
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        allUUIDs.append(",\nC=").append(characteristic.getUuid());
                        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors())
                            allUUIDs.append(",\nD=").append(descriptor.getUuid());
                    }
                    allUUIDs.append("}");
                    Log.i(TAG, "onServicesDiscovered:" + allUUIDs.toString());
                }

                BluetoothGattService service = getGattService(BleServerActivity.UUID_SERVICE);
                if (service != null) {
                    // 设置Characteristic通知
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_WRITE_NOTIFY);//通过UUID获取可通知的Characteristic
                    mBluetoothGatt.setCharacteristicNotification(characteristic, true);

                    // 向Characteristic的Descriptor属性写入通知开关，使蓝牙设备主动向手机发送数据
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BleServerActivity.UUID_DESC_NOTITY);
                    // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);//和通知类似,但服务端不主动发数据,只指示客户端读取数据
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("onCharacteristicRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("读取Characteristic[" + uuid + "]:\n" + valueStr);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("onCharacteristicWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("写入Characteristic[" + uuid + "]:\n" + Util.bytesToHex(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            byte[] msg = characteristic.getValue();

            //是个ack的回复包儿，需要重发丢失的包儿
            if(Util.getPkgInfo(msg[0]).isMsgType()){
                //这里处理丢包儿，把丢的包儿给Client再发回去
            }else{
                MergePackage.getInstance().appendPackage(msg);
            }


            if(MergePackage.getInstance().isReceiveLastPackage()){
                //Client 最后一包儿后，把Log打出来
                String rspWholeJson = MergePackage.getInstance().exportToJson();
                logTv("收到所有Server的Rsp JSON");
                logTv(rspWholeJson);

            }

            //是个ack包儿，需要回收到了哪些包儿，没收到哪些包儿
            if(Util.getPkgInfo(msg[0]).isAckR()){
                logTv("收到Ack Package");
                writeSinglePackage(Util.getAckRsp(packageToggle));
            }

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = Arrays.toString(descriptor.getValue());
            Log.i(TAG, String.format("onDescriptorRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("读取Descriptor[" + uuid + "]:\n" + valueStr);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = Arrays.toString(descriptor.getValue());
            Log.i(TAG, String.format("onDescriptorWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("写入Descriptor[" + uuid + "]:\n" + valueStr);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleclient);
        RecyclerView rv = findViewById(R.id.rv_ble);
        mWriteET = findViewById(R.id.et_write);
        mTips = findViewById(R.id.tv_tips);
        rv.setLayoutManager(new LinearLayoutManager(this));
        mBleDevAdapter = new BleDevAdapter(new BleDevAdapter.Listener() {
            @Override
            public void onItemClick(BluetoothDevice dev) {
                closeConn();
                mBluetoothGatt = dev.connectGatt(BleClientActivity.this, false, mBluetoothGattCallback); // 连接蓝牙设备
                logTv(String.format("与[%s]开始连接............", dev));
            }
        });
        rv.setAdapter(mBleDevAdapter);

        //注册 Package 的观察者
        PackageRegister.getInstance().addedPackageListener(this);


        //TODO:还不是明白为什么要写在这里？而之前的 read 那个不需要写入
//        BluetoothGattService service = new BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
//        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(UUID_CHAR_WRITE_NOTIFY,
//                BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE);
//        characteristicWrite.addDescriptor(new BluetoothGattDescriptor(UUID_CHAR_WRITE_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE));
//        service.addCharacteristic(characteristicWrite);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeConn();
    }

    // BLE中心设备连接外围设备的数量有限(大概2~7个)，在建立新连接之前必须释放旧连接资源，否则容易出现连接错误133
    private void closeConn() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
    }

    // 扫描BLE
    public void reScan(View view) {
        if (mBleDevAdapter.isScanning)
            APP.toast("正在扫描...", 0);
        else
            mBleDevAdapter.reScan();
    }


    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 写入数据成功会回调->onCharacteristicWrite()
    public void write(View view) {
        BluetoothGattService service = getGattService(UUID_SERVICE);


        if (service != null && isConnected) {
            try {
                String text = mWriteET.getText().toString();



                //目前有2种Client的输入：
                //1。模拟App，这里输入的是 01，02，03。。。。99
                //2。Commission App 输入的{"test":"01"} 这样的

                //m1: 02 7e ba de 00
                final byte[] m1Bbytes = {0x02, 0x7e, (byte) 0xba, (byte) 0xde, 0x00};


                if(Integer.parseInt(text)>=0){
                    //输入的数字，这个时候拼接成，{"test":"01"} 这样的

                    text = String.format("{\"test\":\"%s\"}",text);


                }else{
                    //拼一个2可以内的json
                    text =  MockRequestPackages.generateBigData();
                }

                text = text.replace(" ","");
                text = text.replace("\r\n","");
                text = text.replace("\n","");
                BleClientSender bleClientSender = new BleClientSender(mBluetoothGatt,service);
                bleClientSender.sendMessage(text);

            }catch (Exception e){
                logTv("写入服务端错误！");
            }
        }else{
            APP.toast("请链接蓝牙设备!", 0);
        }

    }



    // 获取Gatt服务
    private BluetoothGattService getGattService(UUID uuid) {
        if (!isConnected) {
            APP.toast("没有连接", 0);
            return null;
        }
        BluetoothGattService service = mBluetoothGatt.getService(uuid);
        if (service == null)
            APP.toast("没有找到服务UUID=" + uuid, 0);
        return service;
    }

    // 输出日志
    public void logTv(final String msg) {
        if (isDestroyed())
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                APP.toast(msg, 0);
                mTips.append(msg + "\n\n");
            }
        });
    }

    @Override
    public void receiveLastPackage() {

    }

    private void writeSinglePackage(byte[] singleByte){
        BluetoothGattService service = getGattService(UUID_SERVICE);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_WRITE_NOTIFY);//通过UUID获取可写的Characteristic

        characteristic.setValue(singleByte);
        mBluetoothGatt.writeCharacteristic(characteristic);
        logTv("发送 Ack Package:"+Util.bytesToHex(singleByte));
    }
}