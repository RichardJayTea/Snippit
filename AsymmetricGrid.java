package com.snipper.jt.snipper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JT on 11/8/17.
 */

public class AsymmetricGrid extends Fragment {
    private GridAdapter mGridAdapter;
    private RecyclerView mRecyclerView;
    private List<Picture> mPictures;
    private List<Picture> mPictureList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Point mPointDisplay;
    private Display mDisplaySize;


    public void setPictures(List<Picture> pictures){
        mPictures = pictures;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.recyclerview, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.search_recycler);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.addItemDecoration(new InsetDecoration(getActivity()));
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swiperefresh);

        mDisplaySize = getActivity().getWindowManager().getDefaultDisplay();
        mPointDisplay = new Point();
        mDisplaySize.getSize(mPointDisplay);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                Thread t = new Thread(){
                    public void run(){
                        ((ThemeTrendingFragment)getParentFragment()).executeFetchTrending();
                    }
                };
                t.start();
            }
        });

        ((ThemeTrendingFragment)getParentFragment()).executeFetchTrending();

        return v;
    }

    public void toggleRefreshing(final boolean enabled){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(enabled);
                updateUI();
            }
        });
    }

    public void updateUI(){
        mPictureList.clear();
        mPictureList.addAll(mPictures);
        mPictureList.add(0, new Picture());
        if (mGridAdapter == null){
            mGridAdapter = new GridAdapter(mPictureList);
            mRecyclerView.setAdapter(mGridAdapter);
        } else {
            mGridAdapter.setFeed(mPictureList);
            mGridAdapter.notifyDataSetChanged();
        }
    }

    private class DiscoverText extends RecyclerView.ViewHolder{
        private TextView mDiscoverText;

        public DiscoverText(View itemView){
            super(itemView);
            mDiscoverText = (TextView) itemView.findViewById(R.id.discover_text);
        }
    }

    private class GridHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private int mPosition;
        private Picture mPicture;
        private ImageView mImageView;
        //private RelativeLayout mRelativeLayout;
        private List<Picture> mPictureList;
        private int offset;

        public GridHolder(View itemView){
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.staggered_grid_image);
            mImageView.setOnClickListener(this);
            offset = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.card_insets);
            //mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.individual_staggered_grid);
            //mRelativeLayout.setOnClickListener(this);

        }

        @Override
        public void onClick(View v){
                switch (v.getId()) {
                    case R.id.staggered_grid_image:
                        FullScreenImageFeedFragment.mRecordLastSnip = false;
                        Intent intent = FullScreenImageStillFeedActivity.newIntent(getActivity(), (ArrayList<Picture>)mPictureList, mPosition);
                        startActivity(intent);
                    default:
                        Log.d("FeedFragment: ", "View onClick switch case");
                        break;
                }
        }

        public void bindFeed(List<Picture> pictures, Picture picture, int position, int height) {
            mPosition = position;
            mPictureList = pictures;
            mPicture = picture;
            Log.d("AGAGAG", (mPointDisplay.x / 2) - (2 * offset) + " " + height);
            Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP +  mPicture.getImagePath()).apply(new RequestOptions().override((mPointDisplay.x / 2) - (2 * offset), height).centerCrop().timeout(20000)).into(mImageView);

        }


    }

    private class GridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Picture> pictures;
        private Picture picture;

        public GridAdapter(List<Picture> pics) {
            pictures = pics;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType){
            if (viewType == 0){
                View feedView = LayoutInflater.from(getActivity()).inflate(R.layout.discover_text, parent, false);
                return new DiscoverText(feedView);
            } else {
                View feedView = LayoutInflater.from(getActivity()).inflate(R.layout.recent_snips, parent, false);
                return new GridHolder(feedView);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0){
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == 0){
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
            } else {
                final View itemView = holder.itemView;
                int height;
                if (position % 4 == 0) {
                    height = itemView.getContext().getResources()
                            .getDimensionPixelSize(R.dimen.card_staggered_height);
                    itemView.setMinimumHeight(height);
                } else {
                    height = itemView.getContext().getResources()
                            .getDimensionPixelSize(R.dimen.one_hundred_dp);
                    itemView.setMinimumHeight(height);
                }
                picture = pictures.get(position);
                ((GridHolder) holder).bindFeed(pictures, picture, position, height);
            }
        }

        @Override
        public int getItemCount(){
            return pictures.size();
        }

        public void setFeed(List<Picture> pics){
            pictures = pics;
        }
    }

    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
