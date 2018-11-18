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
    private EditText reviewEdit = null;
    private View rootView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.release_detail, container, false);

        updateView();

        return rootView;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        HomeActivity a = (HomeActivity)getActivity();
        a.enableDelete();
        a.disableSearch();
    }

    private void updateView()
    {
        if (rootView != null)
        {
            TextView titleView = rootView.findViewById(R.id.detail_release_title);
            TextView artistView = rootView.findViewById(R.id.detail_release_artist);
            TextView yearView = rootView.findViewById(R.id.detail_release_year);
            ImageView coverView = rootView.findViewById(R.id.detail_release_thumb);
            TextView countryView = rootView.findViewById(R.id.release_detail_country);
            TextView genresView = rootView.findViewById(R.id.release_detail_genres);
            reviewEdit = rootView.findViewById(R.id.release_detail_review);
            starsEdit = rootView.findViewById(R.id.detail_estrellas_number);

            if (release != null)
            {
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
            }
        }
    }

    public void setRelease(Release r)
    {
        release = r;
        updateView();
    }
    public Release getRelease()
    {
        try {
            release.stars = Integer.parseInt(starsEdit.getText().toString());
            if (release.stars > 5) release.stars = 5;
        } catch (NumberFormatException e) {
            Log.i(this.getClass().getSimpleName(), "Number format failed");
            release.stars = 0;
        }

        release.review = reviewEdit.getText().toString();
        return release;
    }
}
