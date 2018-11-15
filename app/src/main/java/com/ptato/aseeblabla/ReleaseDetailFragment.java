package com.ptato.aseeblabla;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.BundleCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ReleaseDetailFragment extends Fragment
{
    private Release release = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.release_detail, container, false);

        TextView titleView = rootView.findViewById(R.id.detail_release_title);
        TextView artistView = rootView.findViewById(R.id.detail_release_artist);
        TextView yearView = rootView.findViewById(R.id.detail_release_year);
        ImageView coverView = rootView.findViewById(R.id.detail_release_thumb);

        if (release != null)
        {
            titleView.setText(release.title);
            artistView.setText(release.artist);
            yearView.setText(release.year);
            new DownloadImageTask(coverView).execute(release.thumbUrl);
        }

        return rootView;
    }

    public void setRelease(Release r)
    {
        release = r;
    }
    public Release getRelease() { return release; }
}
