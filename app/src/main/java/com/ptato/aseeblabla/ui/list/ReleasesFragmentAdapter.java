package com.ptato.aseeblabla.ui.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptato.aseeblabla.R;
import com.ptato.aseeblabla.data.db.Release;
import com.ptato.aseeblabla.utilities.DownloadImageTask;

import java.util.ArrayList;
import java.util.List;

public class ReleasesFragmentAdapter extends RecyclerView.Adapter<ReleasesFragmentAdapter.ViewHolder>
{

    public interface OnClickReleaseListener
    {
        void onClick(Release r);
    }

    public List<Release> releases;
    private OnClickReleaseListener adapterOnClick;

    public ReleasesFragmentAdapter(List<Release> _releases, OnClickReleaseListener listener)
    {
        if (_releases == null)
            this.releases = new ArrayList<>();
        else
            this.releases = _releases;
        adapterOnClick = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View releaseLayoutView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.release_entry, viewGroup, false);


        ViewHolder viewHolder = new ViewHolder(releaseLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder releaseViewHolder, int i)
    {
        releaseViewHolder.title.setText(releases.get(i).title);
        releaseViewHolder.artist.setText(releases.get(i).artist);
        releaseViewHolder.year.setText(releases.get(i).year);

        releaseViewHolder.thumb.setImageResource(android.R.color.transparent);
        new DownloadImageTask(releaseViewHolder.thumb).execute(releases.get(i).thumbUrl);

        int starIndex = 0;
        for (ImageView star : releaseViewHolder.stars)
        {
            star.setVisibility(starIndex < releases.get(i).stars ? View.VISIBLE : View.INVISIBLE);
        }

        releaseViewHolder.bindListener(releases.get(i), adapterOnClick);
    }

    @Override
    public int getItemCount()
    {
        return releases.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView title;
        public TextView artist;
        public TextView year;
        public ImageView thumb;
        public List<ImageView> stars;
        public View restOfReleaseView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            title = itemView.findViewById(R.id.release_title_view);
            artist = itemView.findViewById(R.id.release_artist_view);
            year = itemView.findViewById(R.id.release_year_view);
            thumb = itemView.findViewById(R.id.cover_preview);
            stars = new ArrayList<>();
            stars.add((ImageView)itemView.findViewById(R.id.release_rating_star1));
            stars.add((ImageView)itemView.findViewById(R.id.release_rating_star2));
            stars.add((ImageView)itemView.findViewById(R.id.release_rating_star3));
            stars.add((ImageView)itemView.findViewById(R.id.release_rating_star4));
            stars.add((ImageView)itemView.findViewById(R.id.release_rating_star5));
            restOfReleaseView = itemView.findViewById(R.id.rest_of_release_view);
        }

        public void bindListener(final Release r, final OnClickReleaseListener listener)
        {
            View.OnClickListener onClickListener = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    listener.onClick(r);
                }
            };
            thumb.setOnClickListener(onClickListener);
            restOfReleaseView.setOnClickListener(onClickListener);
        }
    }
}
