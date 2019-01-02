package com.ptato.aseeblabla.ui.detail.release;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptato.aseeblabla.DownloadImageTask;
import com.ptato.aseeblabla.R;
import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.data.db.Release;
import com.ptato.aseeblabla.ui.detail.DetailViewModelFactory;
import com.ptato.aseeblabla.ui.detail.artist.ArtistDetailActivity;
import com.ptato.aseeblabla.ui.detail.artist.ArtistDetailViewModel;

public class ReleaseDetailActivity extends AppCompatActivity
{
    public static final String RELEASE_ID_EXTRA = "RELEASE_ID_EXTRA";
    private ReleaseDetailViewModel viewModel;
    private Repository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.release_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        int releaseId = getIntent().getIntExtra(RELEASE_ID_EXTRA, -1);
        repository = Repository.getInstance(this);
        DetailViewModelFactory factory = new DetailViewModelFactory(repository, releaseId);
        viewModel = ViewModelProviders.of(this, factory).get(ReleaseDetailViewModel.class);
        viewModel.getRelease().observe(this, new Observer<Release>()
        {
            @Override
            public void onChanged(@Nullable Release release)
            {
                bindReleaseToUi(release);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void bindReleaseToUi(final Release release)
    {
        if (release != null && release.discogsId != -1)
        {
            TextView titleView = findViewById(R.id.detail_release_title);
            TextView artistView = findViewById(R.id.detail_release_artist);
            TextView yearView = findViewById(R.id.detail_release_year);
            ImageView coverView = findViewById(R.id.detail_release_thumb);
            TextView countryView = findViewById(R.id.release_detail_country);
            TextView genresView = findViewById(R.id.release_detail_genres);
            final EditText starsEdit = findViewById(R.id.detail_estrellas_number);
            final EditText reviewEdit = findViewById(R.id.release_detail_review);
            titleView.setText(release.title);
            artistView.setText(release.artist);
            yearView.setText(release.year);

            String myImageUrl = release.imageUrl == null || release.imageUrl.equals("")
                    ? release.thumbUrl : release.imageUrl;
            new DownloadImageTask(coverView).execute(myImageUrl);

            starsEdit.setText(Integer.toString(release.stars));

            countryView.setText(release.country);
            genresView.setText(release.genres);

            reviewEdit.setText(release.review);


            artistView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ReleaseDetailActivity.this, ArtistDetailActivity.class);
                    intent.putExtra(ArtistDetailActivity.ARTIST_ID_EXTRA, release.artistId);
                    startActivity(intent);
                }
            });


            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Release release = viewModel.getRelease().getValue();
                    if (release != null && release.discogsId != -1)
                    {
                        release.stars = Integer.parseInt(starsEdit.getText().toString());
                        release.review = reviewEdit.getText().toString();
                        repository.insertOrUpdateRelease(release);
                        Snackbar.make(v, "Hecho", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        ((FloatingActionButton)v).setImageResource(R.drawable.ic_menu_gallery);
                    }
                }
            });
            if (viewModel.isReleaseSavedByUser())
            {
                fab.setImageResource(R.drawable.ic_menu_gallery);
            } else
            {
                fab.setImageResource(R.mipmap.plus);
            }
        }
    }
}
