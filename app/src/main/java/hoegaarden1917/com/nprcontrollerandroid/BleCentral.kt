package hoegaarden1917.com.nprcontrollerandroid

import android.annotation.TargetApi
import android.app.Activity
import android.bluetooth.le.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.graphics.BlurMaskFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.Random
import java.util.Timer
import java.util.TimerTask

import java.util.UUID

class BleCentral(private val mContext: Context, private val mHandler: Handler) : IBleActivity {

    private val bleManager: BluetoothManager
    private val bleAdapter: BluetoothAdapter?
    private var isBleEnabled = false
    private var bleScanner: BluetoothLeScanner? = null

    private var mSwitchStatus = false

    private val random = Random()
    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("onConnectionState", "Connected")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("onConnectionState", "Disonnected")
                val msg = Message.obtain(mHandler, 0, gatt)
                mHandler.sendMessage(msg)
                isBleEnabled = false
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val bleService = gatt.getService(UUID.fromString(mContext.getString(R.string.uuid_service)))

                if (bleService != null) {
                    val bleCharacteristic = bleService.getCharacteristic(UUID.fromString(mContext.getString(R.string.uuid_write_characteristic)))
                    if (bleCharacteristic != null) {

                        val device = gatt.device
                        val name = device.name
                        val address = device.address
                        Log.d("Service discovererd.", "$name/$address")

                        val msg = Message.obtain(mHandler, 1, gatt)
                        mHandler.sendMessage(msg)
                        isBleEnabled = true
                    }
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {

        }
    }

    fun writeCharacteristic(gatt: BluetoothGatt, bytes: ByteArray) {
        val bleService = gatt.getService(UUID.fromString(mContext.getString(R.string.uuid_service)))
        if (bleService == null) {
            Log.d("Error", "bleService=null")
            return
        }
        val bleCharacteristic = bleService.getCharacteristic(UUID.fromString(mContext.getString(R.string.uuid_write_characteristic)))
        if (bleCharacteristic == null) {
            Log.d("Error", "bleCharacteristic=null")
            return
        }
        bleCharacteristic.value = bytes
        gatt.writeCharacteristic(bleCharacteristic)
    }

    fun readCharacteristic(gatt: BluetoothGatt) {

        val bleService = gatt.getService(UUID.fromString(mContext.getString(R.string.uuid_service)))
        if (bleService == null) {
            Log.d("Error", "bleService=null")
            return
        }
        val bleCharacteristic = bleService.getCharacteristic(UUID.fromString(mContext.getString(R.string.uuid_read_characteristic)))
        if (bleCharacteristic == null) {
            Log.d("Error", "bleCharacteristic=null")
            return
        }
        gatt.readCharacteristic(bleCharacteristic)

        val b = bleCharacteristic.value
        if (b != null) {
            val s = String(b)
            Log.d("d:", s)
        }

        mSwitchStatus = !mSwitchStatus
    }


    override fun onGpsIsEnabled() {
        this.startScanByBleScanner()
    }

    init {

        isBleEnabled = false

        bleManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bleManager.adapter

        if (bleAdapter == null || !bleAdapter.isEnabled) {
        } else {
            this.scanNewDevice()
        }
    }

    private fun scanNewDevice() {
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
        this.startScanByBleScanner()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //            locationAccesser.checkIsGpsOn(mContext, this);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.startScanByBleScanner()
        } else {

        }
        /*
        }
        */
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startScanByBleScanner() {
        bleScanner = bleAdapter!!.bluetoothLeScanner

        bleScanner!!.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)

                val device = result.device
                val name = device.name
                val address = device.address

                //               Log.d("BLE device",name + "/" + address);

                // Trying to connect only to specific name
                //                if ((name != null) && name.equals("NaokyAndHiroky")) {
                if (name != null && name == mContext.getString(R.string.device_name)) {
                    Log.d("BLE device", "$name/$address")
                    result.device.connectGatt(mContext.applicationContext, false, mGattCallback)
                }
            }

            override fun onScanFailed(intErrorCode: Int) {
                super.onScanFailed(intErrorCode)
            }
        })
    }

    companion object {
        private val locationAccesser = LocationAccesser()
    }

}