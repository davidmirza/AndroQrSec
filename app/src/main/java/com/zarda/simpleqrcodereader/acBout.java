package com.zarda.simpleqrcodereader;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

// dulu com.google.firebase.BuildConfig -> yang tampil versi Firebase, bukan versi aplikasi

public class acBout extends AppCompatActivity {
    TextView tversi,tenging;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ac_bout);
        tversi = findViewById(R.id.tversi);
        tenging = findViewById(R.id.tengine);
        tenging.setText("Engine by: ZXING + qrsec");
        tversi.setText("Version: "+ BuildConfig.VERSION_NAME);
        getOnBackPressedDispatcher().addCallback(this, kembaliKeScanner);
    }
    public void kembali(View v){
       // onBackPressed();
        back();
    }
    private void back(){
        Intent intent = new Intent(acBout.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        acBout.this.startActivity(intent);
        acBout.this.finish();
    }

    // sejak targetSdk 36 (predictive back) override onBackPressed() tidak dipanggil -> pakai callback
    private final OnBackPressedCallback kembaliKeScanner = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            back();
        }
    };
}