package com.ptato.aseeblabla.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Nullable;

@Entity(tableName = "releases")
public class Release
{
    @PrimaryKey public int discogsId;
    public String title;
    public String artist;
    public String year;
    public String thumbUrl;
    public int stars;

    @Ignore
    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (obj == this) return true;
        if (obj instanceof Release)
        {
            return discogsId == ((Release)obj).discogsId;
        } else
        {
            return false;
        }
    }
}
