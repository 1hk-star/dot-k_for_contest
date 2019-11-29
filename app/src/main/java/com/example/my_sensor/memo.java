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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class memo extends AppCompatActivity {

        TextToSpeech tts;

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
                toast("음성 인식을 시작하기 위해 권한이 필요합니다.");
            }
            // tts = new TextToSpeech(this, this);

            final TextView txt = (TextView)findViewById(R.id.text1);
            txt.setText("\n");
            txt.setTextSize(18);
            txt.setTextColor(Color.BLACK);


            ImageView im1 = (ImageView) findViewById(R.id.im1);
            im1.setOnClickListener(new View.OnClickListener() {
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

        }


        public void text_click(View view){
            Intent intent =  new Intent(this, beacon2.class);
            startActivity(intent);
        }

        public void inputVoice(final TextView txt){
            try{
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                final SpeechRecognizer stt=SpeechRecognizer.createSpeechRecognizer(this);
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
                        toast("음성 입력을 종료합니다.");

                    }

                    @Override
                    public void onError(int error) {
                        toast("오류 발생 : "+error);
                        stt.destroy();

                    }

                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> result =(ArrayList<String>)results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                        txt.append("[사용자]"+result.get(0)+"\n");
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


            } catch(Exception e) {
                toast(e.toString());
            }
        }
        private void toast(String msg) {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }

        private void replyAnswer(String input, TextView txt){
            try{
                String cmd = input.split("")[0];
                String[] custom ={"안녕", "방법", "몰라"};
                String[] guide = {"안녕하세요", "버튼을 누르고 안내라고 말씀해주세요.", "다시한 번 말씀해주세요."};
                if (input.equals("종료")){
                    finish();
                }
                for(int n=0; n<custom.length; n++){
                    if(input.equals(custom[n])){
                        txt.append("[안내]"+guide[n]+"\n");
                        tts.speak(guide[n], TextToSpeech.QUEUE_FLUSH, null);
                        return;
                    }
                }

                if(input.equals("안내")){
                    txt.append("[음성안내] 길 안내 페이지로 이동합니다?\n");
                    tts.speak("길 안내 페이지로 이동합니다.", TextToSpeech.QUEUE_FLUSH, null);
                    //toast("happy");
                    Intent intent = new Intent(this, beacon2.class);
                    startActivity(intent);
                    return;

                } else if (input.equals("누구")) {
                    txt.append("[음성안내] 나는 음성도우미 입니다.\n");
                    tts.speak("나는 음성도우미 입니다.", TextToSpeech.QUEUE_FLUSH, null);


                }else if(input.equals("종료")){
                    finish();
                } else{
                    txt.append("[음성안내] 이해할 수 없습니다.\n");
                    tts.speak("이해할 수 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
                }
            }catch(Exception e){
                toast(e.toString());
            }
        }
    }

