package com.ptato.aseeblabla;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptato.aseeblabla.db.Release;

public class ReleaseDetailFragment extends Fragment
{
    private Release release = null;
    private EditText starsEdit = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.release_detail, container, false);

        TextView titleView = rootView.findViewById(R.id.detail_release_title);
        TextView artistView = rootView.findViewById(R.id.detail_release_artist);
        TextView yearView = rootView.findViewById(R.id.detail_release_year);
        ImageView coverView = rootView.findViewById(R.id.detail_release_thumb);

        starsEdit = rootView.findViewById(R.id.detail_estrellas_number);

        if (release != null)
        {
            titleView.setText(release.title);
            artistView.setText(release.artist);
            yearView.setText(release.year);
            new DownloadImageTask(coverView).execute(release.thumbUrl);
            starsEdit.setText(Integer.toString(release.stars));
        }

        return rootView;
    }

    public void setRelease(Release r)
    {
        release = r;
    }
    public Release getRelease()
    {
        try { release.stars = Integer.parseInt(starsEdit.getText().toString()); }
        catch (NumberFormatException e) {
            Log.i(this.getClass().getSimpleName(), "Number format failed");
            release.stars = 0;
        }
        return release;
    }
}
