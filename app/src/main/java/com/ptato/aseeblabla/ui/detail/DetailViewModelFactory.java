package com.ptato.aseeblabla.ui.detail;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.ui.detail.artist.ArtistDetailViewModel;
import com.ptato.aseeblabla.ui.detail.release.ReleaseDetailViewModel;

public class DetailViewModelFactory extends ViewModelProvider.NewInstanceFactory
{
    private Repository repository;
    private int id;

    public DetailViewModelFactory(Repository _repository, int _id) {
        repository = _repository;
        id = _id;
    }

    @NonNull @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
    {
        if (ReleaseDetailViewModel.class.getSimpleName().equals(modelClass.getSimpleName()))
            // noinspection unchecked
            return (T) new ReleaseDetailViewModel(repository, id);
        else
            // noinspection unchecked
            return (T) new ArtistDetailViewModel(repository, id);
    }
}
