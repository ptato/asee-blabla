package com.ptato.aseeblabla.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = { Release.class }, version=1)
public abstract class AppDatabase extends RoomDatabase
{
    private static AppDatabase instance = null;
    public static AppDatabase getInstance(Context context)
    {
        if (instance == null)
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "app.db").build();
        return instance;
    }

    public abstract ReleaseDAO releaseDAO();
}
