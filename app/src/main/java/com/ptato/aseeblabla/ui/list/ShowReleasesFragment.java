package com.ptato.aseeblabla.ui.list;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public abstract class ShowReleasesFragment extends Fragment
{
    public interface OnClickReleaseListener
    {
        void onClick(Release r);
    }

    private RecyclerView recyclerView;
    private OnClickReleaseListener itemOnClickListener;
    private TextView emptyTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.general_list_content, container, false);
        recyclerView = rootView.findViewById(R.id.releases_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setAdapter(new ReleasesFragmentAdapter(new ArrayList<Release>(), itemOnClickListener));
        emptyTextView = rootView.findViewById(R.id.releases_empty_recycler);
        emptyTextView.setVisibility(View.VISIBLE);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        Log.i(this.getClass().getSimpleName(), "OnCreateView");

        return rootView;
    }

    public void setItemOnClickListener(OnClickReleaseListener listener)
    {
        itemOnClickListener = listener;
    }

    protected abstract LiveData<List<Release>> getShownReleases();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getShownReleases().observe(this, new Observer<List<Release>>()
        {
            @Override
            public void onChanged(@Nullable List<Release> releases)
            {
                updateUi(releases);
            }
        });
    }

    private void updateUi(@Nullable List<Release> releases)
    {
        ReleasesFragmentAdapter rfa = recyclerView == null ?
                null : (ReleasesFragmentAdapter)recyclerView.getAdapter();
        if (releases != null && rfa != null)
        {
            rfa.releases = releases;
            rfa.notifyDataSetChanged();
            if (releases.size() > 0)
                emptyTextView.setVisibility(View.INVISIBLE);
            else
                emptyTextView.setVisibility(View.VISIBLE);
        }

        if (releases == null || releases.size() == 0)
        {
            emptyTextView.setText(R.string.results_not_found);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    private class ReleasesFragmentAdapter extends RecyclerView.Adapter<ReleasesFragmentAdapter.ViewHolder>
    {
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
                    .inflate(R.layout.release_list_entry, viewGroup, false);


            return new ViewHolder(releaseLayoutView);
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
}
