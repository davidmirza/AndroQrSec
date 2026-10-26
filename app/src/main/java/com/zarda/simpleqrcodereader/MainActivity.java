package com.zarda.simpleqrcodereader;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zarda.simpleqrcodereader.libs.CodeScanner;
import com.zarda.simpleqrcodereader.libs.CodeScannerView;
import com.zarda.simpleqrcodereader.libs.DecodeCallback;
import com.zarda.simpleqrcodereader.libs.ErrorCallback;

/**
 * Layar scan utama. Memakai reader qrsec (libs/CodeScanner + ZXing tertanam) menggantikan
 * me.dm7 ZXingScannerView: dm7 meregangkan preview kamera ke ukuran layar (gambar tertarik),
 * CodeScanner memotong preview di tengah dengan rasio asli kamera.
 */
public class MainActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;
    CodeScanner mCodeScanner;
    FloatingActionButton flon;
    Boolean isOn;
    ImageButton ib_his;
    ImageButton ib_about;
    ViewModel historiViewModel;
    HistoriAdapter historiAdapter;
    Animation btAnim;
    pandora pnd = new pandora(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CodeScannerView scannerView = findViewById(R.id.frame);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleResult(result);
                    }
                });
            }
        });
        mCodeScanner.setErrorCallback(new ErrorCallback() {
            @Override
            public void onError(@NonNull final Exception error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Kamera tidak bisa dibuka: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        flon = findViewById(R.id.fab_flash);
        isOn = false;
        pnd.beri_akses();
        btAnim = AnimationUtils.loadAnimation(this, R.anim.animasiku);
        ib_about = findViewById(R.id.ib_about);
        ib_his = findViewById(R.id.ib_his);
        ib_his.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_his.startAnimation(btAnim);
                Intent itn = new Intent(MainActivity.this, acthasil.class);
                startActivity(itn);
            }
        });

        ib_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_about.startAnimation(btAnim);
                Intent intent = new Intent(MainActivity.this, acBout.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        });
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        historiViewModel = new ViewModelProvider(this).get(ViewModel.class);
        historiAdapter = new HistoriAdapter();
        getOnBackPressedDispatcher().addCallback(this, backDuaKali);
    }
    public void flashON(View v) {

        if(!isOn){
            isOn=true;
            mCodeScanner.setFlashEnabled(true);
            flon.setImageResource(R.drawable.ic_redfof);
        }
        else{
            isOn=false;
            mCodeScanner.setFlashEnabled(false);
            flon.setImageResource(R.drawable.ic_redfon);
        }
        flon.startAnimation(btAnim);
    }

    private void handleResult(Result rawResult) {
        QrSec.Hasil hasil = QrSec.periksa(rawResult.getText(), rawResult.getRawBytes());
        if(isOn){
            isOn=false;
            mCodeScanner.setFlashEnabled(false);
            flon.setImageResource(R.drawable.ic_redfon);
        }

        try{
            Histori histori = new Histori();
            histori.setHisto(hasil.status == QrSec.Status.BIASA ? hasil.main
                    : hasil.main + " [" + hasil.rahasia + "]");
            historiViewModel.insertHistori(histori);
        }
        catch (Exception ex){
            Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
            return;
        }
        // ===== [SERVER-1] NONAKTIF: kirim hasil scan ke server (counter + history lokasi) =====
        // Buka komentar bila server sudah ada. Langkah lengkap: CATATAN_SERVER.md
        // if (hasil.status != QrSec.Status.BIASA) {
        //     kirimKeServer(hasil);
        // }
        Intent itn = new Intent(MainActivity.this, acHasil.class);
        itn.putExtra("hasil", hasil.main);
        itn.putExtra("status", hasil.status.name());
        itn.putExtra("rahasia", hasil.rahasia);
        itn.putExtra("alasan", hasil.alasan);
        itn.putExtra("keyId", hasil.keyId);
        startActivity(itn);
    }

    // ===== [SERVER-2] NONAKTIF: kirim di thread latar, tampilkan balasan server =====
    // Butuh import: android.annotation.SuppressLint, android.location.Location,
    //   android.location.LocationManager, android.provider.Settings,
    //   java.util.concurrent.ExecutorService, java.util.concurrent.Executors
    //
    // private final ExecutorService jaringan = Executors.newSingleThreadExecutor();
    //
    // private void kirimKeServer(final QrSec.Hasil hasil) {
    //     final Location lokasi = lokasiTerakhir();
    //     @SuppressLint("HardwareIds")
    //     final String perangkat = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    //     jaringan.execute(new Runnable() {
    //         @Override
    //         public void run() {
    //             final ServerQr.Balasan b = ServerQr.kirim(ServerQr.URL_SCAN, hasil,
    //                     lokasi == null ? null : lokasi.getLatitude(),
    //                     lokasi == null ? null : lokasi.getLongitude(),
    //                     lokasi == null ? null : lokasi.getAccuracy(),
    //                     perangkat);
    //             runOnUiThread(new Runnable() {
    //                 @Override
    //                 public void run() {
    //                     String teks = b.berhasil
    //                             ? "Server: " + b.hasil + " (scan ke-" + b.jumlahScan + ")\n" + (b.pesan == null ? "" : b.pesan)
    //                             : "Server: " + b.pesan;
    //                     Toast.makeText(getApplicationContext(), teks, Toast.LENGTH_LONG).show();
    //                 }
    //             });
    //         }
    //     });
    // }
    //
    // ===== [SERVER-3] NONAKTIF: lokasi terakhir (butuh izin lokasi di AndroidManifest.xml) =====
    // @SuppressLint("MissingPermission")
    // private Location lokasiTerakhir() {
    //     if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    //             != android.content.pm.PackageManager.PERMISSION_GRANTED) {
    //         requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
    //                 android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1002);
    //         return null; // scan pertama tanpa lokasi; scan berikutnya sudah ada
    //     }
    //     LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
    //     Location terbaik = null;
    //     for (String p : lm.getProviders(true)) {
    //         Location l = lm.getLastKnownLocation(p);
    //         if (l != null && (terbaik == null || l.getAccuracy() < terbaik.getAccuracy())) {
    //             terbaik = l;
    //         }
    //     }
    //     return terbaik;
    // }

    @Override
    protected void onResume() {
        super.onResume();
        // setelah dialog izin ditutup activity kembali ke onResume -> preview dimulai di sini
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        isOn = false;
        flon.setImageResource(R.drawable.ic_redfon);
        super.onPause();
    }

    /**
     * Tekan BACK 2x untuk keluar. Memakai OnBackPressedCallback: sejak targetSdk 36 (predictive back)
     * override onBackPressed() tidak dipanggil lagi oleh sistem.
     */
    private final OnBackPressedCallback backDuaKali = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                finish();
                return;
            }

            doubleBackToExitPressedOnce = true;
            Toast.makeText(MainActivity.this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    };
}
