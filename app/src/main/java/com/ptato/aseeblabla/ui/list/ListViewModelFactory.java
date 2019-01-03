package com.ptato.aseeblabla.ui.list;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.ui.detail.artist.ArtistDetailViewModel;
import com.ptato.aseeblabla.ui.detail.release.ReleaseDetailViewModel;

public class ListViewModelFactory extends ViewModelProvider.NewInstanceFactory
{
    private Repository repository;

    public ListViewModelFactory(Repository _repository)
    {
        repository = _repository;
    }

    @NonNull @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
    {
        //if (UserReleasesViewModel.class.getSimpleName().equals(modelClass.getSimpleName()))
            // noinspection unchecked
            return (T) new UserReleasesViewModel(repository);
    }
}
