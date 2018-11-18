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

import com.ptato.aseeblabla.db.AppDatabase;
import com.ptato.aseeblabla.db.Artist;
import com.ptato.aseeblabla.db.Release;
import com.ptato.aseeblabla.db.ReleaseDAO;

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
    public List<Release> userReleases;

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

    FloatingActionButton fab = null;
    MenuItem searchItem = null;
    MenuItem deleteItem = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (getCurrentView().equals(ReleaseDetailFragment.class.getSimpleName()))
                {
                    ReleaseDetailFragment rdf =
                            (ReleaseDetailFragment)getSupportFragmentManager().findFragmentById(R.id.home_content_area);
                    Release newRelease = rdf.getRelease();

                    if (userReleases.contains(newRelease))
                    {
                        userReleases.set(userReleases.indexOf(newRelease), newRelease);
                        Snackbar.make(view, newRelease.title + " editado.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else
                    {
                        userReleases.add(newRelease);
                        setFABModeEdit();
                        Snackbar.make(view, newRelease.title + " se ha añadido.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }


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

        final UserReleasesFragment urf = changeToUserReleaseView(false);
        userReleases = new ArrayList<>();
        new AsyncTask<Context, Void, List<Release>>() {
            @Override
            protected List<Release> doInBackground(Context ... contexts)
            {
                AppDatabase db = AppDatabase.getInstance(contexts[0]);
                userReleases = db.releaseDAO().getAll();
                return userReleases;
            }

            @Override
            protected void onPostExecute(List<Release> releases)
            {
                if (urf != null) urf.setReleases(releases);
            }
        }.execute(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        new AsyncTask<Context, Void, Void>() {
            @Override
            protected Void doInBackground(Context... contexts)
            {
                ReleaseDAO releaseDAO = AppDatabase.getInstance(contexts[0]).releaseDAO();
                List<Release> existingReleases = releaseDAO.getAll();
                releaseDAO.deleteReleases(existingReleases);
                releaseDAO.insertReleases(userReleases);
                return null;
            }
        }.execute(this);
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
                    if (getCurrentView().equals(ReleaseDetailFragment.class.getSimpleName()))
                    {
                        setFABModeAdd();
                    }
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

        searchItem = menu.findItem(R.id.action_search);
        deleteItem = menu.findItem(R.id.action_delete);

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
                        new DiscogsSearchQueryTask().execute(
                                Pair.create(DiscogsSearchQueryTask.TYPE, DiscogsSearchQueryTask.TYPE_ARTIST),
                                Pair.create(DiscogsSearchQueryTask.COMBINED_TITLE, s));
                        searchView.clearFocus();
                        menu.findItem(R.id.action_search).collapseActionView();
                        return true;
                    } else if (getCurrentView().equals(UserReleasesFragment.class.getSimpleName()))
                    {
                        Fragment f = getSupportFragmentManager().findFragmentById(R.id.home_content_area);
                        ((UserReleasesFragment)f).setSearchQuery(s);
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
        } else if (id == R.id.action_delete)
        {
            if (getCurrentView().equals(ReleaseDetailFragment.class.getSimpleName()))
            {
                ReleaseDetailFragment rdf = (ReleaseDetailFragment)getSupportFragmentManager().findFragmentById(R.id.home_content_area);
                Release deleteRelease = rdf.getRelease();
                userReleases.remove(deleteRelease);
                getSupportFragmentManager().popBackStack();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
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

    private JSONObject getResponseFromDiscogs(String url, String authToken)
    {
        JSONObject jsonResult = null;

        try
        {
            URL queryURL = new URL(url);

            HttpURLConnection urlConnection = (HttpURLConnection) queryURL.openConnection();
            urlConnection.setRequestProperty("User-Agent", "ASEE-Blabla/1.0");
            urlConnection.setRequestProperty("Authorization", "Discogs token=" + authToken);

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
                Log.e(DiscogsSearchQueryTask.class.getName(), "Codigo de respuesta de error");
                throw new IOException();
            }

            String stringResponse = stringBuilder.toString();
            jsonResult = new JSONObject(stringResponse);
        } catch (MalformedURLException e)
        {
            Log.e(DiscogsSearchQueryTask.class.getName(), "No se puede crear la URL");
            Log.e(DiscogsSearchQueryTask.class.getName(), e.getMessage());
        } catch (IOException e)
        {
            Log.e(DiscogsSearchQueryTask.class.getName(), "No se puede conectar a la URL");
            Log.e(DiscogsSearchQueryTask.class.getName(), e.getMessage());
        } catch (JSONException e)
        {
            Log.e(DiscogsSearchQueryTask.class.getName(), "Recibido JSON incorrecto");
            Log.e(DiscogsSearchQueryTask.class.getName(), e.getMessage());
        }

        try
        {
            Log.i(DiscogsSearchQueryTask.class.getName(), jsonResult == null ? "JSON Vacío" : jsonResult.toString(4));
        } catch (JSONException e)
        {
            Log.i(DiscogsSearchQueryTask.class.getName(), "Can't log JSON");
        }

        return jsonResult;
    }
    private class DiscogsGetReleaseDetailsTask extends AsyncTask<Integer, Void, JSONObject>
    {
        private static final String baseURL = "https://api.discogs.com/";
        private static final String authToken = "GRkDMwQSYOBKnnWIOeJTokxkkViZQIrqcLzJilow";
        private ReleaseDetailFragment rdf;

        public DiscogsGetReleaseDetailsTask(ReleaseDetailFragment _rdf)
        {
            rdf = _rdf;
        }

        @Override
        protected JSONObject doInBackground(Integer... integers)
        {
            JSONObject jsonResult = null;

            if (integers.length > 0)
            {
                StringBuilder queryURLBuilder = new StringBuilder(baseURL + "releases/" + integers[0].toString());
                Log.i(this.getClass().getName(), queryURLBuilder.toString());
                jsonResult = getResponseFromDiscogs(queryURLBuilder.toString(), authToken);
            }

            return jsonResult;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject)
        {
            super.onPostExecute(jsonObject);

            Release r = new Release();
            r.discogsId = jsonObject.optInt("id", -1);
            r.title = jsonObject.optString("title", "Unknown Title");
            r.year = Integer.toString(jsonObject.optInt("year", 0));
            r.thumbUrl = jsonObject.optString("thumb", "");
            r.country = jsonObject.optString("country", "");

            JSONArray array = jsonObject.optJSONArray("artists");
            JSONObject artistObject = array == null ? null : array.optJSONObject(0);
            r.artist = artistObject == null ? "Unknown Artist" :
                    artistObject.optString("name", "Unknown Artist");
            r.artistId = artistObject == null ? -1 :
                    artistObject.optInt("id", -1);

            JSONObject community = jsonObject.optJSONObject("community");
            JSONObject rating = community == null ? null : community.optJSONObject("rating");
            r.communityStars = rating == null ? 0 : (int)Math.round(rating.optDouble("average", 0));

            JSONArray images = jsonObject.optJSONArray("images");
            JSONObject image0 = images == null ? null : images.optJSONObject(0);
            r.imageUrl = image0 == null ? "" : image0.optString("uri", "");

            JSONArray genres = jsonObject.optJSONArray("genres");
            JSONArray styles = jsonObject.optJSONArray("styles");
            r.genres =
                    (genres == null ? "" : genres.toString())
                    + (styles == null ? "" : styles.toString());

            rdf.setRelease(r);
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
                jsonResult = getResponseFromDiscogs(queryURLBuilder.toString(), authToken);
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

    public void changeToSearchReleaseView() { changeToSearchReleaseView(true);}
    public void changeToSearchReleaseView(boolean addToBackStack)
    {
        SearchReleasesFragment searchReleasesFragment = new SearchReleasesFragment();
        searchReleasesFragment.setItemOnClickListener(new OpenReleaseDetailListener());
        FragmentTransaction replace = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, searchReleasesFragment, SearchReleasesFragment.class.getSimpleName());
        if (addToBackStack) replace.addToBackStack(null);
        setFABModeInvisible();
        replace.commit();
    }

    public UserReleasesFragment changeToUserReleaseView() { return changeToUserReleaseView(true); }
    public UserReleasesFragment changeToUserReleaseView(boolean addToBackStack)
    {
        UserReleasesFragment urf = new UserReleasesFragment();
        urf.setItemOnClickListener(new OpenReleaseDetailListener());
        urf.setReleases(userReleases);
        FragmentTransaction replace = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, urf, UserReleasesFragment.class.getSimpleName());
        if (addToBackStack) replace.addToBackStack(null);
        replace.commit();
        setFABModeAdd();
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
        setFABModeInvisible();
    }

    public void changeToDetailReleaseView(Release release)
    {
        ReleaseDetailFragment releaseDetailFragment = new ReleaseDetailFragment();

        Release addRelease = release;
        if (userReleases.contains(release))
            addRelease = userReleases.get(userReleases.indexOf(release));
        releaseDetailFragment.setRelease(addRelease);


        if (getCurrentView().equals(SearchReleasesFragment.class.getSimpleName()))
            new DiscogsGetReleaseDetailsTask(releaseDetailFragment).execute(release.discogsId);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, releaseDetailFragment, ReleaseDetailFragment.class.getSimpleName())
                .addToBackStack(null)
                .commit();

        if (userReleases.contains(release))
        {
            setFABModeEdit();
        } else
        {
            setFABModeAdd();
        }
    }

    public void changeToDetailArtistView(Artist artist)
    {
        ArtistDetailFragment adf = new ArtistDetailFragment();

        Artist testArtist = new Artist();
        testArtist.name = "TEST";
        testArtist.imgUrl = "";
        ArtistDetailFragment testFragment = new ArtistDetailFragment();
        testFragment.setArtist(testArtist);

        Artist addArtist = artist;
        // TODO: look at changeToDetailReleaseView
        adf.setArtist(addArtist);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content_area, adf, ArtistDetailFragment.class.getSimpleName())
                .add(    R.id.home_content_area, testFragment, ArtistDetailFragment.class.getSimpleName())
                .addToBackStack(null)
                .commit();
    }

    public String getCurrentView()
    {
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.home_content_area);
        String name = f != null ? f.getClass().getSimpleName() : "";
        return name;
    }

    public List<Release> getUserReleases()
    {
        return userReleases;
    }

    public void setFABModeAdd()
    {
        fab.setImageResource(R.mipmap.plus);
        fab.show();
    }

    public void setFABModeEdit()
    {
        fab.setImageResource(R.drawable.ic_menu_gallery);
        fab.show();
    }

    public void setFABModeInvisible()
    {
        fab.hide();
    }

    public void disableSearch() { if (searchItem != null) searchItem.setVisible(false); }
    public void enableSearch() { if (searchItem != null) searchItem.setVisible(true); }

    public void disableDelete() { if (deleteItem!= null) deleteItem.setVisible(false); }
    public void enableDelete() { if (deleteItem != null) deleteItem.setVisible(true); }
}
