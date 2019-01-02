package com.ptato.aseeblabla.ui.detail.artist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Artist;

public class ArtistDetailViewModel extends ViewModel
{
    private LiveData<Artist> artistData;

    public ArtistDetailViewModel(Repository repository, int id)
    {
        artistData = repository.getArtist(id);
    }

    public LiveData<Artist> getArtist()
    {
        return artistData;
    }
}
