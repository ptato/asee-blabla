package com.ptato.aseeblabla.ui.detail.release;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Release;

public class ReleaseDetailViewModel extends ViewModel
{
    private LiveData<Release> releaseData;
    private LiveData<Integer> isReleaseSaved;

    public ReleaseDetailViewModel(@NonNull Repository repository, int id)
    {
        releaseData = repository.getRelease(id);
        isReleaseSaved = repository.getUserReleaseCountWithId(id);
    }

    public LiveData<Release> getRelease()
    {
        return releaseData;
    }
    public LiveData<Integer> isReleaseSavedByUser()
    {
        return isReleaseSaved;
    }
}
