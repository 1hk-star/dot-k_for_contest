package com.example.my_sensor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class end extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end);

    }

    public void click_home(View view) {
        startActivity(new Intent (this, MainActivity.class));
        finish();}
}
