package com.snipper.jt.snipper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by JT on 10/15/17.
 */

public class MessageFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private MessageAdapter mAdapter;
    private TextInputEditText mInputEditText;
    private TextView mProfileName;
    private BroadcastReceiver mBR;
    private FetchMessageTask mFetchMessages;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.message_fragment, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.message_recyclerview);
        mInputEditText = (TextInputEditText) v.findViewById(R.id.message_edit_text);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProfileName = (TextView) v.findViewById(R.id.profile_name);

        mInputEditText.setImeOptions(mInputEditText.getImeOptions()| EditorInfo.IME_ACTION_DONE);
        mInputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE){
                    List<String> messageParameters = new ArrayList<String>();
                    messageParameters.add(SnipperActivity.mActualUser.getmuID());
                    messageParameters.add(ProfileFragment.mUser.getmTag());
                    messageParameters.add(textView.getText().toString());
                    new SendMessageTask().execute(messageParameters);
                    mInputEditText.getText().clear();
                    return true;
                }
                return false;
            }
        });
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                scrollRecyclerBottom();
            }
        });
        /*mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                toggleScroll(!(recyclerView.canScrollVertically(1)));
            }
        });
        toggleScroll(true);*/

        mProfileName.setText(ProfileFragment.mUser.getUsername());
        updateUI();


        mBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ProfileFragment.mUser.getmTag().equals(intent.getStringExtra("senderTag")) && !(ProfileFragment.mUser.getmTag().equals(SnipperActivity.mActualUser.getmTag()))){
                    ProfileFragment.mUser.getMessage().add(new Message(intent.getStringExtra("senderTag"), intent.getStringExtra("message")));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUIFirebase();
                        }
                    });
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.snipper.jt.snipper.MessageFragment");
        getActivity().registerReceiver(mBR, filter);


        return v;
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    public void executeFetch(){
        mFetchMessages = new FetchMessageTask();
        mFetchMessages.execute(ProfileFragment.mUser);
    }

    public void updateUI(){
        if (mAdapter == null) {
            mAdapter = new MessageAdapter(ProfileFragment.mUser);
            mRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setUser(ProfileFragment.mUser);
            mAdapter.notifyDataSetChanged();
        }
        scrollRecyclerBottom();
    }

    public void updateUIFirebase(){
        if (mAdapter == null) {
            mAdapter = new MessageAdapter(ProfileFragment.mUser);
            mRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setUser(ProfileFragment.mUser);
            mAdapter.notifyItemInserted(ProfileFragment.mUser.getMessage().size() - 1);
        }
        scrollRecyclerBottom();
    }

    private void scrollRecyclerBottom(){
        if (mAdapter != null && mAdapter.getItemCount() != 0){
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    // Call smooth scroll
                    mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                }
            });
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Message> messageList = new ArrayList<Message>();
        private User profileUser;
        private boolean isStarting = true;

        public MessageAdapter(User user) {
            profileUser = user;
            messageList = profileUser.getMessage();
        }

        class MessageHeader extends RecyclerView.ViewHolder {
            private Message message;
            private CircleImageView imageView;
            private TextView name;
            private TextView content;

            public MessageHeader(View view){
                super(view);
                content = view.findViewById(R.id.message_list_text);
                imageView = view.findViewById(R.id.message_user_thumb);
                name = view.findViewById(R.id.message_user_name);

            }

            public void bindMessage(User user, int position){
                message = user.getMessage().get(position);
                if (message.getmTag().equals(SnipperActivity.mActualUser.getmTag())){
                    name.setText(SnipperActivity.mActualUser.getUsername());
                    Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + SnipperActivity.mActualUser.getmProfileImagePath()).into(imageView);
                } else {
                    name.setText(user.getUsername());
                    Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + user.getmProfileImagePath()).into(imageView);
                }
                content.setText(message.getmContent());
            }

        }

        class MessageText extends RecyclerView.ViewHolder {
            private TextView content;

            public MessageText(View view){
                super(view);
                content = view.findViewById(R.id.message_list_text);
            }

            public void bindMessage(Message message){
                content.setText(message.getmContent());
            }

        }

        class MessageDivider extends RecyclerView.ViewHolder {
            private TextView content;

            public MessageDivider(View view){
                super(view);
                content = view.findViewById(R.id.message_text_above_divider);
            }

            public void bindMessage(Message message){
                content.setText(message.getmContent());
            }

        }

        class MessageHeaderTextDivider extends RecyclerView.ViewHolder{
            private Message message;
            private CircleImageView imageView;
            private TextView name;
            private TextView content;

            public MessageHeaderTextDivider(View view){
                super(view);
                content = view.findViewById(R.id.message_text_above_divider);
                imageView = view.findViewById(R.id.message_user_thumb);
                name = view.findViewById(R.id.message_user_name);
            }

            public void bindMessage(User user, int position){
                message = user.getMessage().get(position);
                if (message.getmTag().equals(SnipperActivity.mActualUser.getmTag())){
                    name.setText(SnipperActivity.mActualUser.getUsername());
                    Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + SnipperActivity.mActualUser.getmProfileImagePath()).into(imageView);
                } else {
                    name.setText(user.getUsername());
                    Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + user.getmProfileImagePath()).into(imageView);
                }
                content.setText(message.getmContent());
            }
        }

        class MessageImageHeader extends RecyclerView.ViewHolder implements View.OnClickListener{
            private Message message;
            private CircleImageView imageView;
            private TextView name;
            private ImageView image;
            private Picture pic;

            public MessageImageHeader(View view){
                super(view);
                image = view.findViewById(R.id.message_image);
                imageView = view.findViewById(R.id.message_user_thumb);
                name = view.findViewById(R.id.message_user_name);
                image.setOnClickListener(this);

            }

            @Override
            public void onClick(View v){
                switch (v.getId()){
                    case R.id.message_image:
                        List<Picture> picture = new ArrayList<>();
                        picture.add(pic);
                        Intent intent = FullScreenImageStillFeedActivity.newIntent(getActivity(), (ArrayList<Picture>)picture, 0);
                        startActivity(intent);
                        break;
                }
            }

            public void bindMessage(User user, int position){
                message = user.getMessage().get(position);
                if (message.getmTag().equals(SnipperActivity.mActualUser.getmTag())){
                    name.setText(SnipperActivity.mActualUser.getUsername());
                    Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + SnipperActivity.mActualUser.getmProfileImagePath()).into(imageView);
                } else {
                    name.setText(user.getUsername());
                    Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + user.getmProfileImagePath()).into(imageView);
                }
                Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + message.getPic().getImagePath()).apply(new RequestOptions().override(800,800).centerInside()).into(image);
                pic = message.getPic();
            }

        }

        class MessageImage extends RecyclerView.ViewHolder implements View.OnClickListener {
            private ImageView image;
            private Picture pic;

            public MessageImage(View view){
                super(view);
                image = view.findViewById(R.id.message_image);
                image.setOnClickListener(this);

            }

            @Override
            public void onClick(View v){
                switch (v.getId()){
                    case R.id.message_image:
                        List<Picture> picture = new ArrayList<>();
                        picture.add(pic);
                        Intent intent = FullScreenImageStillFeedActivity.newIntent(getActivity(), (ArrayList<Picture>)picture, 0);
                        startActivity(intent);
                        break;
                }
            }

            public void bindMessage(Message message){
                Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + message.getPic().getImagePath()).apply(new RequestOptions().override(800,800).centerInside()).into(image);
                pic = message.getPic();

            }

        }

        class MessageImageDivider extends RecyclerView.ViewHolder implements View.OnClickListener{
            private ImageView image;
            private Picture pic;

            public MessageImageDivider(View view){
                super(view);
                image = view.findViewById(R.id.message_image);
                image.setOnClickListener(this);

            }

            @Override
            public void onClick(View v){
                switch (v.getId()){
                    case R.id.message_image:
                        List<Picture> picture = new ArrayList<>();
                        picture.add(pic);
                        Intent intent = FullScreenImageStillFeedActivity.newIntent(getActivity(), (ArrayList<Picture>)picture, 0);
                        startActivity(intent);
                        break;
                }
            }

            public void bindMessage(Message message){
                Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + message.getPic().getImagePath()).apply(new RequestOptions().override(800,800).centerInside()).into(image);
                pic = message.getPic();
            }

        }

        class MessageImageHeaderDivider extends RecyclerView.ViewHolder implements View.OnClickListener{
            private Message message;
            private CircleImageView imageView;
            private TextView name;
            private ImageView image;
            private Picture pic;

            public MessageImageHeaderDivider(View view){
                super(view);
                image = view.findViewById(R.id.message_image);
                imageView = view.findViewById(R.id.message_user_thumb);
                name = view.findViewById(R.id.message_user_name);
                image.setOnClickListener(this);

            }

            @Override
            public void onClick(View v){
                switch (v.getId()){
                    case R.id.message_image:
                        List<Picture> picture = new ArrayList<>();
                        picture.add(pic);
                        Intent intent = FullScreenImageStillFeedActivity.newIntent(getActivity(), (ArrayList<Picture>)picture, 0);
                        startActivity(intent);
                        break;
                }
            }

            public void bindMessage(User user, int position){
                message = user.getMessage().get(position);
                if (message.getmTag().equals(SnipperActivity.mActualUser.getmTag())){
                    name.setText(SnipperActivity.mActualUser.getUsername());
                    Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + SnipperActivity.mActualUser.getmProfileImagePath()).into(imageView);
                } else {
                    name.setText(user.getUsername());
                    Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + user.getmProfileImagePath()).into(imageView);
                }
                Glide.with(getContext()).load(SqlHTTP.BASE_URL_FETCH_SNIP + message.getPic().getImagePath()).apply(new RequestOptions().override(800,800).centerInside()).into(image);
                pic = message.getPic();
            }
        }


        @Override
        public int getItemViewType(int position) {
            String currentUID = profileUser.getMessage().get(position).getmTag();
            if (!(position+1 == profileUser.getMessage().size())){
                String nextUID = profileUser.getMessage().get(position+1).getmTag();
                if (position != 0 && profileUser.getMessage().get(position-1).getmTag().equals(currentUID)){
                    isStarting = false;
                } else {
                    isStarting = true;
                }
                Log.d("MFMFMF", "isStarting is " + String.valueOf(isStarting));
                if (!(profileUser.getMessage().get(position).getmContent().equals("*****null*****"))){
                    if ((!(nextUID.equals(currentUID)) && isStarting)){
                        Log.d("MFMFMF", "INSERTING MESSAGEHEADERTEXTDIVIDER");
                        return 3; //MessageHeaderTextDivider since only one text.
                    } else if (nextUID.equals(currentUID) && isStarting){
                        isStarting = false;
                        Log.d("MFMFMF", "INSERTING MESSAGEHEADER");
                        return 0; //MessageHeader
                    } else {
                        if (!(nextUID.equals(currentUID))){
                            isStarting = true;
                            Log.d("MFMFMF", "INSERTING MESSAGEDIVIDER");
                            return 2; //MessageDivider
                        } else {
                            Log.d("MFMFMF", "INSERTING MESSAGETEXT");
                            return 1; //MessageText
                        }
                    }
                } else {
                    if ((!(nextUID.equals(currentUID)) && isStarting)){
                        Log.d("MFMFMF", "INSERTING MESSAGEHEADERIMAGEDIVIDER");
                        return 7; //MessageHeaderImageDivider
                    } else if (nextUID.equals(currentUID) && isStarting){
                        isStarting = false;
                        Log.d("MFMFMF", "INSERTING MESSAGEIMAGEHEADER");
                        return 4; //MessageImageHeader
                    } else {
                        if (!(nextUID.equals(currentUID))){
                            isStarting = true;
                            Log.d("MFMFMF", "INSERTING MESSAGEIMAGEDIVIDER");
                            return 6; //MessageImageDivider
                        } else {
                            Log.d("MFMFMF", "INSERTING MESSAGEIMAGE");
                            return 5; //MessageImageText
                        }
                    }
                }
            } else {
                if (!(profileUser.getMessage().get(position).getmContent().equals("*****null*****"))){
                    if (isStarting){
                        Log.d("MFMFMF", "INSERTING LASTMESSAGEHEADER");
                        return 0; //MessageHeader
                    } else {
                        Log.d("MFMFMF", "INSERTING LASTMESSAGETEXT");
                        return 1; //MessageText
                    }
                } else {
                    if (isStarting){
                        Log.d("MFMFMF", "INSERTING LASTMESSAGEIMAGEHEADER");
                        return 4; //MessageImageHeader
                    } else {
                        Log.d("MFMFMF", "INSERTING LASTMESSAGEIMAGE");
                        return 5; //MessageImageText
                    }
                }
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType){
            View view;
            switch (viewType){
                case 0:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.message_list_header, parent, false);
                    return new MessageHeader(view);
                case 1:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.message_list_text, parent, false);
                    return new MessageText(view);
                case 2:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.message_list_divider, parent, false);
                    return new MessageDivider(view);
                case 3:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.message_list_header_text_divider, parent, false);
                    return new MessageHeaderTextDivider(view);
                case 4:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.message_image_header, parent, false);
                    return new MessageImageHeader(view);
                case 5:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.message_image, parent, false);
                    return new MessageImage(view);
                case 6:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.message_image_divider, parent, false);
                    return new MessageImageDivider(view);
                case 7:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.message_image_header_divider, parent, false);
                    return new MessageImageHeaderDivider(view);
                default:
                    Log.d("MFMFMF", "Not valid viewtype.");
                    return new MessageHeader(null);
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    MessageHeader messageHeader = (MessageHeader)holder;
                    messageHeader.bindMessage(profileUser, position);
                    break;
                case 1:
                    MessageText messageText = (MessageText)holder;
                    messageText.bindMessage(profileUser.getMessage().get(position));
                    break;
                case 2:
                    MessageDivider messageDivider = (MessageDivider)holder;
                    messageDivider.bindMessage(profileUser.getMessage().get(position));
                    break;
                case 3:
                    MessageHeaderTextDivider messageHeaderTextDivider = (MessageHeaderTextDivider)holder;
                    messageHeaderTextDivider.bindMessage(profileUser, position);
                    break;
                case 4:
                    MessageImageHeader messageImageHeader = (MessageImageHeader)holder;
                    messageImageHeader.bindMessage(profileUser, position);
                    break;
                case 5:
                    MessageImage messageImage = (MessageImage)holder;
                    messageImage.bindMessage(profileUser.getMessage().get(position));
                    break;
                case 6:
                    MessageImageDivider messageImageDivider = (MessageImageDivider)holder;
                    messageImageDivider.bindMessage(profileUser.getMessage().get(position));
                    break;
                case 7:
                    MessageImageHeaderDivider messageImageHeaderDivider = (MessageImageHeaderDivider)holder;
                    messageImageHeaderDivider.bindMessage(profileUser, position);
                    break;
            }
        }

        @Override
        public int getItemCount(){
            Log.d("MFMFMF", String.valueOf(profileUser.getMessage().size()));
            return profileUser.getMessage().size();
        }

        public void setUser(User user){ profileUser = user;}
    }

    private class FetchMessageTask extends AsyncTask<User, Void, Void> {
        @Override
        protected Void doInBackground(User... params) {
            Log.d("MFMFMF", "Starting up FMT");
            //params[0].setContext(getContext());
            new SqlHTTP().fetchMessages(params[0], SnipperActivity.mActualUser.getmuID());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUI();
                }
            });
            Log.d("MFMFMF", "Finishing up FMT");

            return null;
        }

    }

    private class SendMessageTask extends AsyncTask<List<String>, Void, Void> {
        @Override
        protected Void doInBackground(List<String>... params) {
            Log.d("MFMFMF", "Starting up SMT");
            new SqlHTTP().sendMessage(params[0].get(0), params[0].get(1), params[0].get(2), SnipperActivity.mActualUser.getmTag());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new FetchMessageTask().execute(ProfileFragment.mUser);
                }
            });
            Log.d("MFMFMF", "Finishing up SMT");

            return null;
        }

    }

}
