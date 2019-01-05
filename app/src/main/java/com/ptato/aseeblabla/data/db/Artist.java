package com.ptato.aseeblabla.data.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Nullable;

import java.util.Date;

@Entity(tableName = "artists")
public class Artist
{
    @PrimaryKey public int discogsId;
    public String name;
    public String profile;
    public String imgUrl;
    public String url;
    public Date lastAccessedDate;

    @Ignore @Override
    public boolean equals(@Nullable Object obj)
    {
        return (obj == this) || (obj instanceof Artist && discogsId == ((Artist)obj).discogsId);
    }
}
