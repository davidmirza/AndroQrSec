package com.zarda.simpleqrcodereader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;



public class splash extends AppCompatActivity {
    private int splashtimeout=500;
    private TextView tlogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        tlogo = findViewById(R.id.tqr);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent in = new Intent( splash.this, Check.class );
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);
                finish();
            }
        }, splashtimeout);
        Animation aopen = AnimationUtils.loadAnimation(this,R.anim.animasiku);
        tlogo.startAnimation(aopen);

        } else{
            Toast.makeText(this,"Sorry, Your Device is not Supported ",Toast.LENGTH_LONG).show();
            return;
        }
    }
}