package com.ptato.aseeblabla.ui.list;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Release;

import java.util.List;

public class UserReleasesViewModel extends ViewModel
{
    private LiveData<List<Release>> userReleases;

    public UserReleasesViewModel(@NonNull Repository repository)
    {
        userReleases = repository.getUserReleases();
    }

    public LiveData<List<Release>> getUserReleases()
    {
        return userReleases;
    }
}
