package com.ptato.aseeblabla.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.ui.detail.artist.ArtistDetailViewModel;
import com.ptato.aseeblabla.ui.detail.release.ReleaseDetailViewModel;
import com.ptato.aseeblabla.ui.list.HomeViewModel;
import com.ptato.aseeblabla.ui.list.user.UserReleasesViewModel;

public class AppViewModelFactory extends ViewModelProvider.NewInstanceFactory
{
    private Repository repository;
    private int id;

    public AppViewModelFactory(Repository _repository, int _id)
    {
        repository = _repository;
        id = _id;
    }

    public AppViewModelFactory(Repository _repository)
    {
        repository = _repository;
        id = -1;
    }

    @NonNull @Override @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
    {
        if (ReleaseDetailViewModel.class.getSimpleName().equals(modelClass.getSimpleName()))
            return (T) new ReleaseDetailViewModel(repository, id);
        else if (ArtistDetailViewModel.class.getSimpleName().equals(modelClass.getSimpleName()))
            return (T) new ArtistDetailViewModel(repository, id);
        else if (UserReleasesViewModel.class.getSimpleName().equals(modelClass.getSimpleName()))
            return (T) new UserReleasesViewModel(repository);
        else
            return (T) new HomeViewModel(repository);
    }
}
