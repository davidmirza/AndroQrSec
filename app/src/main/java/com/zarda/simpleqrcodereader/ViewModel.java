package com.zarda.simpleqrcodereader;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
public class ViewModel extends AndroidViewModel{
    HistoRepository histoRepository;
    LiveData<List<Histori>> histoList;
    private Histori histori;


    public ViewModel(@NonNull Application application) {
        super(application);
        histoRepository = new HistoRepository(application);
        histoList = histoRepository.getAllHistori();
    }
    public LiveData<List<Histori>> getAllHistori(){
        return histoRepository.getAllHistori();
    }

    public  void insertHistori(Histori histori){
            histoRepository.insertHisto(histori);
    }

}
