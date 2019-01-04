package com.ptato.aseeblabla.ui.list.releases;

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

import com.ptato.aseeblabla.ui.list.HomeActivity;
import com.ptato.aseeblabla.R;
import com.ptato.aseeblabla.ui.list.ReleasesFragmentAdapter;
import com.ptato.aseeblabla.data.db.Release;

import java.util.List;

public class SearchReleasesFragment extends Fragment
{
    private RecyclerView recyclerView;
    private ReleasesFragmentAdapter.OnClickReleaseListener itemOnClickListener;
    private List<Release> persistReleases = null;

    private TextView emptyTextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.releases_list_view, container, false);
        recyclerView = rootView.findViewById(R.id.releases_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        recyclerView.setAdapter(new ReleasesFragmentAdapter(persistReleases, itemOnClickListener));


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
    }


    public void setItemOnClickListener(ReleasesFragmentAdapter.OnClickReleaseListener listener)
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

        ReleasesFragmentAdapter rfa = recyclerView == null ?
                null : (ReleasesFragmentAdapter)recyclerView.getAdapter();
        if (rfa != null)
        {
            rfa.releases = releases;
            rfa.notifyDataSetChanged();
            if (releases.size() > 0)
                emptyTextView.setVisibility(View.INVISIBLE);
            else
                emptyTextView.setVisibility(View.VISIBLE);
        }

        if (releases.size() == 0)
        {
            emptyTextView.setText(R.string.results_not_found);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }
}
