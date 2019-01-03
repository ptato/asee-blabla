package com.ptato.aseeblabla;

import android.app.SearchManager;
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

import com.ptato.aseeblabla.data.DiscogsAPIUtils;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.Release;
import com.ptato.aseeblabla.ui.detail.artist.ArtistDetailActivity;
import com.ptato.aseeblabla.ui.detail.release.ReleaseDetailActivity;
import com.ptato.aseeblabla.ui.list.UserReleasesFragment;

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

    public class OpenArtistDetailListener implements ArtistsFragment.OnClickArtistListener
    {
        @Override public void onClick(Artist a) { changeToDetailArtistView(a); }
    }

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

            if (f != null)
            {
                if (getCurrentView().equals(ArtistsFragment.class.getSimpleName())
                        && ((ArtistsFragment)f).getArtistCount() == 0)
                {
                    ((ArtistsFragment)f).setArtists(new ArrayList<Artist>());


                } else if (getCurrentView().equals(UserReleasesFragment.class.getSimpleName())
                        && ((UserReleasesFragment)f).isUsingSearchResults()) {
                    ((UserReleasesFragment)f).clearSearchQuery();


                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                {
                    fm.popBackStack();
                } else
                {
                    super.onBackPressed();
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
                    if (getCurrentView().equals(SearchReleasesFragment.class.getSimpleName()))
                    {
                        //noinspection unchecked
                        new DiscogsSearchQueryTask().execute(
                                Pair.create(DiscogsSearchQueryTask.TYPE, DiscogsSearchQueryTask.TYPE_RELEASE),
                                Pair.create(DiscogsSearchQueryTask.COMBINED_TITLE, s));
                        searchView.clearFocus();
                        menu.findItem(R.id.action_search).collapseActionView();
                        return true;
                    } else if (getCurrentView().equals(ArtistsFragment.class.getSimpleName()))
                    {
                        // noinspection unchecked
                        new DiscogsSearchQueryTask().execute(
                                Pair.create(DiscogsSearchQueryTask.TYPE, DiscogsSearchQueryTask.TYPE_ARTIST),
                                Pair.create(DiscogsSearchQueryTask.COMBINED_TITLE, s));
                        searchView.clearFocus();
                        menu.findItem(R.id.action_search).collapseActionView();
                        return true;
                    } else if (getCurrentView().equals(UserReleasesFragment.class.getSimpleName()))
                    {
                        Fragment f = getSupportFragmentManager().findFragmentById(R.id.home_content_area);
                        if (f != null) ((UserReleasesFragment)f).setSearchQuery(s);
                        searchView.clearFocus();
                        menu.findItem(R.id.action_search).collapseActionView();
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
        if (id == R.id.nav_releases && !getCurrentView().equals(SearchReleasesFragment.class.getSimpleName()))
        {
            changeToSearchReleaseView();
        } else if (id == R.id.nav_artists && !getCurrentView().equals(ArtistsFragment.class.getSimpleName()))
        {
            changeToGeneralArtistView();
        } else if (id == R.id.nav_my_releases && !getCurrentView().equals(UserReleasesFragment.class.getSimpleName()))
        {
            changeToUserReleaseView();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class DiscogsGetArtistReleasesTask extends AsyncTask<Integer, Void, JSONObject>
    {
        private static final String baseURL = "https://api.discogs.com/";
        private static final String authToken = "GRkDMwQSYOBKnnWIOeJTokxkkViZQIrqcLzJilow";
        private SearchReleasesFragment srf;
        private HomeActivity ha;

        public DiscogsGetArtistReleasesTask(HomeActivity _ha, SearchReleasesFragment _srf)
        {
            srf = _srf;
            ha = _ha;
        }


        @Override
        protected JSONObject doInBackground(Integer... integers)
        {
            JSONObject jsonResult = null;

            if (integers.length > 0)
            {
                StringBuilder queryURLBuilder = new StringBuilder(
                        baseURL + "artists/" + integers[0].toString() + "/releases");
                Log.i(this.getClass().getName(), queryURLBuilder.toString());
                jsonResult = DiscogsAPIUtils.getResponseFromDiscogs(queryURLBuilder.toString());
            }

            return jsonResult;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject)
        {
            super.onPostExecute(jsonObject);

            List<Release> releasesResult = new ArrayList<>();

            if (jsonObject != null)
            {
                JSONArray releases = jsonObject.optJSONArray("releases");
                if (releases != null)
                {
                    for (int i = 0; i < releases.length(); ++i)
                    {
                        JSONObject object = releases.optJSONObject(i);
                        if (object.optString("type", "").equals("release"))
                        {
                            Release release = new Release();
                            release.discogsId = object.optInt("id", -1);

                            release.title = object.optString("title", "Unknown Title");
                            release.thumbUrl = object.optString("thumb", "");
                            release.year = Integer.toString(object.optInt("year", 0));

                            releasesResult.add(release);
                        }

                    }
                }
            }

            srf.setReleases(releasesResult);
        }
    }

    private class DiscogsSearchQueryTask extends AsyncTask<Pair<String, String>, Void, JSONObject>
    {
        //private static final String baseURL = "https://api.discogs.com/releases/249504";
        private static final String baseURL = "https://api.discogs.com/";
        private static final String authToken = "GRkDMwQSYOBKnnWIOeJTokxkkViZQIrqcLzJilow";

        public static final String TYPE = "type";
        public static final String TYPE_RELEASE = "release";
        public static final String TYPE_ARTIST = "artist";
        public static final String TYPE_MASTER = "master";
        public static final String TYPE_LABEL = "label";

        public static final String NORMAL_QUERY = "query";
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
        protected final JSONObject doInBackground(Pair<String, String>... params)
        {
            JSONObject jsonResult = null;

            if (params.length > 0)
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
                jsonResult = DiscogsAPIUtils.getResponseFromDiscogs(queryURLBuilder.toString());
            }

            boolean isArtists = true;
            for (Pair<String, String> param : params)
            {
                if (Objects.equals(param.first, TYPE))
                {
                    isArtists = Objects.equals(param.second, TYPE_ARTIST);
                }
            }

            try
            {
                jsonResult.put("type_of_json", isArtists);
            } catch (Exception e)
            {
                Log.e(DiscogsSearchQueryTask.class.getName(), "Did not add type_of_json. This isn't going to work.");
            }

            return jsonResult;
        }

        @Override
        protected void onPostExecute(JSONObject json)
        {
            super.onPostExecute(json);

            try
            {
                boolean isArtists = json.getBoolean("type_of_json");
                if (json != null && json.has("results"))
                {
                    if (isArtists)
                    {
                        List<Artist> artists = new ArrayList<>();

                        JSONArray results = json.getJSONArray("results");
                        for (int artistIndex = 0; artistIndex < results.length(); ++artistIndex)
                        {
                            JSONObject artist = results.getJSONObject(artistIndex);
                            if (artist.optString("type").equals(TYPE_ARTIST))
                            {
                                Artist a = new Artist();

                                a.discogsId = artist.optInt("id", -1);
                                a.name = artist.optString("title");
                                a.imgUrl = artist.optString("thumb");

                                artists.add(a);
                            }

                        }
;

                        if(getCurrentView().equals(ArtistsFragment.class.getSimpleName()))
                        {
                            ArtistsFragment af = (ArtistsFragment)getSupportFragmentManager().findFragmentById(R.id.home_content_area);
                            if (af != null) af.setArtists(artists);
                        }

                    } else
                    {
                        List<Release> releases = new ArrayList<>();

                        JSONArray results = json.getJSONArray("results");
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

                        if(getCurrentView().equals(SearchReleasesFragment.class.getSimpleName()))
                        {
                            SearchReleasesFragment rf = (SearchReleasesFragment)getSupportFragmentManager().findFragmentById(R.id.home_content_area);
                            if (rf != null) rf.setReleases(releases, true);
                        }
                    }
                }
            } catch (JSONException e)
            {
                Log.e(DiscogsSearchQueryTask.class.getName(), "Error al parsear JSON");
                Log.e(DiscogsSearchQueryTask.class.getName(), e.getMessage());
            }
        }
    }

    public SearchReleasesFragment changeToSearchReleaseView() { return changeToSearchReleaseView(true);}
    public SearchReleasesFragment changeToSearchReleaseView(boolean addToBackStack)
    {
        SearchReleasesFragment searchReleasesFragment = new SearchReleasesFragment();
        searchReleasesFragment.setItemOnClickListener(new OpenReleaseDetailListener());
        FragmentTransaction replace = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, searchReleasesFragment, SearchReleasesFragment.class.getSimpleName());
        if (addToBackStack) replace.addToBackStack(null);
        replace.commit();
        return searchReleasesFragment;
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

    public void changeToGeneralArtistView()
    {
        ArtistsFragment artistsFragment = new ArtistsFragment();
        artistsFragment.setItemOnClickListener(new OpenArtistDetailListener());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, artistsFragment, ArtistsFragment.class.getSimpleName())
                .addToBackStack(null)
                .commit();
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

    public String getCurrentView()
    {
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.home_content_area);
        return f != null ? f.getClass().getSimpleName() : "";
    }
}
