package com.zarda.simpleqrcodereader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class acHasil extends AppCompatActivity {
    TextView t_result;
 //   private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ac_hasil);
        t_result=findViewById(R.id.trsult);
        Bundle bndl =getIntent().getExtras();
        assert bndl != null;
        t_result.setText(bndl.getString("hasil"));
        tampilkanStatus(bndl);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        AdLoader.Builder builder = new AdLoader.Builder(this,getString(R.string.banner_native));
        builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                TemplateView templateView  = findViewById(R.id.my_template);
                templateView.setNativeAd(nativeAd);
            }
        });
        AdLoader adLoader = builder.build();
        AdRequest adRequest = new AdRequest.Builder().build();
        adLoader.loadAd(adRequest);
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
    }
//    @Override
//    protected void onPause() {
//        if (mAdView != null) {
//            mAdView.pause();
//        }
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (mAdView != null) {
//            mAdView.destroy();
//        }
//        super.onDestroy();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mAdView != null) {
//            mAdView.resume();
//        }
//    }
    public void Kembali(View v){
        onBackPressed();
    }

    /**
     * Indikator signing key + secret dari QrSec (extra dari MainActivity).
     * Secret selalu ditampilkan; warna kotak menunjukkan apakah signing key ditemukan dan sah.
     */
    private void tampilkanStatus(Bundle bndl) {
        String status = bndl.getString("status");
        if (status == null || QrSec.Status.BIASA.name().equals(status)) {
            return; // QR biasa / dibuka dari history: tampilan lama
        }
        TextView tStatus = findViewById(R.id.tstatus);
        TextView tRahasia = findViewById(R.id.trahasia);
        int keyId = bndl.getInt("keyId", -1);
        String indikator;
        int warna;
        switch (QrSec.Status.valueOf(status)) {
            case SAH:
                indikator = "🔑 Signing key: DITEMUKAN (keyId " + keyId + ")\n✔ ASLI - tanda tangan sah";
                warna = Color.parseColor("#2E7D32");
                break;
            case TANPA_TANDA:
                indikator = "🔓 Signing key: TIDAK AKTIF\nSecret terbaca, keaslian tidak dijamin";
                warna = Color.parseColor("#546E7A");
                break;
            default: // PALSU
                indikator = "🔑 Signing key: DITEMUKAN (keyId " + keyId + ")\n✖ TIDAK SAH - "
                        + bndl.getString("alasan");
                warna = Color.parseColor("#C62828");
                break;
        }
        tStatus.setText(indikator);
        tStatus.setBackgroundColor(warna);
        tStatus.setVisibility(View.VISIBLE);
        String rahasia = bndl.getString("rahasia");
        if (rahasia != null) {
            tRahasia.setText(QrSec.Status.PALSU.name().equals(status)
                    ? "Secret (TIDAK terjamin): " + rahasia
                    : "Secret: " + rahasia);
            tRahasia.setVisibility(View.VISIBLE);
        }
    }
}