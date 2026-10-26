package com.zarda.simpleqrcodereader.libs;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

public interface ErrorCallback {
    @NonNull
    ErrorCallback SUPPRESS = new Utils.SuppressErrorCallback();
    @WorkerThread
    void onError(@NonNull Exception error);
}
