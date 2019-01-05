package com.ptato.aseeblabla.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;

import com.ptato.aseeblabla.data.db.AppDatabase;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.ArtistDAO;
import com.ptato.aseeblabla.data.db.Release;
import com.ptato.aseeblabla.data.db.ReleaseDAO;

import java.util.ArrayList;
import java.util.Date;
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
            synchronized (SINGLETON_LOCK)
            {
                instance = new Repository(context);
            }
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

    public LiveData<List<Artist>> getCachedArtists()
    {
        ArtistDAO dao = database.artistDAO();
        return dao.getAll();
    }

    public void bumpCachedArtist(int id)
    {
        ArtistDAO dao = database.artistDAO();
        Date date = new Date();
        dao.bumpArtistDate(id, date);
    }

    public LiveData<List<Release>> searchUserReleases(String query)
    {
        ReleaseDAO dao = database.releaseDAO();
        if (query == null)
            return dao.getAll();
        else
            return dao.search(query);
    }

    private void insertOrUpdateCachedArtist(Artist artist)
    {
        final ArtistDAO dao = database.artistDAO();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (dao.getCount() >= 5)
                    dao.deleteOldest();
            }
        }).start();

        dao.delete(artist);
        artist.lastAccessedDate = new Date();
        dao.insert(artist);
    }

    public LiveData<Artist> getArtist(final int id)
    {
        final ArtistDAO dao = database.artistDAO();
        final MediatorLiveData<Artist> liveData = new MediatorLiveData<>();
        final LiveData<Artist> cachedArtist = dao.getArtistOfId(id);

        final Observer<Artist> networkObserver = new Observer<Artist>()
        {
            @Override
            public void onChanged(@Nullable Artist artist)
            {
                if (artist != null && artist.discogsId == id)
                {
                    liveData.setValue(artist);
                    insertOrUpdateCachedArtist(artist);
                }
            }
        };

        liveData.addSource(cachedArtist, new Observer<Artist>()
        {
            @Override
            public void onChanged(@Nullable Artist artist)
            {
                if (artist == null || artist.discogsId == -1)
                {
                    liveData.addSource(DiscogsAPIUtils.getArtist(id), networkObserver);
                    liveData.removeSource(cachedArtist);
                } else
                {
                    liveData.setValue(artist);
                }
            }
        });

        return liveData;
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
