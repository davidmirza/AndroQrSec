package com.zarda.simpleqrcodereader.libs;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.zarda.simpleqrcodereader.Result;

public interface DecodeCallback {

    @WorkerThread
    void onDecoded(@NonNull Result result);
}
