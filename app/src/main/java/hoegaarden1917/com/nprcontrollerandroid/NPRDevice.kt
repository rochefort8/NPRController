package hoegaarden1917.com.nprcontrollerandroid

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.util.Log

/**
 * Created by 0000920402 on 2017/05/23.
 */

class NPRDevice(internal var mBleCentral: BleCentral, gatt: BluetoothGatt) : Any() {
    var gatt: BluetoothGatt
        internal set
    internal var name: String? = null
    internal var imagePath: String? = null
    internal var mSwitch: Boolean = false

    val address: String
        get() {
            val device = gatt.device
            return device.address
        }

    init {
        this.gatt = gatt

        val device = gatt.device
        val address = device.address

        Log.d("New NPRDevice found.", address)
        mSwitch = false
    }

    fun startRecognition() {
        _startRecognition(false)
    }

    fun startRecognitionWithImageSave() {
        _startRecognition(true)
    }

    private fun _startRecognition(willSaveImage: Boolean) {
        val bytes = ByteArray(1)
        if (willSaveImage == false) {
            bytes[0] = 0x00
        } else {
            bytes[0] = 0x01
        }
        mBleCentral.writeCharacteristic(gatt, bytes)
        mSwitch = true
    }
}
