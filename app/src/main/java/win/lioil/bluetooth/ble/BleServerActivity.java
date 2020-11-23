package win.lioil.bluetooth.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.TextView;

import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.IPackageNotification;
import win.lioil.bluetooth.PackageRegister;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.util.Util;

/**
 * BLE服务端(从机/外围设备/peripheral)
 */
public class BleServerActivity extends Activity implements IPackageNotification {
    public static final UUID UUID_SERVICE = UUID.fromString("0000b304-1212-efde-1523-785feabcd133"); //自定义UUID
    public static final UUID UUID_CHAR_READ_NOTIFY = UUID.fromString("11000000-0000-0000-0000-000000000000");
    public static final UUID UUID_CHAR_WRITE_NOTIFY = UUID.fromString("0000b831-1212-efde-1523-785feabcd133");
    public static final UUID UUID_DESC_NOTITY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final String TAG = BleServerActivity.class.getSimpleName();
    private TextView mTips;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser; // BLE广播
    private BluetoothGattServer mBluetoothGattServer; // BLE服务端

    private boolean packageToggle;

    private BleReceiver mBleReceiver;
    private BleServerSender mBleServerSender;

    // BLE广播Callback
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            logTv("BLE广播开启成功");
        }

        @Override
        public void onStartFailure(int errorCode) {
            logTv("BLE广播开启失败,错误码:" + errorCode);
        }
    };

    // BLE服务端Callback
    private BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.i(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", device.getName(), device.getAddress(), status, newState));
            logTv(String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status), device));
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.i(TAG, String.format("onServiceAdded:%s,%s", status, service.getUuid()));
            logTv(String.format(status == 0 ? "添加服务[%s]成功" : "添加服务[%s]失败,错误码:" + status, service.getUuid()));
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {

        }

        @Override
        public void onCharacteristicWriteRequest(final BluetoothDevice device, final int requestId, final BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, final int offset, final byte[] requestBytes) {
            // 获取客户端发过来的数据

            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, requestBytes);// 响应客户端

            String pkgType = "包儿类型：";
            if (Util.getPkgInfo(requestBytes[0]).isAckR()){
                pkgType += "Ack,";
            }if (Util.getPkgInfo(requestBytes[0]).isFragmentation()){
                pkgType += "Frag,";
            }if (Util.getPkgInfo(requestBytes[0]).isLastPackage()){
                pkgType += "Last,";
            }if (Util.getPkgInfo(requestBytes[0]).isMsgType()){
                pkgType += "Msg,";
            }
            if (pkgType.equals("包儿类型：")){
                pkgType += "正常包儿:";
            }

            pkgType+="Togg:"+Util.getPkgInfo(requestBytes[0]).isPackageToggle();

            byte currentPkgIndex = requestBytes[2];
            byte pageCount = requestBytes[1];

            logTv("!------------------收到对方分包儿--------------------------!");
            logTv("1.&Page&:"+currentPkgIndex + "/" + pageCount);
            logTv("2.&&"+pkgType);
            logTv("3.&分包儿内容&:" + new String(requestBytes));
            logTv("4.&分包儿内容&:" + Util.bytesToHex(requestBytes));

            mBleReceiver = BleReceiver.getInstance(requestBytes);

            LinkedList<byte[]> needSendBack = mBleReceiver.receiveData(requestBytes);
            mBleServerSender = BleServerSender.getInstance(characteristic,mBluetoothGattServer,device,this);
            if (needSendBack!=null && needSendBack.size()>0){
                if (mBleServerSender !=null){
                    try {
                        mBleServerSender.sendMessage(needSendBack);
                    } catch (InterruptedException e) {
                        logTv("Send back Error!");
                    }
                }
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.i(TAG, String.format("onDescriptorReadRequest:%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, offset, descriptor.getUuid()));
            String response = "DESC_" + (int) (Math.random() * 100); //模拟数据
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response.getBytes()); // 响应客户端
            logTv("客户端读取Descriptor[" + descriptor.getUuid() + "]:\n" + response);
        }

        @Override
        public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            // 获取客户端发过来的数据
            String valueStr = Arrays.toString(value);
            Log.i(TAG, String.format("onDescriptorWriteRequest:%s,%s,%s,%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, descriptor.getUuid(),
                    preparedWrite, responseNeeded, offset, valueStr));
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);// 响应客户端
            logTv("客户端写入Descriptor[" + descriptor.getUuid() + "]:\n" + valueStr);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            Log.i(TAG, String.format("onExecuteWrite:%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, execute));
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            Log.i(TAG, String.format("onNotificationSent:%s,%s,%s", device.getName(), device.getAddress(), status));
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            Log.i(TAG, String.format("onMtuChanged:%s,%s,%s", device.getName(), device.getAddress(), mtu));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleserver);
        mTips = findViewById(R.id.tv_tips);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ============启动BLE蓝牙广播(广告) =================================================================================
        //广播设置(必须)
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //广播模式: 低功耗,平衡,低延迟
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) //发射功率级别: 极低,低,中,高
                .setConnectable(true) //能否连接,广播分为可连接广播和不可连接广播
                .build();
        //广播数据(必须，广播启动就会发送)
        byte[] manufacturerData = generateManufacturerData();
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true) //包含蓝牙名称
                .setIncludeTxPowerLevel(true) //包含发射功率级别
                .addManufacturerData(0x0526, manufacturerData) //设备厂商数据，自定义
                .build();
        //扫描响应数据(可选，当客户端扫描时才发送)
        AdvertiseData scanResponse = new AdvertiseData.Builder()
                .addManufacturerData(0x0526, manufacturerData)//new byte[]{66, 66}) //设备厂商数据，自定义
                .addServiceUuid(new ParcelUuid(UUID_SERVICE)) //服务UUID
//                .addServiceData(new ParcelUuid(UUID_SERVICE), new byte[]{2}) //服务数据，自定义
                .build();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponse, mAdvertiseCallback);

        // 注意：必须要开启可连接的BLE广播，其它设备才能发现并连接BLE服务端!
        // =============F服务端=====================================================================================
        BluetoothGattService service = new BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //添加可写+通知characteristic
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(UUID_CHAR_WRITE_NOTIFY,
                BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristicWrite.addDescriptor(new BluetoothGattDescriptor(UUID_DESC_NOTITY, BluetoothGattCharacteristic.PERMISSION_WRITE));
        service.addCharacteristic(characteristicWrite);

        if (bluetoothManager != null)
            mBluetoothGattServer = bluetoothManager.openGattServer(this, mBluetoothGattServerCallback);
        mBluetoothGattServer.addService(service);

        //注册 Package 的观察者
        PackageRegister.getInstance().addedPackageListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothLeAdvertiser != null)
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        if (mBluetoothGattServer != null)
            mBluetoothGattServer.close();
    }

    public void logTv(final String msg) {
        if (isDestroyed())
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //APP.toast(msg, 0);
                mTips.append(msg + "\n");
            }
        });
    }

    @Override
    public void receiveLastPackage() {
        if (mBleReceiver!=null){
            String exportToJson = mBleReceiver.exportToJson();
            logTv("Receive All From Client:"+exportToJson);

//            if (mBleServerSender !=null){
//                try {
//                    mBleServerSender.sendMessage(exportToJson);
//                } catch (InterruptedException e) {
//                    logTv("Send back Error!");
//                }
//            }
        }
    }

    private byte[] generateManufacturerData(){

        Intent intent = getIntent();
        int online = intent.getIntExtra("online", 0);
        int hubOrSocket = intent.getIntExtra("hubOrSocket",0);

        String mac = getMacFromHardware();
        mac = mac.replace(":","");
        String typeOnline = "";
        if (online==0 && hubOrSocket==0){
            //Socket 离线
            typeOnline = "00";
        }else if(online==1 && hubOrSocket==0){
            //Socket 在线
            typeOnline = "01";
        }else if(online==0 && hubOrSocket==1){
            //HUB 离线
            typeOnline = "02";
        }else{
            //HUB 在线
            typeOnline = "03";
        }


        byte[] bytes = Util.hexToBytes(mac+typeOnline);
        logTv("广播信息："+Util.bytesToHex(bytes));

        return bytes;
    }

    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }
}