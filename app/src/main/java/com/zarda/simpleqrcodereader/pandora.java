package com.zarda.simpleqrcodereader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Minta izin kamera. Dulu memakai Dexter (com.karumi:dexter) yang sudah tidak dirawat dan hanya ada
 * di jcenter (sudah ditutup) - kini API bawaan AndroidX. Setelah izin diberikan, activity kembali ke
 * onResume dan scanner otomatis dimulai ulang.
 */
class pandora {
    static final int REQUEST_CAMERA = 1001;
    Activity activity;
    pandora(Activity aktifitas){
        activity = aktifitas;
    }

    void beri_akses() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }


}
