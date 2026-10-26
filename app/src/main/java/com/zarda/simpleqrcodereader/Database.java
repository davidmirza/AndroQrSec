package com.zarda.simpleqrcodereader;
import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {Histori.class}, version = 1,exportSchema = false)
public abstract class Database extends RoomDatabase{
    public abstract HistoDao histoDao();
    public static volatile Database INSTANCE;

    static Database getDatabase(Context context){
        if(INSTANCE==null){
            synchronized (Database.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context,Database.class,"tbhis.db").build();
                }
            }
        }
        return INSTANCE;
    }
}
