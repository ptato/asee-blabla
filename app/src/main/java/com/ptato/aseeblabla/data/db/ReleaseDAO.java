package com.ptato.aseeblabla.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ReleaseDAO
{
    @Insert
    void insertReleases(List<Release> releases);
    @Update
    void updateReleases(List<Release> releases);
    @Delete
    void deleteReleases(List<Release> releases);
    @Query("SELECT * FROM releases")
    LiveData<List<Release>> getAll();

    @Query("SELECT *FROM releases WHERE lower(title) LIKE '%'||lower(:query)||'%' OR lower(artist) LIKE '%'||lower(:query)||'%'")
    LiveData<List<Release>> search(String query);

    @Query("SELECT * FROM releases WHERE discogsId = :id")
    LiveData<Release> getReleaseOfId(int id);
    @Query("SELECT count(*) FROM releases WHERE discogsId = :id")
    int getReleaseCountOfId(int id);
    @Query("SELECT count(*) FROM releases WHERE discogsId = :id")
    LiveData<Integer> getCoolReleaseCountOfId(int id);
}
