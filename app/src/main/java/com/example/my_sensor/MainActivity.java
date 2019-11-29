package com.example.my_sensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {
    public static final String EXTRAS_DEVICE_NAME    = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI    = "BLE_DEVICE_RSSI";
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceRSSI;
    TextToSpeech tts;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceRSSI = intent.getIntExtra(EXTRAS_DEVICE_RSSI, 0) + " db";
        Log.d("MainActivity() : ", "Get info about device ( "+mDeviceName+" )");

        ImageView imf = (ImageView) findViewById(R.id.imf);
        imf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak("길 안내를 시작하려면 화면을 클릭하세요", TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.KOREAN);
            }
        });

    }
    public void view_click(View view){
        final Intent intent = new Intent(this, beacon2.class);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, mDeviceName);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_RSSI, mDeviceRSSI);
        startActivity(intent);
        finish();
    }

    public void text_click(View view){
        final Intent intent = new Intent(this, beacon2.class);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, mDeviceName);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_RSSI, mDeviceRSSI);
        startActivity(intent);
        finish();
    }




    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

}