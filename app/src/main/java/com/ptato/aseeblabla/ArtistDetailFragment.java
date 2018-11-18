package com.ptato.aseeblabla;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptato.aseeblabla.db.Artist;

public class ArtistDetailFragment extends Fragment
{
    private Artist artist;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.artist_detail, container, false);

        updateView();



        return rootView;
    }

    private void updateView()
    {
        if (rootView != null)
        {
            ImageView artistImage = rootView.findViewById(R.id.detail_artist_image);
            TextView artistName = rootView.findViewById(R.id.detail_artist_name);
            TextView artistProfile = rootView.findViewById(R.id.detail_artist_profile);

            if (artist != null)
            {
                artistName.setText(artist.name);
                artistProfile.setText(artist.profile);
                new DownloadImageTask(artistImage).execute(artist.imgUrl);
            }
        }

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        final HomeActivity a = (HomeActivity)getActivity();
        if (a != null)
        {
            a.disableDelete();
            a.disableSearch();
        }

        Button viewReleases = rootView.findViewById(R.id.detail_artist_view_releases);
        viewReleases.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SearchReleasesFragment srf = a.changeToSearchReleaseView();
                new HomeActivity.DiscogsGetArtistReleasesTask(a, srf).execute();
            }
        });
    }

    public void setArtist(Artist a) { artist = a; updateView(); }
    public Artist getArtist()
    {
        return artist;
    }
}
