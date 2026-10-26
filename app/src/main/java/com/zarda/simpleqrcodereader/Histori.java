package com.zarda.simpleqrcodereader;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbhis")
public class Histori {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "histo")
    private String histo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHisto() {
        return histo;
    }

    public void setHisto(String histo) {
        this.histo = histo;
    }
}
