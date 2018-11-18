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
import android.widget.TextView;

import com.ptato.aseeblabla.db.Release;

import java.util.ArrayList;
import java.util.List;

public class UserReleasesFragment extends Fragment
{
    private RecyclerView recyclerView = null;
    private List<Release> persistReleases;
    private ReleasesFragmentAdapter.OnClickReleaseListener itemOnClickListener = null;
    private TextView emptyTextView = null;
    private boolean isUsingSearchResultsValue;

    public void setItemOnClickListener(ReleasesFragmentAdapter.OnClickReleaseListener listener)
    {
        itemOnClickListener = listener;
    }

    public void setReleases(List<Release> releases)
    {
        persistReleases = releases;
        updateAdapter(releases);
    }

    public void setSearchQuery(String query)
    {
        List<Release> searchResultReleases = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Release r: persistReleases)
        {
            if(r.title.toLowerCase().contains(lowerQuery) || r.artist.contains(lowerQuery))
            {
                searchResultReleases.add(r);
            }
        }

        updateAdapter(searchResultReleases);
        isUsingSearchResultsValue = true;
    }

    public void clearSearchQuery()
    {
        updateAdapter(persistReleases);
        isUsingSearchResultsValue = false;
    }

    private void updateAdapter(List<Release> newReleases)
    {
        ReleasesFragmentAdapter rfa = recyclerView == null ?
                null : (ReleasesFragmentAdapter)recyclerView.getAdapter();
        if (rfa != null)
        {
            rfa.releases = newReleases;
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

        recyclerView.setAdapter(new ReleasesFragmentAdapter(persistReleases, itemOnClickListener));
        isUsingSearchResultsValue = false;

        emptyTextView = rootView.findViewById(R.id.releases_empty_recycler);
        emptyTextView.setVisibility(
                persistReleases != null && persistReleases.size() > 0 ? View.INVISIBLE : View.VISIBLE);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        HomeActivity a = (HomeActivity)getActivity();
        a.disableDelete();
        a.enableSearch();
    }

    public boolean isUsingSearchResults() { return isUsingSearchResultsValue; }
}