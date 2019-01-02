package com.ptato.aseeblabla.ui.detail.artist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.db.Artist;

public class ArtistDetailViewModel extends ViewModel
{
    private LiveData<Artist> artistData;

    public ArtistDetailViewModel(int id)
    {
        artistData = Repository.getInstance().getArtist(id);
    }

    public LiveData<Artist> getArtist()
    {
        return artistData;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory
    {
        int id;
        public Factory(int a) { id = a; }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
        {
            return (T) new ArtistDetailViewModel(id);
        }
    }
}
