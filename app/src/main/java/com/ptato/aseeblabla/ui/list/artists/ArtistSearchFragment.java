package com.ptato.aseeblabla.ui.list.artists;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Artist;
import com.ptato.aseeblabla.ui.list.ListViewModelFactory;
import com.ptato.aseeblabla.utilities.DownloadImageTask;

import java.util.ArrayList;
import java.util.List;

public class ArtistSearchFragment extends Fragment
{
    private RecyclerView recyclerView;
    private OnClickArtistListener itemOnClickListener;

    private TextView emptyTextView;
    private ArtistSearchViewModel viewModel;

    public interface OnClickArtistListener
    {
        void onClick(Artist r);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.releases_list_view, container, false);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = getActivity();
        if (activity != null)
        {
            Repository repository = Repository.getInstance(activity);
            ListViewModelFactory factory = new ListViewModelFactory(repository);
            viewModel = ViewModelProviders.of(this, factory).get(ArtistSearchViewModel.class);

            viewModel.getArtists().observe(this, new Observer<List<Artist>>()
            {
                @Override
                public void onChanged(@Nullable List<Artist> artists)
                {
                    updateUi(artists);
                }
            });
        }
    }

    public void setItemOnClickListener(OnClickArtistListener listener)
    {
        itemOnClickListener = listener;
    }

    private void updateUi(List<Artist> artists)
    {
        ArtistsFragmentAdapter afa = (ArtistsFragmentAdapter) recyclerView.getAdapter();
        if (afa != null)
        {
            afa.artists = artists;
            afa.notifyDataSetChanged();
            if (artists.size() > 0)
                emptyTextView.setVisibility(View.INVISIBLE);
            else
                emptyTextView.setVisibility(View.VISIBLE);
        }

        if (artists.size() == 0)
        {
            emptyTextView.setText(R.string.results_not_found);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setSearchQuery(String query)
    {
        viewModel.setSearchQuery(query);
    }

    public void clearSearchQuery()
    {
        viewModel.setSearchQuery(null);
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
                    .inflate(R.layout.artist_entry, viewGroup, false);
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

    public int getArtistCount()
    {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        return adapter == null ? 0 : adapter.getItemCount();
    }
}
