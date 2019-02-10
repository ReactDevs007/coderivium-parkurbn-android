package com.cruxlab.parkurbn.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.cruxlab.parkurbn.model.History;

/**
 * Created by alla on 6/22/17.
 */

@Database(entities = {History.class}, version = 2)
public abstract class ParkUrbnDatabase extends RoomDatabase {

    private static final String DB_NAME = "park_urbn_db";

    private static ParkUrbnDatabase INSTANCE;

    public abstract HistoryDao historyDao();

    public static ParkUrbnDatabase getInMemoryDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), ParkUrbnDatabase.class, DB_NAME)
                            .build();
        }
        return INSTANCE;
    }
}
