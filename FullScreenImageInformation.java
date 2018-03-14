package com.snipper.jt.snipper;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by JT on 11/12/17.
 */

public class FullScreenImageInformation extends Fragment implements View.OnClickListener{
    private Picture mPicture;
    private RelativeLayout mRootLayout;
    private View mFillView;
    private ImageView mImageSwitcher;
    private CircleImageView mProfileImage;
    private TextView mProfileName;
    private TextView mDescription;
    private TextView mAdore;
    private boolean isLiked = false;
    private User mUser;
    private ImageButton mOptions;
    private View mBottom;
    private int mLikes = 0;

    public void setPicture(Picture pic){
        mPicture = pic;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.full_screen_image_information, container, false);
        mRootLayout = (RelativeLayout) v.findViewById(R.id.root_layout);
        mFillView = (View) v.findViewById(R.id.fill_view);
        mImageSwitcher = (ImageView) v.findViewById(R.id.adore_button);
        mProfileName = (TextView) v.findViewById(R.id.profile_name);
        mProfileImage = (CircleImageView) v.findViewById(R.id.profile_thumb);
        mDescription = (TextView) v.findViewById(R.id.description);
        mAdore = (TextView) v.findViewById(R.id.adores);
        mOptions = (ImageButton) v.findViewById(R.id.overflow_options);
        mBottom = (View) v.findViewById(R.id.bottom_transparent);

        mOptions.setOnClickListener(this);
        mImageSwitcher.setOnClickListener(this);
        mRootLayout.setOnClickListener(this);

        setUpInformation();

        return v;
    }

    private void setUpInformation(){
        new checkLike().execute(mPicture.getImagePath());
        mRootLayout.animate().translationY(0);
        mProfileName.setText(mPicture.getOwnerName());
        if (mPicture.getmOwnerImage().equals("null")){
            mProfileImage.setImageResource(R.mipmap.ic_profile_thumb);
        } else {
            Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + mPicture.getmOwnerImage()).apply(new RequestOptions().override(120,120).centerCrop()).into(mProfileImage);
        }
        if (mPicture.getmDescription().equals("null")){
            mDescription.setText(" " );
        } else {
            mDescription.setText(mPicture.getmDescription());
        }
    }

    public void showDialog(){
        CharSequence optionsMyPhotos[] = new CharSequence[] {"Delete", "Report"};
        CharSequence options[] = new CharSequence[] {"Report"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (mPicture.getmOwnerTag().equals(SnipperActivity.mActualUser.getmTag())){
            builder.setItems(optionsMyPhotos, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0:
                            new DeletePhotoTask().execute(mPicture.getImagePath());
                            break;
                        case 1:
                            new ReportPhotoTask().execute(mPicture.getImagePath());
                            break;
                        default:
                            break;
                    }
                }
            });
        } else {
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0:
                            new ReportPhotoTask().execute(mPicture.getImagePath());
                            break;
                        default:
                            break;
                    }
                }
            });
        }
        builder.show();
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.adore_button:
                if (!isLiked){
                    mImageSwitcher.setImageResource(R.drawable.yellow_heart);
                    new LikePhotoTask().execute(mPicture.getImagePath());
                    mLikes += 1;
                    mAdore.setText(String.valueOf(mLikes));
                    isLiked = true;
                } else {
                    mImageSwitcher.setImageResource(R.drawable.white_heart);
                    new DislikePhotoTask().execute(mPicture.getImagePath());
                    mLikes -= 1;
                    mAdore.setText(String.valueOf(mLikes));
                    isLiked = false;
                }
                break;
            case R.id.root_layout:
                getFragmentManager().popBackStack();
                break;
            case R.id.overflow_options:
                showDialog();
                break;
            default:
                break;
        }
    }

    private class LikePhotoTask extends AsyncTask<String, Void, Void>{
        protected Void doInBackground(String... params ){
            new SqlHTTP().LikePhoto(SnipperActivity.mActualUser.getmuID(), params[0], SnipperActivity.mActualUser.getmTag(), mPicture.getmOwnerTag());
            return null;
        }
    }

    private class DislikePhotoTask extends AsyncTask<String, Void, Void>{
        protected Void doInBackground(String... params ){
            new SqlHTTP().DislikePhoto(SnipperActivity.mActualUser.getmuID(), params[0]);
            return null;
        }
    }

    private class checkLike extends AsyncTask<String, Void, Void>{
        protected Void doInBackground(String... params ){
            List<String> listOfuID = new SqlHTTP().fetchLikes(params[0]);
            mLikes = listOfuID.size();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdore.setText(String.valueOf(mLikes));
                }
            });
            int binarySearchReturn = Collections.binarySearch(listOfuID, SnipperActivity.mActualUser.getmTag());
            if (binarySearchReturn == listOfuID.size() || binarySearchReturn < 0){
                return null;
            } else if (listOfuID.get(binarySearchReturn).equals(SnipperActivity.mActualUser.getmTag())){
                isLiked = true;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageSwitcher.setImageResource(R.drawable.yellow_heart);
                    }
                });
            }
            return null;
        }
    }

    private class DeletePhotoTask extends AsyncTask<String, Void, Void>{
        protected Void doInBackground(String... params ){
            new SqlHTTP().deletePhoto(SnipperActivity.mActualUser.getmuID(), params[0]);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().finish();
                }
            });
            return null;
        }
    }

    private class ReportPhotoTask extends AsyncTask<String, Void, Void>{
        protected Void doInBackground(String... params ){
            new SqlHTTP().reportPhoto(params[0]);
            return null;
        }
    }

}
