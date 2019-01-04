package com.ptato.aseeblabla.ui.detail.artist;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import com.ptato.aseeblabla.ui.AppViewModelFactory;
import com.ptato.aseeblabla.utilities.DownloadImageTask;
import com.ptato.aseeblabla.R;
import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Artist;

public class ArtistDetailActivity extends AppCompatActivity
{
    public static final String ARTIST_ID_EXTRA = "ARTIST_ID_EXTRA";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        int artistId = getIntent().getIntExtra(ARTIST_ID_EXTRA, -1);
        Repository repository = Repository.getInstance(this);
        AppViewModelFactory factory = new AppViewModelFactory(repository, artistId);
        ArtistDetailViewModel viewModel = ViewModelProviders.of(this, factory).get(ArtistDetailViewModel.class);
        viewModel.getArtist().observe(this, new Observer<Artist>()
        {
            @Override
            public void onChanged(@Nullable Artist artist)
            {
                bindArtistToUi(artist);
            }
        });

        Button viewReleases = findViewById(R.id.detail_artist_view_releases);
        viewReleases.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /*
                SearchReleasesFragment srf = a.changeToSearchReleaseView();
                new HomeActivity.DiscogsGetArtistReleasesTask(a, srf).execute(artist.discogsId);
                */
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void bindArtistToUi(Artist artist)
    {
        if (artist != null && artist.discogsId != -1)
        {
            ImageView artistImage = findViewById(R.id.detail_artist_image);
            TextView artistName = findViewById(R.id.detail_artist_name);
            TextView artistProfile = findViewById(R.id.detail_artist_profile);
            TextView artistUrl = findViewById(R.id.detail_artist_url);

            artistName.setText(artist.name);
            artistProfile.setText(artist.profile);
            new DownloadImageTask(artistImage).execute(artist.imgUrl);
            if (artist.url != null)
                artistUrl.setText(artist.url);
            else
                artistUrl.setVisibility(View.GONE);
        }
    }
}
