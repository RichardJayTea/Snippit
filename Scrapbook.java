package com.snipper.jt.snipper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by JT on 9/9/17.
 */

public class Scrapbook extends Fragment{
    private CardView mCardView;
    public static User mUser;
    private RecyclerView mRecyclerView;
    private ProfileSnipAdapter mProfileSnipAdapter;
    private RecyclerView mStoryRecyclerView;
    private ProfileStoryAdapter mProfileStoryAdapter;
    private LinearLayout mRootLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.scrapbook_fragment, container, false);
        mRootLayout = (LinearLayout) v.findViewById(R.id.root_layout);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.profile_fragment_recycler);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.addItemDecoration(new ItemDecorationAlbumColumns(10, 3, 0));
        //mRecyclerView.setHasFixedSize(true);

        mStoryRecyclerView = (RecyclerView) v.findViewById(R.id.profile_fragment_recycler_story);
        mStoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));

        updateUI();


        return v;
    }


    public void updateUI(){
        if (mProfileSnipAdapter == null) {
            mProfileSnipAdapter = new ProfileSnipAdapter(mUser);
            mRecyclerView.setAdapter(mProfileSnipAdapter);
        }
        else {
            mProfileSnipAdapter.setFeed(mUser);
            mProfileSnipAdapter.notifyDataSetChanged();
        }

        if (mProfileStoryAdapter == null) {
            mProfileStoryAdapter = new ProfileStoryAdapter(mUser);
            mStoryRecyclerView.setAdapter(mProfileStoryAdapter);
        }
        else {
            mProfileStoryAdapter.setFeed(mUser);
            mProfileStoryAdapter.notifyDataSetChanged();
        }

    }

    //STORY GRID
    private class ProfileStoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private User mUser;
        private int mPosition;
        private Picture mPicture;
        private CircleImageView mImageView;
        private RelativeLayout mRelativeLayout;
        private CardView mCardView;

        public ProfileStoryHolder(View itemView){
            super(itemView);
            mImageView = (CircleImageView) itemView.findViewById(R.id.individual_image_thumbnails);
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.individual_snip_grid);
            mCardView = (CardView) itemView.findViewById(R.id.snip_grid_cardview);
            mRelativeLayout.setOnClickListener(this);

        }

        @Override
        public void onClick(View v){
                switch (v.getId()) {
                    case R.id.individual_snip_grid:
                        mUser.setmPictureCounter(mPosition);
                        //FullScreenImageFeedFragment.mUser = mUser;
                        //FullScreenImageFeedFragment.isStory = true;
                        FullScreenImageFeedFragment.mRecordLastSnip = false;
                        Intent intent = FullScreenImageStillFeedActivity.newIntent(getActivity(), (ArrayList<Picture>) mUser.getmStoryPicture(), mUser.getmPictureCounter());
                        startActivity(intent);
                    default:
                        Log.d("FeedFragment: ", "View onClick switch case");
                        break;
                }

        }

        public void bindFeed(User user, Picture picture, int position) {
            mUser = user;
            mPosition = position;
            mPicture = picture;
            Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP +  mPicture.getImagePath()).apply(new RequestOptions().override(140,140).centerCrop()).into(mImageView);

        }


    }

    private class ProfileStoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private User profileUser;

        public ProfileStoryAdapter(User user) {
            profileUser = user;
            //Log.d("USERLISTSIZE", snipUsers.get(0).getmuID() + " " + snipUsers.get(1).getmuID(), new Exception());
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType){
            View feedView = LayoutInflater.from(getActivity()).inflate(R.layout.recent_story_grid, parent, false);


            return new ProfileStoryHolder(feedView);


        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Picture picture = profileUser.getmStoryPicture().get(position);
            ((ProfileStoryHolder) holder).bindFeed(profileUser, picture, position);
        }

        @Override
        public int getItemCount(){
            return profileUser.getmStoryPicture().size();
        }

        public void setFeed(User user){ profileUser = user;}
    }

    //SCRAPBOOK GRID
    private class ProfileSnipHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private User mUser;
        private int mPosition;
        private Picture mPicture;
        private ImageView mImageView;
        private LinearLayout mLinearLayout;
        private CardView mCardView;

        public ProfileSnipHolder(View itemView){
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.individual_image_thumbnails);
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.individual_snip_grid);
            mCardView = (CardView) itemView.findViewById(R.id.snip_grid_cardview);
            mLinearLayout.setOnClickListener(this);

        }

        @Override
        public void onClick(View v){
                switch (v.getId()) {
                    case R.id.individual_snip_grid:
                        mUser.setmFeedCounter(mPosition);
                        //FullScreenImageFeedFragment.mUser = mUser;
                        //FullScreenImageFeedFragment.isFeed = true;
                        FullScreenImageFeedFragment.mRecordLastSnip = false;
                        Intent intent = FullScreenImageStillFeedActivity.newIntent(getActivity(), (ArrayList<Picture>)mUser.getFeedPictures(), mUser.getmFeedCounter());
                        startActivity(intent);
                    default:
                        Log.d("FeedFragment: ", "View onClick switch case");
                        break;
                }

        }

        public void bindFeed(User user, Picture picture, int position) {
            mUser = user;
            mPosition = position;
            mPicture = picture;
            //Glide.with(getContext()).load(SqlHTTP.BASE_URL + mPicture.getImagePath()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).fit().centerCrop().into(mImageView);
            Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + mPicture.getImagePath()).apply(new RequestOptions().override(280,280).centerCrop()).into(mImageView);

        }


    }

    private class ProfileSnipAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private User profileUser;

        public ProfileSnipAdapter(User user) {
            profileUser = user;
            //Log.d("USERLISTSIZE", snipUsers.get(0).getmuID() + " " + snipUsers.get(1).getmuID(), new Exception());
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType){
            View feedView = LayoutInflater.from(getActivity()).inflate(R.layout.recent_scrapbook_grid, parent, false);


            return new ProfileSnipHolder(feedView);


        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Picture picture = profileUser.getFeedPictures().get(position);
            ((ProfileSnipHolder) holder).bindFeed(profileUser, picture, position);
        }

        @Override
        public int getItemCount(){
            return profileUser.getFeedPictures().size();
        }

        public void setFeed(User user){ profileUser = user;}
    }
}
