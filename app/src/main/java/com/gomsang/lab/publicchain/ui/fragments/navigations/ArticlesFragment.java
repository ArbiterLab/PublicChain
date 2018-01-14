package com.gomsang.lab.publicchain.ui.fragments.navigations;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.gomsang.lab.publicchain.R;
import com.gomsang.lab.publicchain.databinding.FragmentArticlesBinding;
import com.gomsang.lab.publicchain.datas.CampaignData;
import com.gomsang.lab.publicchain.libs.PublicChainState;
import com.gomsang.lab.publicchain.libs.RecyclerItemClickListener;
import com.gomsang.lab.publicchain.libs.VerticalSpaceItemDecoration;
import com.gomsang.lab.publicchain.ui.adapters.CampaignAdapter;
import com.gomsang.lab.publicchain.ui.dialogs.CampaignDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ArticlesFragment extends Fragment {
    private static final String ARG_SORT = "sort";
    private String sort;
    private FragmentArticlesBinding binding;
    private DatabaseReference database;

    public ArticlesFragment() {
        this.database = FirebaseDatabase.getInstance().getReference();
    }


    public static ArticlesFragment newInstance(String sort) {
        ArticlesFragment fragment = new ArticlesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SORT, sort);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sort = getArguments().getString(ARG_SORT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_articles, null, false);
        binding.articleRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
//        binding.articleRecycler.addItemDecoration(new VerticalSpaceItemDecoration(16));

        if (sort.equals("유저 캠페인")) {
            final CampaignAdapter campaignAdapter = new CampaignAdapter(getActivity());
            binding.articleRecycler.setAdapter(campaignAdapter);
            binding.articleRecycler.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), binding.articleRecycler, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    CampaignDialog campaignDialog = new CampaignDialog(getActivity(), campaignAdapter.getItem(position), PublicChainState
                            .getInstance().getCurrentUserData());
                    campaignDialog.show();
                }

                @Override
                public void onLongItemClick(View view, int position) {

                }
            }));

            database.child("campaigns").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshots) {
                    for (DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
                        campaignAdapter.addItem(dataSnapshot.getValue(CampaignData.class));
                        campaignAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}