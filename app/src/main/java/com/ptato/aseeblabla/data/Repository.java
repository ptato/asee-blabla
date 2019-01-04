package com.ptato.aseeblabla.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;

import com.ptato.aseeblabla.data.db.AppDatabase;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.Release;
import com.ptato.aseeblabla.data.db.ReleaseDAO;

import java.util.ArrayList;
import java.util.List;

public class Repository
{
    private AppDatabase database;
    private Repository(Context context)
    {
        database = AppDatabase.getInstance(context);
    }

    private static final Object SINGLETON_LOCK = new Object();
    private static Repository instance;
    public static Repository getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new Repository(context);
        }
        return instance;
    }



    public LiveData<Release> getRelease(int id)
    {
        ReleaseDAO dao = database.releaseDAO();
        if (dao.getReleaseCountOfId(id) > 0)
            return dao.getReleaseOfId(id);
        else
            return DiscogsAPIUtils.getRelease(id);
    }

    public void insertOrUpdateRelease(Release release)
    {
        ReleaseDAO dao = database.releaseDAO();
        List<Release> releases = new ArrayList<>();
        releases.add(release);

        if (dao.getReleaseCountOfId(release.discogsId) > 0)
            dao.updateReleases(releases);
        else
            dao.insertReleases(releases);
    }

    public void deleteRelease(Release release)
    {
        ReleaseDAO dao = database.releaseDAO();
        List<Release> deleteThis = new ArrayList<>();
        deleteThis.add(release);
        dao.deleteReleases(deleteThis);
    }

    public LiveData<Integer> getUserReleaseCountWithId(int id)
    {
        ReleaseDAO dao = database.releaseDAO();
        return dao.getCoolReleaseCountOfId(id);
    }

    public LiveData<List<Release>> getUserReleases()
    {
        ReleaseDAO dao = database.releaseDAO();
        return dao.getAll();
    }

    public LiveData<Artist> getArtist(int id)
    {
        return DiscogsAPIUtils.getArtist(id);
    }

    public LiveData<List<Release>> getArtistReleases(int id)
    {
        return DiscogsAPIUtils.getArtistReleases(id);
    }

    public LiveData<List<Release>> searchReleases(String query)
    {
        return DiscogsAPIUtils.searchReleases(query);
    }

    public LiveData<List<Artist>> searchArtists(String query)
    {
        return DiscogsAPIUtils.searchArtists(query);
    }
}
