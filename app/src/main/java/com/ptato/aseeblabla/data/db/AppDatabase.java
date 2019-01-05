package com.ptato.aseeblabla.data.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = { Release.class, Artist.class }, version=1, exportSchema=false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase
{
    private static AppDatabase instance = null;
    public static AppDatabase getInstance(Context context)
    {
        if (instance == null)
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "app.db").allowMainThreadQueries().build();
        return instance;
    }

    public abstract ReleaseDAO releaseDAO();
    public abstract ArtistDAO  artistDAO();
}
