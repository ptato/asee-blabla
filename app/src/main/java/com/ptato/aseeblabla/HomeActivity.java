package com.ptato.aseeblabla;

import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    public class OpenReleaseDetailListener implements ReleasesFragment.OnClickReleaseListener
    {
        @Override
        public void onClick(Release release)
        {
            changeToDetailReleaseView(release);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (getCurrentView().equals(ReleaseDetailFragment.class.getSimpleName()))
                {
                    Snackbar.make(view, "Añadir a nosedonde", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else
                {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        changeToGeneralReleaseView(false);
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

            if (getCurrentView().equals(ReleasesFragment.class.getSimpleName())
                    && ((ReleasesFragment)f).isSearching())
            {
                ((ReleasesFragment)f).setReleases(new ArrayList<Release>());
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
                    if (getCurrentView().equals(ReleasesFragment.class.getSimpleName()))
                    {
                        //noinspection unchecked
                        new GetMusicAPITask().execute(
                                Pair.create(GetMusicAPITask.TYPE, GetMusicAPITask.TYPE_RELEASE),
                                Pair.create(GetMusicAPITask.RELEASE_TITLE, s));
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_releases && !getCurrentView().equals(ReleasesFragment.class.getSimpleName()))
        {
            changeToGeneralReleaseView();
        } else if (id == R.id.nav_artists && !getCurrentView().equals("TODO TODO TODO TODO TODO"))
        {
            changeToGeneralArtistView();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetMusicAPITask extends AsyncTask<Pair<String, String>, Void, List<Release>>
    {
        //private static final String baseURL = "https://api.discogs.com/releases/249504";
        private static final String baseURL = "https://api.discogs.com/";
        private static final String authToken = "GRkDMwQSYOBKnnWIOeJTokxkkViZQIrqcLzJilow";

        public static final String TYPE = "type";
        public static final String TYPE_RELEASE = "release";
        public static final String TYPE_ARTIST = "artist";
        public static final String TYPE_MASTER = "master";
        public static final String TYPE_LABEL = "label";

        public static final String COMBINED_TITLE = "title";
        public static final String RELEASE_TITLE = "release_title";
        public static final String CREDIT = "credit";
        public static final String ARTIST = "artist";
        public static final String GENRE = "genre";
        public static final String STYLE = "style";
        public static final String COUNTRY = "country";
        public static final String YEAR = "year";

        @SafeVarargs
        @Override
        protected final List<Release> doInBackground(Pair<String, String>... params)
        {
            JSONObject jsonResult = null;

            if (params.length > 0)
            {
                try
                {
                    StringBuilder queryURLBuilder = new StringBuilder(baseURL + "database/search?");
                    int paramIndex = 0;
                    while (paramIndex < params.length)
                    {
                        queryURLBuilder.append(params[paramIndex].first);
                        queryURLBuilder.append("=");
                        queryURLBuilder.append(params[paramIndex].second);
                        paramIndex++;

                        if (paramIndex < params.length)
                            queryURLBuilder.append("&");
                    }
                    Log.i(this.getClass().getName(), queryURLBuilder.toString());
                    URL queryURL = new URL(queryURLBuilder.toString());

                    HttpURLConnection urlConnection = (HttpURLConnection)queryURL.openConnection();
                    urlConnection.setRequestProperty("User-Agent", "ASEE-Blabla/1.0");
                    urlConnection.setRequestProperty("Authorization", "Discogs token="+authToken);

                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    while (true)
                    {
                        String line = bufferedReader.readLine();
                        if (line == null)
                            break;
                        stringBuilder.append(line);
                    }


                    if (urlConnection.getResponseCode() != 200)
                    {
                        Log.e(GetMusicAPITask.class.getName(), "Codigo de respuesta de error");
                        throw new IOException();
                    }

                    String stringResponse = stringBuilder.toString();
                    jsonResult = new JSONObject(stringResponse);
                } catch (MalformedURLException e)
                {
                    Log.e(GetMusicAPITask.class.getName(), "No se puede crear la URL");
                    Log.e(GetMusicAPITask.class.getName(), e.getMessage());
                } catch (IOException e)
                {
                    Log.e(GetMusicAPITask.class.getName(), "No se puede conectar a la URL");
                    Log.e(GetMusicAPITask.class.getName(), e.getMessage());
                } catch (JSONException e)
                {
                    Log.e(GetMusicAPITask.class.getName(), "Recibido JSON incorrecto");
                    Log.e(GetMusicAPITask.class.getName(), e.getMessage());
                }

                try
                {
                    Log.i(GetMusicAPITask.class.getName(), jsonResult == null ? "JSON Vacío" : jsonResult.toString(4));
                } catch (JSONException e)
                {
                    Log.i(GetMusicAPITask.class.getName(), "Can't log JSON");
                }
            }

            List<Release> releases = new ArrayList<>();
            try
            {
                if (jsonResult != null && jsonResult.has("results"))
                {
                    JSONArray results = jsonResult.getJSONArray("results");
                    for(int releaseIndex = 0; releaseIndex < results.length(); ++releaseIndex)
                    {
                        JSONObject release = results.getJSONObject(releaseIndex);

                        Release r = new Release();
                        r.discogsId = release.optInt("id", -1);
                        r.thumbUrl = release.optString("thumb", "");

                        String defaultTitle = "Unknown Artist - Unknown Title";
                        r.title = release.optString("title", defaultTitle).split("-")[1].substring(1);
                        r.artist = release.optString("title", defaultTitle).split("-")[0];
                        r.artist = r.artist.substring(0, r.artist.length() - 1);
                        r.year = release.optString("year", "Unknown Year");
                        // uri, format (list<string>), label (list<string>), cover_image,
                        // genre (list<string>), resource_url, style (list<string>)
                        releases.add(r);
                    }
                }
            } catch (JSONException e)
            {
                Log.e(GetMusicAPITask.class.getName(), "Error al parsear JSON");
                Log.e(GetMusicAPITask.class.getName(), e.getMessage());
            }

            return releases;
        }

        @Override
        protected void onPostExecute(List<Release> releases)
        {
            super.onPostExecute(releases);
            if(getCurrentView().equals(ReleasesFragment.class.getSimpleName()))
            {
                ReleasesFragment rf = (ReleasesFragment)getSupportFragmentManager().findFragmentById(R.id.home_content_area);
                if (rf != null) rf.setReleases(releases, true);
            }
        }
    }

    public void changeToGeneralReleaseView() { changeToGeneralReleaseView(true);}

    public void changeToGeneralReleaseView(boolean addToBackStack)
    {
        ReleasesFragment releasesFragment = new ReleasesFragment();
        releasesFragment.setItemOnClickListener(new OpenReleaseDetailListener());
        FragmentTransaction replace = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, releasesFragment, ReleasesFragment.class.getSimpleName());
        if (addToBackStack) replace.addToBackStack(null);
        replace.commit();
    }

    public void changeToGeneralArtistView()
    {
        ReleasesFragment artistsFragment = new ReleasesFragment();
        artistsFragment.setItemOnClickListener(new OpenReleaseDetailListener());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, artistsFragment, "TODO TODO TODO TODO")
                .addToBackStack(null)
                .commit();
    }

    public void changeToDetailReleaseView(Release release)
    {
        ReleaseDetailFragment releaseDetailFragment = new ReleaseDetailFragment();
        releaseDetailFragment.setRelease(release);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, releaseDetailFragment, ReleasesFragment.class.getSimpleName())
                .addToBackStack(null)
                .commit();
    }

    public String getCurrentView()
    {
        FragmentManager fm = getSupportFragmentManager();
        String name = fm.findFragmentById(R.id.home_content_area).getClass().getSimpleName();
        return name;
    }
}
