package com.gomsang.lab.publicchain.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.gomsang.lab.publicchain.R;
import com.gomsang.lab.publicchain.databinding.DialogCampaignBinding;
import com.gomsang.lab.publicchain.datas.ChatMessageData;
import com.gomsang.lab.publicchain.datas.UserData;
import com.gomsang.lab.publicchain.datas.CampaignData;
import com.gomsang.lab.publicchain.datas.SignatureData;
import com.gomsang.lab.publicchain.libs.Constants;
import com.gomsang.lab.publicchain.libs.GlideApp;
import com.gomsang.lab.publicchain.libs.PublicChainState;
import com.gomsang.lab.publicchain.libs.utils.LocationUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gyeongrok Kim on 2017-08-09.
 */

public class CampaignDialog extends Dialog {

    private DialogCampaignBinding binding;

    private DatabaseReference database;

    private Context context;
    private CampaignData campaignData;
    private UserData currentUserData;

    public CampaignDialog(Context context, CampaignData campaignData, UserData currentUserData) {
        super(context);
        this.context = context;
        this.campaignData = campaignData;
        this.currentUserData = currentUserData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_campaign, null, false);
        setContentView(binding.getRoot());
        setDialogSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        database = FirebaseDatabase.getInstance().getReference();

        binding.campaignNameTextView.setText(campaignData.getName());

        if (campaignData.getLatitude() != null && campaignData.getLongitude() != null) {
            binding.addressTextView.setText(LocationUtil.getAddressInString(context, new LatLng(campaignData.getLatitude(),
                    campaignData.getLongitude())));
        } else {
            binding.addressTextView.setText("주소 정보가 없는 캠페인 입니다.");
        }

        binding.attachImageView.setOnClickListener(view -> CampaignDialog.this.dismiss());
        if (campaignData.getAttachImage() != null) {
            binding.attachImageView.setVisibility(View.VISIBLE);
            GlideApp.with(context).load(campaignData.getAttachImage()).centerCrop().into(binding.attachImageView);
        }

        binding.statusTextView.setOnClickListener((View view) -> {
            CurrentSignaturesDialog currentSignaturesDialog = new CurrentSignaturesDialog(context, campaignData);
            currentSignaturesDialog.show();
        });

        binding.descTextView.setText(campaignData.getDesc());

        binding.shareButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("이 캠페인을 커뮤니티 토론 채팅에 공유할까요?")
                    .setPositiveButton("네", (dialog, id) -> {
                        Toast.makeText(context, "캠페인이 커뮤니티 채팅에 공유되었습니다", Toast.LENGTH_LONG).show();
                        ChatMessageData chatMessageData = new ChatMessageData();
                        if (campaignData.isInstantCampaign()) {
                            chatMessageData.setMessageType("share-data");
                            chatMessageData.setContent(new Gson().toJson(campaignData));
                        } else {
                            chatMessageData.setMessageType("share");
                            chatMessageData.setContent(campaignData.getUuid());
                        }
                        chatMessageData.setAuthor(PublicChainState.getInstance().getCurrentUserData().getUid());
                        database.child("chats").child(Constants.CHATCHANNEL_COMMUNITY).child(System.currentTimeMillis() + "").setValue(chatMessageData);
                    })
                    .setNegativeButton("아니요", (dialog, id) -> {
                    });
            builder.create().show();
        });

        binding.signButton.setOnClickListener(view -> {
            if (currentUserData != null) {
                SignatureDialog signatureDialog = new SignatureDialog(context, campaignData, currentUserData);
                signatureDialog.show();
            } else {
                Toast.makeText(context, "require registration for participate", Toast.LENGTH_SHORT).show();
            }
        });

        // 서명 달성률 표시
        if (campaignData.isFunding()) {
            updateSignProgress(0, campaignData.getGoalOfContribution());
        } else {
            updateSignProgress(0, campaignData.getGoalOfSignature());
        }

        // 서명 실 달성률 (실 서명 개수) 로드
        database.child("signatures").orderByKey().equalTo(campaignData.getUuid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (campaignData.isFunding()) {
                    double fundingsum = 0;
                    for (DataSnapshot signature : dataSnapshot.getChildren()) {
                        fundingsum += signature.child("value").getValue(Double.class);
                    }
                    updateSignProgress(fundingsum, campaignData.getGoalOfContribution());
                } else {
                    updateSignProgress((int) dataSnapshot.getChildrenCount(), campaignData.getGoalOfSignature());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (campaignData.isFunding()) {
                    double fundingsum = 0;
                    for (DataSnapshot signature : dataSnapshot.getChildren()) {
                        fundingsum += signature.child("value").getValue(Double.class);
                    }
                    updateSignProgress(fundingsum, campaignData.getGoalOfContribution());
                } else {
                    updateSignProgress((int) dataSnapshot.getChildrenCount(), campaignData.getGoalOfSignature());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        database.child("signatures").child(campaignData.getUuid()).orderByChild("signerToken")
                .equalTo(currentUserData.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                SignatureData signatureData = dataSnapshot.getValue(SignatureData.class);

                Date signTime = new Date(signatureData.getSignTime());
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

                binding.signButton.setEnabled(false);
                binding.signButton.setBackgroundColor(Color.GRAY);
                binding.signButton.setText("signed in " + sdf.format(signTime));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateSignProgress(double progress, double max) {
        if (campaignData.isFunding()) {
            binding.statusTextView.setText("Achieve " + progress +
                    " ETH / " + max + " ETH");
        } else {
            binding.statusTextView.setText("Achieve " + (int) progress +
                    " / " + (int) max);
        }

        binding.statusProgressBar.setProgress((int) progress);
        binding.statusProgressBar.setMax((int) max);
    }


    private void setDialogSize(int width, int height) {
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.width = width;
        params.height = height;
        getWindow().setAttributes(params);
    }
}
