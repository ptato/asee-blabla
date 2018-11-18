package com.ptato.aseeblabla.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

@Entity(tableName = "releases")
public class Release
{
    @PrimaryKey public int discogsId;
    public String title;
    public String artist;
    public int    artistId;
    public String year;
    public String thumbUrl;
    public String imageUrl;
    public int    communityStars;
    public int    stars;
    public String country;
    public String genres;
    public String review;

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
