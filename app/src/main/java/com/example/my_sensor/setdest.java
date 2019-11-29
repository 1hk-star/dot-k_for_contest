package com.example.my_sensor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class setdest extends AppCompatActivity {
    //*** ble connect device information ***
    public static final String EXTRAS_DEVICE_NAME    = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI    = "BLE_DEVICE_RSSI";
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceRSSI;
    TextToSpeech tts;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setdest);

        //*** ble connect device information ***
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceRSSI = intent.getIntExtra(EXTRAS_DEVICE_RSSI, 0) + " db";
        Log.d("setdest() : ", "Get info about device ( "+mDeviceName+" )");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
            toast("음성 인식을 시작하기 위해 권한이 필요합니다.");
        }
        final TextView txt = (TextView) findViewById(R.id.text2);
        txt.setTextSize(18);
        txt.setTextColor(Color.BLACK);
        txt.setText("\n");


        //음성입력
        Button bt2 = (Button) findViewById(R.id.bt2);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputVoice(txt);
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.KOREAN);
            }
        });


        Button input2 = (Button) findViewById(R.id.bt1);
        input2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
                toast("목적지를 말씀해주세요!");
                tts.speak("목적지 설정버튼을 누르고 목적지를 말씀하세요.", TextToSpeech.QUEUE_FLUSH, null);
            }

        });

        Button bt = (Button)findViewById(R.id.info);
        bt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                tts.speak("목적지 설정버튼을 누르고 목적지를 말씀하세요.", TextToSpeech.QUEUE_FLUSH, null);
            }
        });


    }


    public void inputVoice(final TextView txt) {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            final SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(this);
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                    toast("음성 입력을 시작합니다!");
                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] bytes) {

                }

                @Override
                public void onEndOfSpeech() {
                    //toast("음성 입력을 종료합니다.");

                }

                @Override
                public void onError(int error) {
                    toast("오류 발생 : " + error);
                    stt.destroy();

                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    txt.append("[사용자]" + result.get(0) + "\n");
                    replyAnswer(result.get(0), txt);
                    stt.destroy();

                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
            stt.startListening(intent);


        } catch (Exception e) {
            toast(e.toString());
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void replyAnswer(String input, TextView txt) {
        try {
            if (input.equals("화장실")) {
                txt.append("[음성안내] 가까운 화장실로 안내를 시작합니다.\n");
                tts.speak("가까운 화장실로 안내를 시작합니다.", TextToSpeech.QUEUE_FLUSH, null);
                //toast("happy");
                Intent intent = new Intent(this, annae.class);
                intent.putExtra(setdest.EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(setdest.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                intent.putExtra(setdest.EXTRAS_DEVICE_RSSI, mDeviceRSSI);
                startActivity(intent);
                finish();
                return;

            } else if (input.equals("너는 누구니")) {
                txt.append("[음성안내] 나는 음성도우미 입니다.\n");
                tts.speak("나는 음성도우미 입니다.", TextToSpeech.QUEUE_FLUSH, null);


            } else if (input.equals("종료")) {
                finish();
            } else {
                tts.speak("이해할 수 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
            }
        } catch (Exception e) {
            toast(e.toString());
        }
    }


    public void view_click(View view) {
        startActivity(new Intent(this, annae.class));
        finish();
    }
}
