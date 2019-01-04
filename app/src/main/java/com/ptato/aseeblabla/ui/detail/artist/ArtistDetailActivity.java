package com.ptato.aseeblabla.ui.detail.artist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptato.aseeblabla.R;
import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.Release;
import com.ptato.aseeblabla.ui.AppViewModelFactory;
import com.ptato.aseeblabla.ui.detail.release.ReleaseDetailActivity;
import com.ptato.aseeblabla.ui.list.ShowReleasesFragment;
import com.ptato.aseeblabla.utilities.DownloadImageTask;

import java.util.List;

public class ArtistDetailActivity extends AppCompatActivity
{
    public static final String ARTIST_ID_EXTRA = "ARTIST_ID_EXTRA";

    public static class ShowArtistReleasesFragment extends ShowReleasesFragment
    {
        @Override
        protected LiveData<List<Release>> getShownReleases()
        {
            FragmentActivity activity = getActivity();
            if (activity != null)
            {
                Repository repository = Repository.getInstance(activity);
                AppViewModelFactory factory = new AppViewModelFactory(repository);
                ArtistDetailViewModel viewModel = ViewModelProviders.of(activity, factory).get(ArtistDetailViewModel.class);
                return viewModel.getArtistReleases();
            }

            return new MutableLiveData<>();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_artist);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        int artistId = getIntent().getIntExtra(ARTIST_ID_EXTRA, -1);
        Repository repository = Repository.getInstance(this);
        AppViewModelFactory factory = new AppViewModelFactory(repository, artistId);
        final ArtistDetailViewModel viewModel = ViewModelProviders.of(this, factory).get(ArtistDetailViewModel.class);
        viewModel.getArtist().observe(this, new Observer<Artist>()
        {
            @Override
            public void onChanged(@Nullable Artist artist)
            {
                bindArtistToUi(artist);
            }
        });

        final Button viewReleases = findViewById(R.id.detail_artist_view_releases);
        viewReleases.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ShowReleasesFragment showReleasesFragment = new ShowArtistReleasesFragment();
                showReleasesFragment.setItemOnClickListener(new ShowReleasesFragment.OnClickReleaseListener()
                {
                    @Override
                    public void onClick(Release r)
                    {
                        Intent intent = new Intent(ArtistDetailActivity.this, ReleaseDetailActivity.class);
                        intent.putExtra(ReleaseDetailActivity.RELEASE_ID_EXTRA, r.discogsId);
                        ArtistDetailActivity.this.startActivity(intent);
                    }
                });
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.artist_detail_content, showReleasesFragment, ShowReleasesFragment.class.getSimpleName())
                        .addToBackStack(null)
                        .commit();
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
