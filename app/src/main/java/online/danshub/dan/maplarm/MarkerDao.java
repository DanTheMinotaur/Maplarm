package online.danshub.dan.maplarm;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MarkerDao {
    @Query("SELECT * FROM marker")
    List<Marker> getAll();

    @Query("SELECT * FROM marker WHERE id = :markerId")
    Marker findById(int markerId);

    @Insert
    void insertMarker(Marker marker);

    @Delete
    void deleteMarker(Marker marker);
}
