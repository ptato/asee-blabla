package com.ptato.aseeblabla.ui.list;

import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ptato.aseeblabla.R;
import com.ptato.aseeblabla.data.DiscogsAPIUtils;
import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.Release;
import com.ptato.aseeblabla.ui.AppViewModelFactory;
import com.ptato.aseeblabla.ui.detail.artist.ArtistDetailActivity;
import com.ptato.aseeblabla.ui.detail.release.ReleaseDetailActivity;
import com.ptato.aseeblabla.ui.list.user.UserReleasesFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    public class OpenReleaseDetailListener implements ReleasesFragmentAdapter.OnClickReleaseListener
    {
        @Override public void onClick(Release release)
        {
            changeToDetailReleaseView(release);
        }
    }

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

        changeToUserReleaseView(false);
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
            Fragment f = fm.findFragmentById(R.id.home_content_area);

            if (f instanceof ShowArtistsFragment)
            {
                // @NOTE: ShowArtistsFragment is (for now) only being used for showing
                // artist search results. Which mean it's always showing artist search data we
                // gave to it from our ViewModel.
                List<Artist> artists = viewModel.getArtistSearchResults().getValue();
                if (artists != null && artists.size() > 0)
                {
                    viewModel.setArtistSearchQuery(null);
                    ((ShowArtistsFragment) f).showArtists(null);
                } else
                {
                    fm.popBackStack();
                }
            } else if (f instanceof UserReleasesFragment)
            {
                if (((UserReleasesFragment)f).isUsingSearchResults())
                    ((UserReleasesFragment)f).clearSearchQuery();
                else
                    fm.popBackStack();

            } else if (f instanceof ShowReleasesFragment)
            {
                // @TODO: Need to un-show the results and go back to initial view of this tab
                fm.popBackStack();
            } else if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            {
                fm.popBackStack();
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

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchManager != null)
        {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
            {
                @Override
                public boolean onQueryTextSubmit(String s)
                {
                    Fragment f = getSupportFragmentManager().findFragmentById(R.id.home_content_area);
                    Log.i(HomeActivity.this.getClass().getSimpleName(), "La búsqueda es '" + s + "'");
                    Log.i(HomeActivity.this.getClass().getSimpleName(),
                            "El fragmento actual es " + (f==null?"'NULL'":f.getClass().getSimpleName()));
                    if (f instanceof ShowArtistsFragment)
                    {
                        Log.i(HomeActivity.this.getClass().getSimpleName(), "Buscando artistas '" + s + "'");
                        viewModel.setArtistSearchQuery(s);
                        ((ShowArtistsFragment)f).showArtists(viewModel.getArtistSearchResults());

                        searchView.clearFocus();
                        menu.findItem(R.id.action_search).collapseActionView();
                        return true;
                    } else if (f instanceof ShowReleasesFragment)
                    {
                        viewModel.setReleaseSearchQuery(s);
                        ((ShowReleasesFragment)f).showReleases(viewModel.getReleaseSearchResults());

                        searchView.clearFocus();
                        menu.findItem(R.id.action_search).collapseActionView();
                        return true;
                    } else if (f instanceof UserReleasesFragment)
                    {
                        ((UserReleasesFragment)f).setSearchQuery(s);

                        searchView.clearFocus();
                        menu.findItem(R.id.action_search).collapseActionView();
                        return true;
                    }

                    return false;
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.home_content_area);
        if (id == R.id.nav_releases && !(f instanceof ShowReleasesFragment))
        {
            ShowReleasesFragment showReleasesFragment = new ShowReleasesFragment();
            showReleasesFragment.setItemOnClickListener(new OpenReleaseDetailListener());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_content_area, showReleasesFragment, ShowReleasesFragment.class.getSimpleName())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_artists && !(f instanceof ShowArtistsFragment))
        {
            ShowArtistsFragment showArtistsFragment = new ShowArtistsFragment();
            showArtistsFragment.setItemOnClickListener(new ShowArtistsFragment.OnClickArtistListener()
            {
                @Override
                public void onClick(Artist r)
                {
                    changeToDetailArtistView(r);
                }
            });
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_content_area, showArtistsFragment, ShowArtistsFragment.class.getSimpleName())
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_my_releases && !(f instanceof UserReleasesFragment))
        {
            changeToUserReleaseView();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public UserReleasesFragment changeToUserReleaseView() { return changeToUserReleaseView(true); }
    public UserReleasesFragment changeToUserReleaseView(boolean addToBackStack)
    {
        UserReleasesFragment urf = new UserReleasesFragment();
        urf.setItemOnClickListener(new OpenReleaseDetailListener());
        // urf.setReleases(userReleases);
        FragmentTransaction replace = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, urf, UserReleasesFragment.class.getSimpleName());
        if (addToBackStack) replace.addToBackStack(null);
        replace.commit();
        return urf;
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
        intent.putExtra(ArtistDetailActivity.ARTIST_ID_EXTRA, artist.discogsId);
        startActivity(intent);
    }
}
