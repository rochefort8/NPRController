package hoegaarden1917.com.nprcontrollerandroid;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.util.Log;

/**
 * Created by 0000920402 on 2017/05/23.
 */

public class NPRDevice extends Object  {

    BleCentral mBleCentral ;
    BluetoothGatt mGatt ;
    String name ;
    String imagePath ;
    boolean mSwitch ;


    public NPRDevice (BleCentral bleCentral,BluetoothGatt gatt) {
        mGatt       = gatt ;
        mBleCentral = bleCentral ;

        BluetoothDevice device = gatt.getDevice() ;
        String address = device.getAddress() ;

        Log.d("New NPRDevice found.",address) ;
        mSwitch = false ;
    }

    public BluetoothGatt getGatt() { return mGatt; }

    public void startRecognition() {
        byte[] bytes = new byte[1] ;
        bytes[0] = 0x01 ;
        mBleCentral.writeCharacteristic(mGatt,bytes);
        mSwitch = true ;
    }

    public void startSendImage() {
        byte[] bytes = new byte[1] ;
        bytes[0] = 0x00 ;
        mBleCentral.writeCharacteristic(mGatt,bytes);
        mSwitch = false ;
    }
}
