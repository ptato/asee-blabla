package com.ptato.aseeblabla;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptato.aseeblabla.db.Release;

import java.util.ArrayList;
import java.util.List;

public class ReleasesFragment extends Fragment
{
    private RecyclerView recyclerView;
    private OnClickReleaseListener itemOnClickListener;
    private List<Release> persistReleases = null;

    private boolean isSearchingValue;

    private TextView emptyTextView;

    public interface OnClickReleaseListener
    {
        void onClick(Release r);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.releases_list_view, container, false);
        recyclerView = rootView.findViewById(R.id.releases_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<Release> userReleases = ((HomeActivity)getActivity()).getUserReleases();
        List<Release> whichReleases = null;
        if (isSearchingValue)
            whichReleases = persistReleases;
        else
            whichReleases = userReleases;


        recyclerView.setAdapter(new ReleasesFragmentAdapter(whichReleases, itemOnClickListener));


        emptyTextView = rootView.findViewById(R.id.releases_empty_recycler);
        emptyTextView.setVisibility(
                whichReleases != null && whichReleases.size() > 0 ? View.INVISIBLE : View.VISIBLE);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        return rootView;
    }


    public void setItemOnClickListener(OnClickReleaseListener listener)
    {
        itemOnClickListener = listener;
    }

    public void setReleases(List<Release> releases)
    {
        setReleases(releases, false);
    }

    public void setReleases(List<Release> releases, boolean search)
    {
        persistReleases = releases;

        ReleasesFragmentAdapter rfa = (ReleasesFragmentAdapter)recyclerView.getAdapter();
        if (rfa != null)
        {
            rfa.releases = releases;
            rfa.notifyDataSetChanged();
            if (releases.size() > 0)
                emptyTextView.setVisibility(View.INVISIBLE);
            else
                emptyTextView.setVisibility(View.VISIBLE);
        }

        isSearchingValue = search;
        if (isSearchingValue && releases.size() == 0)
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
            new DownloadImageTask(releaseViewHolder.thumb).execute(releases.get(i).thumbUrl);
            for (ImageView star : releaseViewHolder.stars)
                star.setVisibility(View.INVISIBLE);

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

            private View.OnClickListener listener;

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

    public boolean isSearching()
    {
        return isSearchingValue;
    }
}
