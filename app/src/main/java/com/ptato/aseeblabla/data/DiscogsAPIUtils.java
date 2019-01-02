package com.ptato.aseeblabla.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.ptato.aseeblabla.db.Artist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscogsAPIUtils
{
    private static final String baseURL = "https://api.discogs.com/";
    private static final String authToken = "GRkDMwQSYOBKnnWIOeJTokxkkViZQIrqcLzJilow";


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

            liveData.setValue(a);
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
