package online.danshub.dan.maplarm;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Class for storing Marker entity for created User Stored Marker Objects
 */
@Entity
public class Marker {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "marker_name")
    public String markerName;

    @ColumnInfo(name = "latitiude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;
}
