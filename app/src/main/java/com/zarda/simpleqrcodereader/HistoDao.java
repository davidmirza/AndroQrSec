package com.zarda.simpleqrcodereader;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface HistoDao {
    @Insert
    void insertHis(Histori histori);
    @Query("select * from tbhis")
    LiveData<List<Histori>> getAllHistori();
}
