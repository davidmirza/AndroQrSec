package com.zarda.simpleqrcodereader

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class Check : AppCompatActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private var utils: Utils? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)
        utils = Utils(this)
        //        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
//            utils.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
//        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        appUpdateManager = AppUpdateManagerFactory.create(this)
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
               // cek()
                langsung()
            }
        },1000)

    }
    fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }
    private  fun langsung(){
        val itn = Intent(this, MainActivity::class.java)
        itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        itn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(itn)
        this.finish()
    }
    private fun cek(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener {
                if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                            it,
                            AppUpdateType.FLEXIBLE,
                            this,
                            999
                    )
                } else {

                    val itn = Intent(this, MainActivity::class.java)
                    itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    itn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(itn)
                    this.finish()
                    // Toast.makeText(this, "new", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            Toast.makeText(this,"Sorry, Your Android Version is not Supported",Toast.LENGTH_LONG).show()
            return
        }
    }
    override fun onResume() {
        appUpdateManager.appUpdateInfo
                .addOnSuccessListener {
                    if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        appUpdateManager.startUpdateFlowForResult(
                                it,
                                AppUpdateType.IMMEDIATE,
                                this,
                                999
                        )
                    }
                }
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 999 && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "sukses", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
        }
    }
}