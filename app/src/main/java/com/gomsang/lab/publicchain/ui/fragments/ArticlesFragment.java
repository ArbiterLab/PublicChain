package com.gomsang.lab.publicchain.ui.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gomsang.lab.publicchain.R;
import com.gomsang.lab.publicchain.databinding.FragmentArticlesBinding;
import com.gomsang.lab.publicchain.datas.CampaignData;
import com.gomsang.lab.publicchain.datas.opendata.Item;
import com.gomsang.lab.publicchain.datas.opendata.Response;
import com.gomsang.lab.publicchain.libs.Constants;
import com.gomsang.lab.publicchain.libs.PublicChainState;
import com.gomsang.lab.publicchain.libs.RecyclerItemClickListener;
import com.gomsang.lab.publicchain.ui.adapters.CampaignAdapter;
import com.gomsang.lab.publicchain.ui.dialogs.CampaignDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.gomsang.lab.publicchain.libs.opendata.OpenDataReturn.requestRceptData;

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

        if (sort.equals(Constants.BOARDSORT_USERCAMPAIGNS)) {
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
        } else if (sort.equals(Constants.BOARDSORT_BILLS)) {
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

            Response response = requestRceptData(10, 1, "");
            //numOfRows가 요청 개수라고 생각하시면됨
            //bill_name는 키워드로 목록뽑아준다 생각하면됨 의안 던진놈 기준

            int countArray = response.getBody().getNumOfRows();
            Random signature = new Random();
            for (int i = 0; i < countArray; i++) { //Rcept to Campaign 빌런 + adapter add
                CampaignData data = new CampaignData();
                Item body = response.getBody() //리스폰 데이터 객체 빤쓰런
                        .getItems()
                        .getItems()
                        .get(i);

                data.setAuthor(body.getProposerKind());
                data.setDesc("내용이 없습니다");
                data.setFunding(false);
                try {
                    data.setSignTime(new SimpleDateFormat("yyyy-MM-dd").parse(body.getProposeDt()).getTime());
                } catch (ParseException e) {
                    Log.d("errondate", e.getLocalizedMessage());
                    e.printStackTrace();
                }
                data.setInstantCampaign(true);
                data.setGoalOfContribution(10.5); //?
                data.setGoalOfSignature(signature.nextInt(100)); // 목표량 현재 랜덤값
                data.setName(body.getBillName());
                data.setUuid(body.getBillId()); //UUID
                //위치데이터 포함 안했음 적절하게 더미데이터 해주시길바람

                campaignAdapter.addItem(data);
                campaignAdapter.notifyDataSetChanged();
            }
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