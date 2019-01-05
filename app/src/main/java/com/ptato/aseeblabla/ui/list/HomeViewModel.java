package com.ptato.aseeblabla.ui.list;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.Release;

import java.util.List;

public class HomeViewModel extends ViewModel
{
    private final Repository repository;

    public enum AppNavigationTab
    {
        USER_RELEASES, SEARCH_RELEASES, SEARCH_ARTISTS
    }
    public AppNavigationTab currentNavigationTab;

    private final MutableLiveData<String> userReleaseSearchQueryInput = new MutableLiveData<>();
    private LiveData<List<Release>> userReleases =
            Transformations.switchMap(userReleaseSearchQueryInput, new Function<String, LiveData<List<Release>>>()
            {
                @Override
                public LiveData<List<Release>> apply(String input)
                {
                    return repository.searchUserReleases(input);
                }
            });

    public final MutableLiveData<String> artistSearchTitle = new MutableLiveData<>();
    private final MutableLiveData<String> artistSearchQueryInput = new MutableLiveData<>();
    private final LiveData<List<Artist>> artistSearchResults =
            Transformations.switchMap(artistSearchQueryInput, new Function<String, LiveData<List<Artist>>>()
            {
                @Override
                public LiveData<List<Artist>> apply(String input)
                {
                    if (input == null)
                    {
                        artistSearchTitle.setValue("Búsquedas Recientes");
                        return repository.getCachedArtists();
                    } else
                    {
                        artistSearchTitle.setValue(null);
                        return repository.searchArtists(input);
                    }
                }
            });

    private final MutableLiveData<String> releaseSearchQueryInput = new MutableLiveData<>();
    private final LiveData<List<Release>> releaseSearchResults =
            Transformations.switchMap(releaseSearchQueryInput, new Function<String, LiveData<List<Release>>>()
            {
                @Override
                public LiveData<List<Release>> apply(String input)
                {
                    return repository.searchReleases(input);
                }
            });

    public HomeViewModel(@NonNull Repository _repository)
    {
        repository = _repository;
        userReleaseSearchQueryInput.setValue(null);
        artistSearchQueryInput.setValue(null);
        currentNavigationTab = AppNavigationTab.USER_RELEASES;
    }

    public LiveData<List<Release>> getUserReleases()
    {
        return userReleases;
    }
    public void setUserReleaseSearchQuery(String query)
    {
        userReleaseSearchQueryInput.setValue(query);
    }
    public String getCurrentUserReleaseSearchQuery()
    {
        return userReleaseSearchQueryInput.getValue();
    }

    public LiveData<List<Artist>> getArtistSearchResults()
    {
        return artistSearchResults;
    }
    public void setArtistSearchQuery(String query)
    {
        artistSearchQueryInput.setValue(query);
    }

    public LiveData<List<Release>> getReleaseSearchResults()
    {
        return releaseSearchResults;
    }
    public void setReleaseSearchQuery(String query)
    {
        releaseSearchQueryInput.setValue(query);
    }
}
