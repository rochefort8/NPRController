package hoegaarden1917.com.nprcontrollerandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_PERMISSIONS = 1;
    private boolean isPermissionAllowed;

    private BleCentral mBleCentral;
    private Handler mHandler;
    private Button button0;
    private static final int REQUEST_CODE = 0;
    private Context mContext;
    private NPRDevice mDevice = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("NPR Device Controller");

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            isPermissionAllowed = false ;
            this.requestBlePermission();
        }
        else{
            isPermissionAllowed = true;
        }

        mContext = this;
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    if (mDevice != null) {
                        return ;
                    }
                    mDevice = new NPRDevice(mBleCentral, (BluetoothGatt) msg.obj);
                    setContentView(R.layout.activity_nprdevice);
                    Button button_start_recognition = (Button)findViewById(R.id.button_start_recognition);
                    button_start_recognition.setOnClickListener((View v) -> {
                        Log.d("BDK-TS+pp","Recognition START!") ;
                        mDevice.startRecognition();

                    });
                    Button button_start_image_data_send = (Button)findViewById(R.id.button_start_image_data_send) ;
                    button_start_image_data_send.setOnClickListener((View v) -> {
                        Log.d("BDK-TS+pp","Image sending START!") ;
                        mDevice.startSendImage();
                    });

                } else {
                    mDevice = null ;
                    setContentView(R.layout.activity_main);
                }
            }
        };

        mBleCentral = new BleCentral(this, mHandler);

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestBlePermission(){
        // 権限が許可されていない場合はリクエスト.
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isPermissionAllowed = true;
        }
        else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 権限リクエストの結果を取得する.
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionAllowed = true;
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
