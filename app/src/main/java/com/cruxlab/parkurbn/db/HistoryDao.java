package com.cruxlab.parkurbn.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.cruxlab.parkurbn.model.History;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

/**
 * Created by alla on 6/22/17.
 */

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM history")
    List<History> getAll();

    @Insert(onConflict = IGNORE)
    void insertAll(List<History> histories);

    @Query("DELETE FROM history")
    void deleteAll();
}
