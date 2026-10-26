package com.zarda.simpleqrcodereader;
import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import java.util.List;
class HistoRepository {
    private HistoDao histoDao;
    private Database database;
    private LiveData<List<Histori>> historiList;

    public HistoRepository(Application application) {
        database = Database.getDatabase(application);
        histoDao = database.histoDao();
        historiList = histoDao.getAllHistori();
    }
    public LiveData<List<Histori>> getAllHistori(){
        return histoDao.getAllHistori();
    }
    public void insertHisto(final Histori histori){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                database.histoDao().insertHis(histori);
                return null;
            }
        }.execute();
    }
}
