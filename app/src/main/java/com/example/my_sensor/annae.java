package com.example.my_sensor;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static android.widget.Toast.LENGTH_SHORT;


public class annae extends AppCompatActivity implements SensorEventListener, BleWrapperUiCallbacks {

    private static final String TAG ="annae";
    TextToSpeech tts;
    int flag = 0;

    private Sensor stepDetectorSensor;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private float mAzimut, mPitch, mRoll;
    private TextView msg_textview;
    ImageView img;
    // ImageView img2;
    // ImageView img3;
    private long lastTime;
    private float lastmAzimut;
    private float lastmPitch;
    private float lastmRoll;

    float[] mGravity;
    float[] mGeomagnetic;
    find_path fp;

    //*** 비콘 설정 ***
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothLeScanner;
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private static final int PERMISSIONS = 100;
    Vector<Beacon> beacon;
    BeaconAdapter beaconAdapter;
    ScanSettings.Builder mScanSettings;
    List<ScanFilter> scanFilters;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
    String pre_mac = "1234";

    private BleWrapper mBleWrapper;
    public static final String EXTRAS_DEVICE_NAME    = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI    = "BLE_DEVICE_RSSI";
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceRSSI;
    int len;
    int pos;
    StringBuilder stringBuilder;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.annae);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceRSSI = intent.getIntExtra(EXTRAS_DEVICE_RSSI, 0) + " db";
        Log.d("annae() : ", "Get info about device ( "+mDeviceName+" )");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        len = 0;
        pos = 0;
        stringBuilder = new StringBuilder();

        msg_textview = (TextView) findViewById(R.id.msg);

        fp = new find_path(this);
        fp.set_current_position(10, 10);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,stepDetectorSensor, SensorManager.SENSOR_DELAY_UI);

        Log.d("onResume() : ", "start onResume");
        if(mBleWrapper == null) mBleWrapper = new BleWrapper(this, this);
        if(!mBleWrapper.initialize()) {
            finish();
        }

        // start automatically connecting to the device
        mBleWrapper.connect(mDeviceAddress);
        //byte[] message = {0x20, 0x51, 0x01, 0x31};
        //mBleWrapper.sendByte(message);

    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {
        ImageView img = (ImageView) findViewById(R.id.img);

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);

            if (success&&gabOfTime > 2000) {
                lastTime = currentTime;
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                mAzimut = (float) Math.toDegrees(orientation[0]);
                Log.d(TAG,"2초마다 인식");
                if (mAzimut < 0) {
                    float tmp = 360 + mAzimut;
                    mAzimut = tmp;
                }
                mPitch = (float) Math.toDegrees(orientation[1]);
                mRoll = (float) Math.toDegrees(orientation[2]);
                int res;
                res = fp.find_direction(mAzimut);

                if (res == -1) {
                    tts.speak("목적지에 도착하였습니다.", TextToSpeech.QUEUE_FLUSH, null);
                    byte[] text = {0x20, 0x51, 0x01, 0x30};
                    mBleWrapper.sendByte(text);
                    //bleSendData.sendMessage("목적지에 도착하였습니다."); // sendData to BLE Device
                    Intent intent = new Intent(this, end.class);
                    startActivity(intent);
                } else if (res == 1) {
                    img.setImageResource(com.example.my_sensor.R.drawable.location);
                    //앞르로 가는 이미지 변경
                    msg_textview.setText("앞으로 이동하세요.");
                    byte[] text = {0x20, 0x51, 0x01, 0x46};
                    mBleWrapper.sendByte(text);
                    if (flag != res) {
                        tts.speak("앞으로 이동하세요.", TextToSpeech.QUEUE_FLUSH, null);
                        //bleSendData.sendMessage("앞으로 이동하세요."); // sendData to BLE Device
                        flag=res;
                    }

                } else if (res == 2) {
                    img.setImageResource(com.example.my_sensor.R.drawable.left);
                    //왼쪽으로 가는 이미지 변경
                    msg_textview.setText("방향을 왼쪽으로 서서히 움직이세요.");
                    byte[] text = {0x20, 0x51, 0x01, 0x4C};
                    mBleWrapper.sendByte(text);
                    if (flag != res) {
                        tts.speak("방향을 왼쪽으로 서서히 움직이세요.", TextToSpeech.QUEUE_FLUSH, null);
                        //bleSendData.sendMessage("방향을 왼쪽으로 서서히 움직이세요."); // sendData to BLE Device
                        flag=res;

                    }
                } else if (res == 3) {
                    img.setImageResource(com.example.my_sensor.R.drawable.right);
                    //오른쪽으로 가는 이미지 변경.
                    msg_textview.setText("방향을 오른쪽으로 서서히 움직이세요.");
                    byte[] text = {0x20, 0x51, 0x01, 0x52};
                    mBleWrapper.sendByte(text);
                    if (flag != res) {
                        tts.speak("방향을 오른쪽으로 서서히 움직이세요.", TextToSpeech.QUEUE_FLUSH, null);
                        //bleSendData.sendMessage("방향을 오른쪽으로 서서히 움직이세요."); // sendData to BLE Device
                        flag=res;
                    }
                }
               /* String result;
                result = "Azimut:"+mAzimut+"\n"+"Pitch:"+mPitch+"\n"+"Roll:"+mRoll+"\n"+msg+"\n";
                mResultView.setText(result);*/
            }
        }


        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.KOREAN);
            }
        });

        lastmAzimut=mAzimut;
        lastmPitch=mPitch;
        lastmRoll=mRoll;

    }
    public void show(){
        Toast.makeText(this,"다음 장소로 이동했습니다.",LENGTH_SHORT);
    }
    public void onclick(View view) {
        int res = fp.move_front();
        if (res == -1){
            Intent intent = new Intent(this, end.class);
            startActivity(intent);
            flag = 1;
            Toast.makeText(this,"다음 장소로 이동했습니다.",LENGTH_SHORT);
        }
        if(res == -2){

        }
        else{
            //  Log.e("hihi",fp.getCurrent_y()+", "+fp.getCurrent_x());
        }
    }

    // uiDevice 관련 함수들
    public void uiDeviceConnected(final BluetoothGatt gatt,
                                  final BluetoothDevice device)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public void uiDeviceDisconnected(final BluetoothGatt gatt,
                                     final BluetoothDevice device)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public void uiNewRssiAvailable(final BluetoothGatt gatt,
                                   final BluetoothDevice device,
                                   final int rssi)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public void uiAvailableServices(final BluetoothGatt gatt,
                                    final BluetoothDevice device,
                                    final List<BluetoothGattService> services)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public void uiCharacteristicForService(final BluetoothGatt gatt,
                                           final BluetoothDevice device,
                                           final BluetoothGattService service,
                                           final List<BluetoothGattCharacteristic> chars)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public void uiNewValueForCharacteristic(final BluetoothGatt gatt,
                                            final BluetoothDevice device,
                                            final BluetoothGattService service,
                                            final BluetoothGattCharacteristic characteristic,
                                            final String strValue,
                                            final int intValue,
                                            final byte[] rawValue,
                                            final String timestamp)
    {
    }

    public void uiSuccessfulWrite(final BluetoothGatt gatt,
                                  final BluetoothDevice device,
                                  final BluetoothGattService service,
                                  final BluetoothGattCharacteristic ch,
                                  final String description)
    {
        if (len != 0) {
            sendChunk();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(), "Writing to " + description + " was finished successfully!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void uiFailedWrite(final BluetoothGatt gatt,
                              final BluetoothDevice device,
                              final BluetoothGattService service,
                              final BluetoothGattCharacteristic ch,
                              final String description)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(), "Writing to " + description + " FAILED!", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void uiGotNotification(final BluetoothGatt gatt,
                                  final BluetoothDevice device,
                                  final BluetoothGattService service,
                                  final BluetoothGattCharacteristic ch)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // at this moment we only need to send this "signal" do characteristic's details view
            }
        });
    }

    @Override
    public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {
        // no need to handle that in this Activity (here, we are not scanning)
    }
    public void sendChunk() {
        if (len != 0) {
            stringBuilder.setLength(0);
            if (len>=20) {
                stringBuilder.append(message.toCharArray(), pos, 20 );
                len-=20;
                pos+=20;
            }
            else {
                stringBuilder.append(message.toCharArray(), pos, len);
                len = 0;
            }
            mBleWrapper.send(stringBuilder.toString());
        }
    }

}

