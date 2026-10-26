package com.zarda.simpleqrcodereader;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;


public class Utils {

    //end

    Activity activity;
    DatePickerDialog datePickerDialog;
    SimpleDateFormat dateFormatter;
    public Utils(Activity myactivity){
        activity = myactivity;
    }

    public void pindahNoAnim(Class klas){
        Intent it = new Intent(activity,klas);
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
        activity.startActivity(it);
        activity.overridePendingTransition(0,0);
        activity.finish();
    }
    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
    public void PindahAct(Class klas){
        Intent it = new Intent(activity,klas);
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(it);
        activity.finish();
    }
    public void setBahasa(Class klas, String bahasa, String reload){
        Locale lokal = new Locale(bahasa);
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(lokal);
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
        if(reload=="Y")
        {PindahAct(klas);}
        else
        {pindahNoAnim(klas);}
    }

    public void ShowTanggal(final TextView txv){
        dateFormatter  = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                txv.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    public void toast_top(String pesan){
        Toast toast = Toast.makeText(this.activity, pesan, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 20, 100);
   //     toast.setGravity(Gravity.TOP | Gravity.RIGHT, 100, 200);
        toast.show();
    }
    public String Get_UUID(){
        return UUID.randomUUID().toString();

    }

    public String getId(){
      return  Settings.Secure.getString(this.activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }



}
