package online.danshub.dan.maplarm;

import android.arch.persistence.room.RoomDatabase;

public abstract class MarkerDatabase extends RoomDatabase {
    public abstract MarkerDao markerDao();
}
