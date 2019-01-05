package com.ptato.aseeblabla.ui.detail.artist;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.Release;

import java.util.List;

public class ArtistDetailViewModel extends ViewModel
{
    private final Repository repository;

    private LiveData<Artist> artistData;
    private LiveData<List<Release>> artistReleases;
    public MutableLiveData<Boolean> isShowingArtistReleases = new MutableLiveData<>();

    public ArtistDetailViewModel(Repository _repository, int id)
    {
        repository = _repository;
        artistData = repository.getArtist(id);
        artistReleases = null;
        isShowingArtistReleases.setValue(false);
    }

    public LiveData<Artist> getArtist()
    {
        return artistData;
    }
    private void initArtistReleases()
    {
        artistReleases =
                Transformations.switchMap(artistData, new Function<Artist, LiveData<List<Release>>>()
                {
                    @Override
                    public LiveData<List<Release>> apply(Artist input)
                    {
                        return repository.getArtistReleases(input.discogsId);
                    }
                });
    }
    public LiveData<List<Release>> getArtistReleases()
    {
        if (artistReleases == null)
            initArtistReleases();
        return artistReleases;
    }
}
