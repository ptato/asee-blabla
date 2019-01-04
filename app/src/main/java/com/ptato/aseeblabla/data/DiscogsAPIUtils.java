package com.ptato.aseeblabla.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.util.Log;

import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.Release;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DiscogsAPIUtils
{
    private static final String baseURL = "https://api.discogs.com/";
    private static final String authToken = "GRkDMwQSYOBKnnWIOeJTokxkkViZQIrqcLzJilow";

    public static LiveData<Release> getRelease(int id)
    {
        MutableLiveData<Release> releaseMutableLiveData = new MutableLiveData<>();
        Release release = new Release();
        release.discogsId = -1;
        releaseMutableLiveData.setValue(release);

        if (id != -1)
            new DiscogsGetReleaseDetailsTask(releaseMutableLiveData).execute(id);

        return releaseMutableLiveData;
    }

    public static LiveData<Artist> getArtist(int id)
    {
        MutableLiveData<Artist> artistMutableLiveData = new MutableLiveData<>();
        Artist artist = new Artist();
        artist.discogsId = -1;
        artistMutableLiveData.setValue(artist);

        if (id != -1)
            new DiscogsGetArtistDetailsTask(artistMutableLiveData).execute(id);

        return artistMutableLiveData;
    }

    public static LiveData<List<Release>> searchReleases(String query)
    {
        MutableLiveData<List<Release>> liveData = new MutableLiveData<>();
        List<Release> list = new ArrayList<>();
        liveData.setValue(list);

        if (query != null)
        {
            // noinspection unchecked
            new DiscogsSearchQueryTask(liveData, null).execute(
                    Pair.create(DiscogsSearchQueryTask.TYPE, DiscogsSearchQueryTask.TYPE_RELEASE),
                    Pair.create(DiscogsSearchQueryTask.COMBINED_TITLE, query)
            );
        }

        return liveData;
    }

    public static LiveData<List<Artist>> searchArtists(String query)
    {
        MutableLiveData<List<Artist>> liveData = new MutableLiveData<>();
        List<Artist> list = new ArrayList<>();
        liveData.setValue(list);

        if (query != null)
        {
            // noinspection unchecked
            new DiscogsSearchQueryTask(null, liveData).execute(
                    Pair.create(DiscogsSearchQueryTask.TYPE, DiscogsSearchQueryTask.TYPE_ARTIST),
                    Pair.create(DiscogsSearchQueryTask.COMBINED_TITLE, query));
        }

        return liveData;
    }

    private static class DiscogsGetArtistDetailsTask extends AsyncTask<Integer, Void, JSONObject>
    {
        private MutableLiveData<Artist> liveData;

        public DiscogsGetArtistDetailsTask(MutableLiveData<Artist> ld)
        {
            liveData = ld;
        }

        @Override
        protected JSONObject doInBackground(Integer... integers)
        {
            JSONObject jsonResult = null;

            if (integers.length > 0)
            {
                StringBuilder queryURLBuilder = new StringBuilder(baseURL + "artists/" + integers[0].toString());
                Log.i(this.getClass().getSimpleName(), queryURLBuilder.toString());
                jsonResult = getResponseFromDiscogs(queryURLBuilder.toString());
            }

            return jsonResult;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject)
        {
            super.onPostExecute(jsonObject);

            Artist a = new Artist();

            a.discogsId = jsonObject.optInt("id", -1);
            a.name = jsonObject.optString("name", null);
            if (a.name == null)
            {
                // If discogs doesn't return a name directly, try the alternative names array.
                JSONArray nameVariations = jsonObject.optJSONArray("namevariations");
                a.name = nameVariations == null ? "Unknown Name" : nameVariations.optString(0);
            }

            JSONArray images = jsonObject.optJSONArray("images");
            // I think biggest resolution image is the last, so we take it
            JSONObject image0 = images == null ? null :
                    images.optJSONObject(images.length() - 1);
            a.imgUrl = image0 == null ? "" :
                    image0.optString("uri", "");
            a.profile = jsonObject.optString("profile", "");
            JSONArray urls = jsonObject.optJSONArray("urls");
            a.url = urls == null ? null : urls.optString(0, null);

            liveData.setValue(a);
        }
    }

    private static class DiscogsGetReleaseDetailsTask extends AsyncTask<Integer, Void, JSONObject>
    {
        private MutableLiveData<Release> liveData;

        public DiscogsGetReleaseDetailsTask(MutableLiveData<Release> ld)
        {
            liveData = ld;
        }

        @Override
        protected JSONObject doInBackground(Integer... integers)
        {
            JSONObject jsonResult = null;

            if (integers.length > 0)
            {
                StringBuilder queryURLBuilder = new StringBuilder(baseURL + "releases/" + integers[0].toString());
                Log.i(this.getClass().getName(), queryURLBuilder.toString());
                jsonResult = DiscogsAPIUtils.getResponseFromDiscogs(queryURLBuilder.toString());
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
            StringBuilder builder = new StringBuilder();
            if (genres != null)
                for (int i = 0; i < genres.length(); ++i)
                    builder.append(genres.optString(i, "unknown")).append(", ");
            if (styles != null)
                for (int i = 0; i < styles.length(); ++i)
                    builder.append(styles.optString(i, "unknown")).append(", ");
            r.genres = builder.toString().substring(0, builder.toString().length() - 2);

            liveData.setValue(r);
        }
    }

    @SuppressWarnings("unused")
    private static class DiscogsSearchQueryTask extends AsyncTask<Pair<String, String>, Void, JSONObject>
    {
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

        private boolean isArtists;
        private MutableLiveData<List<Release>> outputReleases;
        private MutableLiveData<List<Artist>>  outputArtists;

        public DiscogsSearchQueryTask(MutableLiveData<List<Release>> releases, MutableLiveData<List<Artist>> artists)
        {
            outputReleases = releases;
            outputArtists = artists;
        }

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

            isArtists = true;
            for (Pair<String, String> param : params)
            {
                if (Objects.equals(param.first, TYPE))
                {
                    isArtists = Objects.equals(param.second, TYPE_ARTIST);
                }
            }

            return jsonResult;
        }

        @Override
        protected void onPostExecute(JSONObject json)
        {
            super.onPostExecute(json);

            try
            {
                if (json != null && json.has("results"))
                {
                    Log.i(this.getClass().getSimpleName(), json.toString(2));
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

                        outputArtists.setValue(artists);

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

                        outputReleases.setValue(releases);
                    }
                }
            } catch (JSONException e)
            {
                Log.e(DiscogsSearchQueryTask.class.getName(), "Error al parsear JSON");
                Log.e(DiscogsSearchQueryTask.class.getName(), e.getMessage());
            }
        }
    }

    public static JSONObject getResponseFromDiscogs(String url)
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
                Log.e(DiscogsAPIUtils.class.getSimpleName(),
                        "Codigo de respuesta de error: " + urlConnection.getResponseCode());
            }

            String stringResponse = stringBuilder.toString();
            jsonResult = new JSONObject(stringResponse);

            Log.i(DiscogsAPIUtils.class.getSimpleName(), jsonResult.toString(4));

        } catch (IOException|JSONException e)
        {
            Log.e(DiscogsAPIUtils.class.getSimpleName(), e.getClass().getSimpleName());
            Log.e(DiscogsAPIUtils.class.getSimpleName(), e.getMessage());
        }


        return jsonResult;
    }
}
