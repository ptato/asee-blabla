package com.ptato.aseeblabla.ui.list;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.Release;

import java.util.List;

public class HomeViewModel extends ViewModel
{
    private final Repository repository;
    private final MutableLiveData<String> searchQueryInput = new MutableLiveData<>();
    private final LiveData<List<Artist>> artists =
            Transformations.switchMap(searchQueryInput, new Function<String, LiveData<List<Artist>>>()
            {
                @Override
                public LiveData<List<Artist>> apply(String input)
                {
                    return repository.searchArtists(input);
                }
            });

    public HomeViewModel(@NonNull Repository _repository)
    {
        repository = _repository;
    }

    public LiveData<List<Artist>> getArtists()
    {
        return artists;
    }
    public void setSearchQuery(String query)
    {
        searchQueryInput.setValue(query);
    }
}
