package com.ptato.aseeblabla.ui.detail.release;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Release;

public class ReleaseDetailViewModel extends ViewModel
{
    private LiveData<Release> releaseData;
    private boolean isReleaseSaved;

    public ReleaseDetailViewModel(@NonNull Repository repository, int id)
    {
        releaseData = repository.getRelease(id);
        isReleaseSaved = repository.isReleaseSavedByUser(id);
    }

    public LiveData<Release> getRelease()
    {
        return releaseData;
    }
    public boolean isReleaseSavedByUser()
    {
        return isReleaseSaved;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory
    {
        Repository repository;
        int id;
        public Factory(Repository re, int a) {
            repository = re;
            id = a;
        }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
        {
            // noinspection unchecked
            return (T) new ReleaseDetailViewModel(repository, id);
        }
    }
}
