package com.gomsang.lab.publicchain.ui.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gomsang.lab.publicchain.R;
import com.gomsang.lab.publicchain.databinding.ItemCampaignBinding;
import com.gomsang.lab.publicchain.databinding.ItemChatBinding;
import com.gomsang.lab.publicchain.databinding.ItemChatMineBinding;
import com.gomsang.lab.publicchain.databinding.ItemChatShareBinding;
import com.gomsang.lab.publicchain.datas.CampaignData;
import com.gomsang.lab.publicchain.datas.ChatMessageData;
import com.gomsang.lab.publicchain.datas.UserData;
import com.gomsang.lab.publicchain.libs.PublicChainState;
import com.gomsang.lab.publicchain.ui.dialogs.CampaignDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by devkg on 2018-01-15.
 */

public class ChatAdpater extends RecyclerView.Adapter<ChatAdpater.ViewHolder> {

    private static final int TYPE_TEXT = 1;
    private static final int TYPE_TEXT_MINE = 2;
    private static final int TYPE_SHARE = 3;

    private DatabaseReference database;
    private HashMap<String, String> userNameCache = new HashMap<>();
    private ArrayList<ChatMessageData> chatMessageDatas = new ArrayList<>();

    private Context context;

    public ChatAdpater(Context context) {
        this.context = context;
        this.database = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case TYPE_TEXT:
                return new ViewHolder((ItemChatBinding) DataBindingUtil.inflate(inflater, R.layout.item_chat, parent, false));
            case TYPE_TEXT_MINE:
                return new ViewHolder((ItemChatMineBinding) DataBindingUtil.inflate(inflater, R.layout.item_chat_mine, parent, false));
            case TYPE_SHARE:
                return new ViewHolder((ItemChatShareBinding) DataBindingUtil.inflate(inflater, R.layout.item_chat_share, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(chatMessageDatas.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageData chatMessageData = chatMessageDatas.get(position);
        if (chatMessageData.getMessageType().equals("text")) {
            if (chatMessageData.getAuthor().equals(PublicChainState.getInstance().getCurrentUserData().getUid())) {
                return TYPE_TEXT_MINE;
            } else {
                return TYPE_TEXT;
            }
        }
        if (chatMessageData.getMessageType().equals("share") || chatMessageData.getMessageType().equals("share-data")) {
            return TYPE_SHARE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return chatMessageDatas.size();
    }

    public void addChat(ChatMessageData chatMessageData) {
        chatMessageDatas.add(chatMessageData);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        public ViewHolder(ItemChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ViewHolder(ItemChatMineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ViewHolder(ItemChatShareBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatMessageData chatMessageData) {
            if (binding instanceof ItemChatBinding) {
                ItemChatBinding itemChatBinding = (ItemChatBinding) binding;
                itemChatBinding.messageText.setText(chatMessageData.getContent());
                getUsernameByUid(chatMessageData.getAuthor(), itemChatBinding.messageAuthor, "");
            }
            if (binding instanceof ItemChatMineBinding) {
                ItemChatMineBinding itemChatMineBinding = ((ItemChatMineBinding) binding);
                itemChatMineBinding.messageText.setText(chatMessageData.getContent());
            }
            if (binding instanceof ItemChatShareBinding) {
                ItemChatShareBinding itemChatShareBinding = ((ItemChatShareBinding) binding);
                getUsernameByUid(chatMessageData.getAuthor(), itemChatShareBinding.infoText, "님이 캠페인을 공유하셨습니다");

                if (chatMessageData.getMessageType().equals("share")) {
                    database.child("campaigns").child(chatMessageData.getContent()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            itemChatShareBinding.campaignName.setText(dataSnapshot.getValue(CampaignData.class).getName());
                            itemChatShareBinding.signButton.setOnClickListener(view ->
                                    new CampaignDialog(context, dataSnapshot.getValue(CampaignData.class),
                                            PublicChainState.getInstance().getCurrentUserData()).show());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if (chatMessageData.getMessageType().equals("share-data")) {
                    final CampaignData campaignData = new Gson().fromJson(chatMessageData.getContent(), CampaignData.class);
                    itemChatShareBinding.campaignName.setText(campaignData.getName());
                    itemChatShareBinding.signButton.setOnClickListener(view ->
                            new CampaignDialog(context, campaignData,
                                    PublicChainState.getInstance().getCurrentUserData()).show());
                }
            }
        }
    }

    public void getUsernameByUid(String uid, TextView textView, String add) {
        if (userNameCache.containsKey(uid)) textView.setText(userNameCache.get(uid) + add);

        database.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userNameCache.put(uid, dataSnapshot.getValue(UserData.class).getName());
                textView.setText(userNameCache.get(uid) + add);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
