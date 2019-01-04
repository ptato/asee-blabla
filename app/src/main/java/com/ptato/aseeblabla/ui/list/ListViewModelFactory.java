package com.ptato.aseeblabla.ui.list;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.ui.list.artists.ArtistSearchViewModel;
import com.ptato.aseeblabla.ui.list.user.UserReleasesViewModel;

public class ListViewModelFactory extends ViewModelProvider.NewInstanceFactory
{
    private Repository repository;

    public ListViewModelFactory(Repository _repository)
    {
        repository = _repository;
    }

    @NonNull @Override @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
    {
        if (UserReleasesViewModel.class.getSimpleName().equals(modelClass.getSimpleName()))
            return (T) new UserReleasesViewModel(repository);
        else
            return (T) new ArtistSearchViewModel(repository);
    }
}
