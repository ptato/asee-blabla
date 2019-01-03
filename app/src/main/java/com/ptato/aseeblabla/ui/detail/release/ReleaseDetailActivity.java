package com.ptato.aseeblabla.ui.detail.release;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import com.ptato.aseeblabla.data.db.Release;
import com.ptato.aseeblabla.ui.detail.DetailViewModelFactory;
import com.ptato.aseeblabla.ui.detail.artist.ArtistDetailActivity;

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

            Button saveButton = findViewById(R.id.release_save_button);
            Button deleteButton = findViewById(R.id.release_delete_button);

            if(viewModel.isReleaseSavedByUser())
            {
                saveButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        release.stars = Integer.parseInt(starsEdit.getText().toString());
                        release.review = reviewEdit.getText().toString();
                        repository.insertOrUpdateRelease(release);
                        Snackbar.make(v, release.title + " ha sido editado", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                deleteButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        new AlertDialog.Builder(ReleaseDetailActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Borrar")
                                .setMessage("¿De verdad quieres borrar '" + release.title + "'?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        repository.deleteRelease(release);
                                        finish();
                                    }
                                }).setNegativeButton("No", null)
                                .show();
                    }
                });
            } else
            {
                Snackbar.make(saveButton, "Luego lo arreglo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }
}
