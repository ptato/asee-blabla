package com.ptato.aseeblabla.data;

import android.arch.lifecycle.LiveData;

import com.ptato.aseeblabla.db.Artist;

public class Repository
{
    private static final Object SINGLETON_LOCK = new Object();
    private static Repository instance;
    public static Repository getInstance()
    {
        if (instance == null)
        {
            instance = new Repository();
        }
        return instance;
    }


    public LiveData<Artist> getArtist(int id)
    {
        return DiscogsAPIUtils.getArtist(id);
    }
}
