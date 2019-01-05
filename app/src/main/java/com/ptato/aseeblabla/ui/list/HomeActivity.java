package com.ptato.aseeblabla.ui.list;

import android.app.SearchManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ptato.aseeblabla.R;
import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.Release;
import com.ptato.aseeblabla.ui.AppViewModelFactory;
import com.ptato.aseeblabla.ui.detail.artist.ArtistDetailActivity;
import com.ptato.aseeblabla.ui.detail.release.ReleaseDetailActivity;

import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Repository repository = Repository.getInstance(this);
        AppViewModelFactory factory = new AppViewModelFactory(repository);
        viewModel = ViewModelProviders.of(this, factory).get(HomeViewModel.class);

        switch (viewModel.currentNavigationTab)
        {
            case USER_RELEASES:
                changeToUserReleaseTab();
                break;
            case SEARCH_ARTISTS:
                changeToSearchArtistsTab();
                break;
            case SEARCH_RELEASES:
                changeToSearchReleasesTab();
                break;
        }

    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            FragmentManager fm = getSupportFragmentManager();

            if (fm.getBackStackEntryCount() > 0)
            {
                switch (viewModel.currentNavigationTab)
                {
                    case SEARCH_RELEASES:
                        // @TODO: Need to un-show the results and go back to initial view of this tab
                        fm.popBackStack();
                        break;

                    case SEARCH_ARTISTS:
                        if (viewModel.artistSearchQueryInput.getValue() != null)
                        {
                            viewModel.artistSearchQueryInput.setValue(null);
                        } else
                        {
                            fm.popBackStack();
                        }
                        break;

                    case USER_RELEASES:
                        if (viewModel.getCurrentUserReleaseSearchQuery() != null)
                        {
                            viewModel.setUserReleaseSearchQuery(null);
                        } else
                        {
                            fm.popBackStack();
                        }
                        break;
                }
            } else
            {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getMenuInflater().inflate(R.menu.home, menu);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchManager != null)
        {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
            {
                @Override
                public boolean onQueryTextSubmit(String s)
                {
                    Log.i(HomeActivity.this.getClass().getSimpleName(), "La búsqueda es '" + s + "'");

                    switch (viewModel.currentNavigationTab)
                    {
                        case USER_RELEASES:
                            viewModel.setUserReleaseSearchQuery(s);
                            break;

                        case SEARCH_ARTISTS:
                            viewModel.artistSearchQueryInput.setValue(s);
                            break;

                        case SEARCH_RELEASES:
                            viewModel.setReleaseSearchQuery(s);
                            break;
                    }

                    searchView.clearFocus();
                    menu.findItem(R.id.action_search).collapseActionView();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s)
                {
                    return false;
                }
            });
        } else
        {
            Log.e(this.getClass().getName(), "No se puede obtener SearchManager");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.nav_releases
                && viewModel.currentNavigationTab != HomeViewModel.AppNavigationTab.SEARCH_RELEASES)
        {
            changeToSearchReleasesTab();

        } else if (id == R.id.nav_artists
                && viewModel.currentNavigationTab != HomeViewModel.AppNavigationTab.SEARCH_ARTISTS)
        {
            changeToSearchArtistsTab();

        } else if (id == R.id.nav_my_releases
                && viewModel.currentNavigationTab != HomeViewModel.AppNavigationTab.USER_RELEASES)
        {
            changeToUserReleaseTab();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    public void changeToUserReleaseTab()
    {
        ShowReleasesFragment srf = new UserReleasesFragment();
        srf.setItemOnClickListener(new ShowReleasesFragment.OnClickReleaseListener()
        {
            @Override
            public void onClick(Release r)
            {
                changeToDetailReleaseView(r);
            }
        });
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, srf, "USER_RELEASES")
                .addToBackStack(null)
                .commit();
        viewModel.currentNavigationTab = HomeViewModel.AppNavigationTab.USER_RELEASES;
    }

    public static class UserReleasesFragment extends ShowReleasesFragment
    {
        @Override
        protected LiveData<List<Release>> getShownReleases()
        {
            FragmentActivity activity = getActivity();
            if (activity != null)
            {
                Repository repository = Repository.getInstance(activity);
                AppViewModelFactory factory = new AppViewModelFactory(repository);
                HomeViewModel viewModel = ViewModelProviders.of(activity, factory).get(HomeViewModel.class);
                return viewModel.getUserReleases();
            }

            return new MutableLiveData<>();
        }

        @Nullable @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View rootView = super.onCreateView(inflater, container, savedInstanceState);
            setTitle("Lanzamientos Guardados");
            return rootView;
        }
    }



    public void changeToSearchReleasesTab()
    {
        ShowReleasesFragment showReleasesFragment = new SearchReleasesFragment();
        showReleasesFragment.setItemOnClickListener(new ShowReleasesFragment.OnClickReleaseListener()
        {
            @Override
            public void onClick(Release r)
            {
                changeToDetailReleaseView(r);
            }
        });
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, showReleasesFragment, "SEARCH_RELEASES")
                .addToBackStack(null)
                .commit();
        viewModel.currentNavigationTab = HomeViewModel.AppNavigationTab.SEARCH_RELEASES;
    }

    public static class SearchReleasesFragment extends ShowReleasesFragment
    {
        @Override
        protected LiveData<List<Release>> getShownReleases()
        {
            FragmentActivity activity = getActivity();
            if (activity != null)
            {
                Repository repository = Repository.getInstance(activity);
                AppViewModelFactory factory = new AppViewModelFactory(repository);
                HomeViewModel viewModel = ViewModelProviders.of(activity, factory).get(HomeViewModel.class);
                return viewModel.getReleaseSearchResults();
            }

            return new MutableLiveData<>();
        }
    }




    public void changeToSearchArtistsTab()
    {
        ShowArtistsFragment saf = new SearchArtistsFragment();
        saf.setItemOnClickListener(new ShowArtistsFragment.OnClickArtistListener()
        {
            @Override
            public void onClick(Artist r)
            {
                changeToDetailArtistView(r);
            }
        });
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, saf, "SEARCH_ARTISTS")
                .addToBackStack(null)
                .commit();
        viewModel.currentNavigationTab = HomeViewModel.AppNavigationTab.SEARCH_ARTISTS;
    }

    public static class SearchArtistsFragment extends ShowArtistsFragment
    {
        @Override
        protected LiveData<List<Artist>> getShownArtists()
        {
            FragmentActivity activity = getActivity();
            if (activity != null)
            {
                Repository repository = Repository.getInstance(activity);
                AppViewModelFactory factory = new AppViewModelFactory(repository);
                HomeViewModel viewModel = ViewModelProviders.of(activity, factory).get(HomeViewModel.class);
                return viewModel.getArtistSearchResults();
            }

            return new MutableLiveData<>();
        }

        @Override
        protected LiveData<String> getShownTitle()
        {
            FragmentActivity activity = getActivity();
            if (activity != null)
            {
                Repository repository = Repository.getInstance(activity);
                AppViewModelFactory factory = new AppViewModelFactory(repository);
                HomeViewModel viewModel = ViewModelProviders.of(activity, factory).get(HomeViewModel.class);
                return viewModel.artistSearchTitle;
            }

            return new MutableLiveData<>();
        }
    }


    public void changeToDetailReleaseView(Release release)
    {
        Intent intent = new Intent(this, ReleaseDetailActivity.class);
        intent.putExtra(ReleaseDetailActivity.RELEASE_ID_EXTRA, release.discogsId);
        startActivity(intent);
    }

    public void changeToDetailArtistView(Artist artist)
    {
        Intent intent = new Intent(this, ArtistDetailActivity.class);
        Repository.getInstance(this).bumpCachedArtist(artist.discogsId);
        intent.putExtra(ArtistDetailActivity.ARTIST_ID_EXTRA, artist.discogsId);
        startActivity(intent);
    }
}
