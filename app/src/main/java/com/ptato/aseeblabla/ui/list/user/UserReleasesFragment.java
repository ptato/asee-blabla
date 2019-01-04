package com.ptato.aseeblabla.ui.list.user;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.TextView;

import com.ptato.aseeblabla.ui.list.HomeActivity;
import com.ptato.aseeblabla.R;
import com.ptato.aseeblabla.ui.list.ReleasesFragmentAdapter;
import com.ptato.aseeblabla.data.Repository;
import com.ptato.aseeblabla.data.db.Release;
import com.ptato.aseeblabla.ui.list.ListViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class UserReleasesFragment extends Fragment
{
    private RecyclerView recyclerView = null;
    private ReleasesFragmentAdapter.OnClickReleaseListener itemOnClickListener = null;
    private TextView emptyTextView = null;
    private boolean isUsingSearchResultsValue;

    private UserReleasesViewModel viewModel = null;
    private String searchQuery = null;

    public void setItemOnClickListener(ReleasesFragmentAdapter.OnClickReleaseListener listener)
    {
        itemOnClickListener = listener;
    }

    public void setSearchQuery(String query)
    {
        searchQuery = query;
        if (viewModel != null)
            updateUi(viewModel.getUserReleases().getValue());
        isUsingSearchResultsValue = true;
    }

    public void clearSearchQuery()
    {
        searchQuery = null;
        if (viewModel != null)
            updateUi(viewModel.getUserReleases().getValue());
        isUsingSearchResultsValue = false;
    }

    private void updateUi(List<Release> newReleases)
    {
        List<Release> processedReleases = new ArrayList<>();
        Log.i(this.getClass().getSimpleName(), "Updating Adapter");
        if (searchQuery != null)
        {
            Log.i(this.getClass().getSimpleName(), "Search Query: " + (searchQuery==null?"'NULL'":searchQuery));
            String lowerQuery = searchQuery.toLowerCase();
            for (Release r: newReleases)
            {
                if(r.title.toLowerCase().contains(lowerQuery) || r.artist.contains(lowerQuery))
                {
                    processedReleases.add(r);
                }
            }
        } else
        {
            processedReleases = newReleases;
        }

        ReleasesFragmentAdapter rfa = recyclerView == null ?
                null : (ReleasesFragmentAdapter)recyclerView.getAdapter();
        if (rfa != null)
        {
            rfa.releases = processedReleases;
            rfa.notifyDataSetChanged();
            if (newReleases.size() > 0)
                emptyTextView.setVisibility(View.INVISIBLE);
            else
                emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.releases_list_view, container, false);

        recyclerView = rootView.findViewById(R.id.releases_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setAdapter(new ReleasesFragmentAdapter(null, itemOnClickListener));
        searchQuery = null;
        isUsingSearchResultsValue = false;

        emptyTextView = rootView.findViewById(R.id.releases_empty_recycler);
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
        HomeActivity activity = (HomeActivity)getActivity();
        if (activity != null)
        {
            Repository repository = Repository.getInstance(activity);
            ListViewModelFactory factory = new ListViewModelFactory(repository);
            viewModel = ViewModelProviders.of(this, factory).get(UserReleasesViewModel.class);

            viewModel.getUserReleases().observe(this, new Observer<List<Release>>()
            {
                @Override
                public void onChanged(@Nullable List<Release> releases)
                {
                    updateUi(releases);
                }
            });
        }
    }

    public boolean isUsingSearchResults() { return isUsingSearchResultsValue; }
}
