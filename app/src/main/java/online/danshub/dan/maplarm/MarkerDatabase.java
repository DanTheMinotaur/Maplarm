package online.danshub.dan.maplarm;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Marker.class}, version = 1)
public abstract class MarkerDatabase extends RoomDatabase {
    public abstract MarkerDao markerDao();
}
