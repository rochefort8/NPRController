package hoegaarden1917.com.nprcontrollerandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.le.*;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.BlurMaskFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import java.util.UUID;

public class BleCentral  implements IBleActivity {
    private final static LocationAccesser locationAccesser = new LocationAccesser();

    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;
    private boolean isBleEnabled = false;
    private BluetoothLeScanner bleScanner;

    private boolean mSwitchStatus = false ;

    private Random random = new Random();
    private Context mContext ;
    private Handler mHandler ;

    public void writeCharacteristic(BluetoothGatt gatt, byte[] bytes) {
        BluetoothGattService bleService = gatt.getService(UUID.fromString(mContext.getString(R.string.uuid_service)));
        if (bleService == null){
            Log.d("Error","bleService=null") ;
            return ;
        }
        BluetoothGattCharacteristic bleCharacteristic =
                bleService.getCharacteristic(UUID.fromString(mContext.getString(R.string.uuid_write_characteristic)));
        if (bleCharacteristic == null){
            Log.d("Error","bleCharacteristic=null") ;
            return ;
        }
        bleCharacteristic.setValue(bytes);
        gatt.writeCharacteristic(bleCharacteristic);
    }

    public void readCharacteristic(BluetoothGatt gatt) {

        BluetoothGattService bleService = gatt.getService(UUID.fromString(mContext.getString(R.string.uuid_service)));
        if (bleService == null){
            Log.d("Error","bleService=null") ;
            return ;
        }
        BluetoothGattCharacteristic bleCharacteristic =
                bleService.getCharacteristic(UUID.fromString(mContext.getString(R.string.uuid_read_characteristic)));
        if (bleCharacteristic == null){
            Log.d("Error","bleCharacteristic=null") ;
            return ;
        }
        gatt.readCharacteristic(bleCharacteristic) ;

        byte b[] = bleCharacteristic.getValue() ;
        if (b != null) {
            String s = new String(b);
            Log.d("d:", s);
        }

        mSwitchStatus = !mSwitchStatus ;
    }


    public void onGpsIsEnabled(){
        this.startScanByBleScanner();
    }

    public BleCentral(Context context,Handler handler) {
        mContext = context ;
        mHandler = handler ;

        isBleEnabled = false;

        bleManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();

        if ((bleAdapter == null)
                || (! bleAdapter.isEnabled())) {
        } else{
            this.scanNewDevice();
        }
    }
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("onConnectionState","Connected") ;
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("onConnectionState","Disonnected") ;
                Message msg = Message.obtain(mHandler, 0, gatt);
                mHandler.sendMessage(msg);
                isBleEnabled = false;
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            if (status == BluetoothGatt.GATT_SUCCESS) {

                List<BluetoothGattService> service = gatt.getServices();

                for (int i = 0; i < service.size();i++) {
                    BluetoothGattService s = service.get(i) ;
                    UUID uuid = s.getUuid() ;
                    Log.d("W",uuid.toString()) ;
                }

                BluetoothGattService bleService = gatt.getService(UUID.fromString(mContext.getString(R.string.uuid_service)));
                if (bleService != null){

                    BluetoothGattCharacteristic bleCharacteristic =
                            bleService.getCharacteristic(UUID.fromString(mContext.getString(R.string.uuid_write_characteristic)));
                    if (bleCharacteristic != null) {

                        BluetoothDevice device = gatt.getDevice() ;
                        String name = device.getName() ;
                        String address = device.getAddress() ;
                        Log.d("Service discovererd.",name + "/" + address);

                        Message msg = Message.obtain(mHandler, 1, gatt);
                        mHandler.sendMessage(msg);
                        isBleEnabled = true;
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){

        }
    };
    private void scanNewDevice() {
/*
        // OS ver.6.0以上ならGPSがOnになっているかを確認する(GPSがOffだとScanに失敗するため).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationAccesser.checkIsGpsOn(this, this);
        }
        // OS ver.5.0以上ならBluetoothLeScannerを使用する.
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.startScanByBleScanner();
        } else {
*/
            // OS ver.6.0以上ならGPSがOnになっているかを確認する(GPSがOffだとScanに失敗するため).
            this.startScanByBleScanner();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            locationAccesser.checkIsGpsOn(mContext, this);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.startScanByBleScanner();
            } else {

            }
        /*
        }
        */
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScanByBleScanner(){
        bleScanner = bleAdapter.getBluetoothLeScanner();

        bleScanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                BluetoothDevice device = result.getDevice() ;
                String name = device.getName() ;
                String address = device.getAddress() ;

//                Log.d("BLE device",name + "/" + address);

                // Trying to connect only to specific name
                if ((name != null) && name.equals("NaokyAndHiroky")) {
                    result.getDevice().connectGatt(mContext.getApplicationContext(), false, mGattCallback);
                }
            }

            @Override
            public void onScanFailed(int intErrorCode) {
                super.onScanFailed(intErrorCode);
            }
        });
    }

}