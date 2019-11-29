package com.example.my_sensor;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static android.speech.tts.TextToSpeech.ERROR;

public class beacon2 extends AppCompatActivity {
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

    //*** ble connect device information ***
    public static final String EXTRAS_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI = "BLE_DEVICE_RSSI";
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceRSSI;

    //*** 알림 설정 ***

    TextToSpeech tts;

    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon);

        Handler timer = new Handler();



        //*** ble connect device information ***
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceRSSI = intent.getIntExtra(EXTRAS_DEVICE_RSSI, 0) + " db";
        Log.d("beacon2() : ", "Get info about device ( " + mDeviceName + " )");

        //*** 비콘 설정 ***
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        beacon = new Vector<>();
        mScanSettings = new ScanSettings.Builder();
        ScanSettings scanSettings = mScanSettings.build();
        //filter와 setting 기능을 사용할때
        scanFilters = new Vector<>();
        ScanFilter.Builder scanFilter = new ScanFilter.Builder();
        scanFilter.setDeviceAddress("C2:01:EC:00:00:DA");
        ScanFilter scan = scanFilter.build();
        scanFilters.add(scan);
/*
        ScanFilter.Builder scanFilter2 = new ScanFilter.Builder();
        scanFilter.setDeviceAddress("C2:01:EC:00:00:DA");
        ScanFilter scan2 = scanFilter2.build();
        scanFilters.add(scan2);
*/
        mBluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);

//        mBluetoothLeScanner.startScan(mScanCallback);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        timer.postDelayed(new Runnable() {
            public void run(){
                Intent intent = new Intent(beacon2.this, setdest.class);
                intent.putExtra(beacon2.EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(beacon2.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                intent.putExtra(beacon2.EXTRAS_DEVICE_RSSI, mDeviceRSSI);
                startActivity(intent);
                finish();
            }
        }, 5000);


    }


    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            try {
                final ScanRecord scanRecord = result.getScanRecord();
                final ScanResult scanResult = result;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int isNewBeacon = -1;
                                for (int i = 0; i < beacon.size(); i++) {
                                    if (beacon.get(i).getAddress().equals(scanResult.getDevice().getAddress())) {
                                        isNewBeacon = i;
                                        break;
                                    }
                                }
                                if (isNewBeacon == -1) {
                                    //New beacon upload to List
                                    beacon.add(0, new Beacon(scanResult.getDevice().getAddress(), scanResult.getRssi(), simpleDateFormat.format(new Date()), scanResult.getDevice().getName(), scanRecord.getTxPowerLevel()));
                                    beaconAdapter = new BeaconAdapter(beacon, getLayoutInflater());
                                    //Log.d("getDeviceAddr : ", scanResult.getDevice().getAddress());
                                    if (scanResult.getDevice().getAddress().equals("C2:01:EC:00:00:DA")) // filtering이 안먹혀서 여기서 그냥 필터링했음
                                        createNotification(scanResult.getDevice().getName(), scanResult.getDevice().getAddress());
                                } else {
                                    //beacon data update to List
                                    beacon.get(isNewBeacon).setRssi(scanResult.getRssi());
                                    beacon.get(isNewBeacon).setNow(simpleDateFormat.format(new Date()));
                                    beacon.get(isNewBeacon).setTxPower(scanRecord.getTxPowerLevel());
                                }
                                beaconAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();
                ;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("onScanFailed()", errorCode + "");
        }

        private int findCodeInBuffer(byte[] buffer, byte code) {
            final int length = buffer.length;
            int i = 0;
            while (i < length - 2) {
                int len = buffer[i];
                if (len < 0) {
                    return -1;
                }
                if (i + len >= length) {
                    return -1;
                }
                byte tcode = buffer[i + 1];
                if (tcode == code) {
                    return i + 2;
                }
                i += len + 1;
            }
            return -1;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeScanner.stopScan(mScanCallback);

        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    private void createNotification(String name, String addr) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(name)
                .setContentText("송도 컨벤션에 입장하셨습니다. (mac addr: " + addr + ")")
                .setAutoCancel(true);

        if (flag == 0) {
            tts.speak("송도 컨벤션에 입장하셨습니다.", TextToSpeech.QUEUE_FLUSH, null);
            toast("건물입장완료");
            flag = 1;
        }



       /* Handler handler = new Handler() {

            public void handleMessage(Message msg) {

                super.handleMessage(msg);

                //startActivity(intent);

                startActivity(new Intent(beacon2.this, setdest.class));

                finish();

            }

        };
        handler.sendEmptyMessageDelayed(0, 2000);*/

        //알림표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "가본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }
        notificationManager.notify(1, builder.build());
    }






    public void text_click2(View view){
        final Intent intent = new Intent(this, setdest.class);
        intent.putExtra(beacon2.EXTRAS_DEVICE_NAME, mDeviceName);
        intent.putExtra(beacon2.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
        intent.putExtra(beacon2.EXTRAS_DEVICE_RSSI, mDeviceRSSI);
        startActivity(intent);
        finish();
    }
}
