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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptato.aseeblabla.R;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.utilities.DownloadImageTask;

import java.util.ArrayList;
import java.util.List;

public abstract class ShowArtistsFragment extends Fragment
{
    private OnClickArtistListener itemOnClickListener;

    private RecyclerView recyclerView;
    private TextView emptyTextView;

    public interface OnClickArtistListener
    {
        void onClick(Artist r);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.general_list_content, container, false);
        recyclerView = rootView.findViewById(R.id.releases_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setAdapter(new ArtistsFragmentAdapter(itemOnClickListener));

        emptyTextView = rootView.findViewById(R.id.releases_empty_recycler);
        emptyTextView.setText("No se han encontrado artistas");
        emptyTextView.setVisibility(View.VISIBLE);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        return rootView;
    }

    public void setItemOnClickListener(OnClickArtistListener listener)
    {
        itemOnClickListener = listener;
    }

    public abstract LiveData<List<Artist>> getShownArtists();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getShownArtists().observe(this, new Observer<List<Artist>>()
        {
            @Override
            public void onChanged(@Nullable List<Artist> releases)
            {
                updateUi(releases);
            }
        });
    }

    private void updateUi(@Nullable List<Artist> artists)
    {
        ArtistsFragmentAdapter afa = (ArtistsFragmentAdapter) recyclerView.getAdapter();
        if (artists != null && afa != null)
        {
            afa.artists = artists;
            afa.notifyDataSetChanged();
            if (artists.size() > 0)
                emptyTextView.setVisibility(View.INVISIBLE);
            else
                emptyTextView.setVisibility(View.VISIBLE);
        }

        if (artists == null || artists.size() == 0)
        {
            emptyTextView.setText(R.string.results_not_found);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    private class ArtistsFragmentAdapter extends RecyclerView.Adapter<ArtistsFragmentAdapter.ViewHolder>
    {
        public List<Artist> artists;
        private OnClickArtistListener adapterOnClick;

        public ArtistsFragmentAdapter(OnClickArtistListener listener)
        {
            artists = new ArrayList<>();
            adapterOnClick = listener;
        }

        @NonNull
        @Override
        public ArtistsFragmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
        {
            View artistLayoutView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.artist_list_entry, viewGroup, false);
            return new ViewHolder(artistLayoutView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder artistViewHolder, int i)
        {
            artistViewHolder.name.setText(artists.get(i).name);
            new DownloadImageTask(artistViewHolder.thumb).execute(artists.get(i).imgUrl);
            artistViewHolder.bindListener(artists.get(i), adapterOnClick);
        }

        @Override
        public int getItemCount()
        {
            return artists.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public TextView name;
            public ImageView thumb;
            public View restOfArtistView;

            private View.OnClickListener listener;

            public ViewHolder(View itemView)
            {
                super(itemView);
                name = itemView.findViewById(R.id.artist_name_view);
                thumb = itemView.findViewById(R.id.artist_image_preview);
                restOfArtistView = itemView.findViewById(R.id.rest_of_artist_view);
            }

            public void bindListener(final Artist a, final OnClickArtistListener listener)
            {
                View.OnClickListener onClickListener = new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        listener.onClick(a);
                    }
                };
                thumb.setOnClickListener(onClickListener);
                restOfArtistView.setOnClickListener(onClickListener);
            }
        }
    }
}
