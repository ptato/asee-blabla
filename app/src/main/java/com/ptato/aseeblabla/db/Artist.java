package com.ptato.aseeblabla.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "artists")
public class Artist
{
    @PrimaryKey public int discogsId;
    public String name;
    public String profile;
    public String imgUrl;
}