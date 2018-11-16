package com.ptato.aseeblabla.db;

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
    public void updateReleases(List<Release> releases);
    @Delete
    void deleteReleases(List<Release> releases);
    @Query("SELECT * FROM releases")
    List<Release> getAll();
}
