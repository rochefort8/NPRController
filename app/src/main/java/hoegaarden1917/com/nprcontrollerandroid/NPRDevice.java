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

    public String getAddress() {
        BluetoothDevice device = mGatt.getDevice() ;
        return device.getAddress() ;
    }
    public void startRecognition()              { _startRecognition(false) ; }
    public void startRecognitionWithImageSave() { _startRecognition(true) ; }

    private void _startRecognition(boolean willSaveImage) {
        byte[] bytes = new byte[1];
        if (willSaveImage == false) {
            bytes[0] = 0x00;
        } else {
            bytes[0] = 0x01;
        }
        mBleCentral.writeCharacteristic(mGatt, bytes);
        mSwitch = true;
    }
}
