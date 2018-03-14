package com.snipper.jt.snipper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by JT on 9/3/17.
 */

public class NotificationFragment extends Fragment implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    private Display mDisplaySize;
    private Point mPointDisplay;
    private SQLiteDatabase mDatabase;
    private NotificationAdapter mAdapter;
    private RelativeLayout mRootLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisplaySize = getActivity().getWindowManager().getDefaultDisplay();
        mPointDisplay = new Point();
        mDisplaySize.getSize(mPointDisplay);

        mDatabase = new SnipperHelper(getActivity()).getWritableDatabase();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.notifications_fragment, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRootLayout = (RelativeLayout) v.findViewById(R.id.root_layout);

        mRootLayout.setOnClickListener(this);

        updateUI();

        return v;
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.root_layout:
                getActivity().finish();
                break;
            default:
                break;
        }
    }

    public void updateUI(){
        if (mAdapter == null) {
            mAdapter = new NotificationAdapter(SnipperActivity.mActualUser);
            mRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setUser(SnipperActivity.mActualUser);
            mAdapter.notifyDataSetChanged();
        }

    }

    private class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private User user;
        private List<User> pendingFriends;

        public NotificationAdapter(User user) {
            this.user = user;
            pendingFriends = user.getPendingFriends();
        }

        class PendingSender extends RecyclerView.ViewHolder implements View.OnClickListener{
            private TextView name;
            private CircleImageView profileImage;

            public PendingSender(View view){
                super(view);
                profileImage = view.findViewById(R.id.profile_thumb);
                name = view.findViewById(R.id.profile_name);
                profileImage.setOnClickListener(this);

            }

            @Override
            public void onClick(View v){
                switch (v.getId()){
                    case R.id.profile_thumb:
                        break;
                }
            }

            public void bindMessage(User user){
                name.setText(user.getUsername());
                if (user.getmProfileImagePath().equals("null")){
                    profileImage.setImageResource(R.mipmap.ic_profile_thumb);
                } else {
                    Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + user.getmProfileImagePath()).apply(new RequestOptions().override(120,120).centerCrop()).into(profileImage);
                }
            }
        }

        class PendingRequest extends RecyclerView.ViewHolder implements View.OnClickListener{
            private TextView name;
            private CircleImageView profileImage;
            private CircleImageView accept;
            private CircleImageView reject;
            private User user;

            public PendingRequest(View view){
                super(view);
                profileImage = view.findViewById(R.id.profile_thumb);
                name = view.findViewById(R.id.profile_name);
                accept = view.findViewById(R.id.accept);
                reject = view.findViewById(R.id.reject);

                accept.setOnClickListener(this);
                reject.setOnClickListener(this);
                profileImage.setOnClickListener(this);

            }

            @Override
            public void onClick(View v){
                switch (v.getId()){
                    case R.id.profile_thumb:
                        break;
                    case R.id.accept:
                        new ConfirmFriend().execute(user);
                        removeRecyclerElement(getAdapterPosition());
                        break;
                    case R.id.reject:
                        new RejectFriend().execute(user);
                        removeRecyclerElement(getAdapterPosition());
                        break;
                }
            }

            public void bindMessage(User user){
                this.user = user;
                name.setText(user.getUsername());
                if (user.getmProfileImagePath().equals("null")){
                    profileImage.setImageResource(R.mipmap.ic_profile_thumb);
                } else {
                    Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + user.getmProfileImagePath()).apply(new RequestOptions().override(120,120).centerCrop()).into(profileImage);
                }
            }
        }


        @Override
        public int getItemViewType(int position) {
            String sender = pendingFriends.get(position).getmPendingSender();
            if (sender.equals(SnipperActivity.mActualUser.getmTag())){
                return 0;
            } else {
                return 1;
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType){
            View view;
            switch (viewType){
                case 0:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.individual_pending_sender, parent, false);
                    return new PendingSender(view);
                case 1:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.individual_pending_request, parent, false);
                    return new PendingRequest(view);
                default:
                    Log.d("MFMFMF", "Not valid viewtype.");
                    return new PendingSender(null);
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    PendingSender pendingSender = (PendingSender) holder;
                    pendingSender.bindMessage(user.getPendingFriends().get(position));
                    break;
                case 1:
                    PendingRequest pendingRequest = (PendingRequest) holder;
                    pendingRequest.bindMessage(user.getPendingFriends().get(position));
                    break;
            }
        }

        @Override
        public int getItemCount(){
            Log.d("MFMFMF", String.valueOf(pendingFriends.size()));
            return pendingFriends.size();
        }

        public void setUser(User user){
            this.user = user;
            pendingFriends = user.getPendingFriends();
        }

        public void removeRecyclerElement(int position){
            pendingFriends.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, pendingFriends.size());
        }
    }

    private class ConfirmFriend extends AsyncTask<User, Void, Void>{
        @Override
        protected Void doInBackground(User... params) {
            new SqlHTTP().confirmPendingFriend(SnipperActivity.mActualUser.getmuID(), params[0].getmTag(), SnipperActivity.mActualUser.getmTag());
            return null;
        }
    }

    private class RejectFriend extends AsyncTask<User, Void, Void>{
        @Override
        protected Void doInBackground(User... params) {
            new SqlHTTP().rejectPendingFriend(SnipperActivity.mActualUser.getmuID(), params[0].getmTag());
            return null;
        }
    }

}
