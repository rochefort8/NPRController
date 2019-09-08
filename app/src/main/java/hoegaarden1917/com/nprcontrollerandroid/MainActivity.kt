package hoegaarden1917.com.nprcontrollerandroid

import android.Manifest
import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private var isPermissionAllowed: Boolean = false

    private var mBleCentral: BleCentral? = null
    private var mHandler: Handler? = null
    private val button0: Button? = null
    private var mContext: Context? = null
    private var mDevice: NPRDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.title = "NPR Device Controller"

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isPermissionAllowed = false
            this.requestBlePermission()
        } else {
            isPermissionAllowed = true
        }

        mContext = this
        mHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == 1) {
                    if (mDevice != null) {
                        return
                    }
                    mDevice = NPRDevice(mBleCentral!!, msg.obj as BluetoothGatt)
                    setContentView(R.layout.activity_nprdevice)
                    supportActionBar!!.title = mDevice!!.address

                    val button_start_recognition = findViewById(R.id.button_start_recognition) as Button
                    button_start_recognition.setOnClickListener { v: View ->
                        Log.d("BDK-TS+pp", "Recognition START!")
                        mDevice!!.startRecognition()
                    }
                    val button_start_recognition_with_data_save = findViewById(R.id.button_start_recognition_with_data_save) as Button
                    button_start_recognition_with_data_save.setOnClickListener { v: View ->
                        Log.d("BDK-TS+pp", "Recognition START(with Image save)!")
                        mDevice!!.startRecognitionWithImageSave()
                    }

                } else {
                    mDevice = null
                    setContentView(R.layout.activity_main)
                    supportActionBar!!.title = "NPR Device Controller"
                }
            }
        }

        mBleCentral = BleCentral(this, mHandler!!)

    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestBlePermission() {
        // 権限が許可されていない場合はリクエスト.
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isPermissionAllowed = true
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // 権限リクエストの結果を取得する.
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionAllowed = true
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {

        private val REQUEST_PERMISSIONS = 1
        private val REQUEST_CODE = 0
    }
}
