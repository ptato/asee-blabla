package com.ptato.aseeblabla.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface ArtistDAO
{
    @Insert
    void insert(Artist ... artists);
    @Update
    void update(Artist ... artists);
    @Delete
    void delete(Artist ... artists);
    @Query("SELECT * FROM artists ORDER BY lastAccessedDate DESC")
    LiveData<List<Artist>> getAll();

    @Query("SELECT count(*) FROM artists")
    int getCount();
    @Query("DELETE FROM artists WHERE lastAccessedDate = (SELECT lastAccessedDate FROM artists ORDER BY lastAccessedDate ASC LIMIT 1)")
    void deleteOldest();

    @Query("SELECT * FROM artists WHERE discogsId = :id")
    LiveData<Artist> getArtistOfId(int id);

    @Query("UPDATE artists SET lastAccessedDate = :date WHERE discogsId = :id")
    void bumpArtistDate(int id, Date date);
}
