package com.blogofyb.forum.adpter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.activities.PostActivity;
import com.blogofyb.forum.beans.CommentBean;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.http.Get;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyResponseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_RESPONSE = 0;
    private final int TYPE_FOOTER = 1;
    private final int LOAD_BEAN_SUCCESS = 2;
    private final int LOAD_BEAN_FAILED = 3;

    private List<CommentBean> mComments;
    private Context mContext;
    private int mIndex = 0;
    private String mAccount;
    private ProgressBar mLoading;
    private boolean mHaveMore = true;
    private boolean mIsLoading = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case LOAD_BEAN_FAILED:
                    mIsLoading = false;
                    if (mLoading != null) {
                        mLoading.setVisibility(View.GONE);
                    }
                    Toast.makeText(mContext, "加载失败", Toast.LENGTH_SHORT).show();
                    break;
                case LOAD_BEAN_SUCCESS:
                    mIsLoading = false;
                    if (mLoading != null) {
                        mLoading.setVisibility(View.GONE);
                    }
                    notifyDataSetChanged();
                    break;
            }
        }
    };

    public MyResponseAdapter(Context mContext, List<CommentBean> mComments, String mAccount) {
        this.mComments = mComments;
        this.mContext = mContext;
        this.mAccount = mAccount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mComments.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_RESPONSE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (i == TYPE_RESPONSE) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_comment, viewGroup, false);
            return new CommentHolder(view);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loading, viewGroup, false);
            return new FooterHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (getItemCount() > 30 && i == getItemCount() - 1 && mHaveMore && !mIsLoading) {
            mIsLoading = true;
            mIndex++;
            loadBean();
        }
        if (getItemViewType(i) == TYPE_RESPONSE) {
            CommentHolder holder = (CommentHolder) viewHolder;
            final CommentBean commentBean = mComments.get(i);
            holder.mDate.setText(commentBean.getDate());
            holder.mContent.setText(commentBean.getContent());
            holder.mSourcePlateName.setText(commentBean.getPlateName());
            holder.mSourcePostTitle.setText(commentBean.getPostTitle());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PostActivity.class);
                    intent.putExtra("id", commentBean.getPostId());
                    intent.putExtra("title", commentBean.getPostTitle());
                    intent.putExtra(Keys.POST_AUTHOR, commentBean.getUserName());
                    intent.putExtra(Keys.POST_CONTENT, commentBean.getDescription());
                    mContext.startActivity(intent);
                }
            });
        } else {
            FooterHolder holder = (FooterHolder) viewHolder;
            mLoading = holder.mLoading;
            if (getItemCount() < 31) {
                mLoading.setVisibility(View.GONE);
            }
            if (!mIsLoading) {
                mLoading.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mComments.size() + 1;
    }

    private void loadBean() {
        Get.sendHttpRequest(ServerInformation.MY_RESPONSE + mAccount + "&index=" + mIndex, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    mHaveMore = ServerInformation.SUCCESS.equals(jsonObject.getString(Keys.STATUS));
                    JSONArray jsonArray = jsonObject.getJSONArray(Keys.RETURN_DATA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        CommentBean commentBean = new CommentBean();
                        commentBean.setContent(object.getString(Keys.POST_CONTENT));
                        commentBean.setDate(object.getString(Keys.POST_DATE));
                        commentBean.setPlateName(object.getString(Keys.PLATE_NAME));
                        commentBean.setPostId(object.getString(Keys.ID));
                        commentBean.setPostTitle(object.getString(Keys.POST_TITLE));
                        commentBean.setTime(object.getString(Keys.TIME));
                        commentBean.setDescription(object.getString(Keys.POST_DESCRIPTION));
                        commentBean.setUserName(object.getString(Keys.POST_AUTHOR));
                        mComments.add(commentBean);
                    }
                    Message message = new Message();
                    message.what = LOAD_BEAN_SUCCESS;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = LOAD_BEAN_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    public void refreshData(List<CommentBean> mComments) {
        mIndex = 0;
        mHaveMore = true;
        this.mComments = mComments;
        notifyDataSetChanged();
    }

    static class CommentHolder extends RecyclerView.ViewHolder {
        private TextView mDate;
        private TextView mContent;
        private TextView mSourcePostTitle;
        private TextView mSourcePlateName;

        public CommentHolder(View view) {
            super(view);
            mDate = view.findViewById(R.id.tv_my_comment_time);
            mContent = view.findViewById(R.id.tv_my_comment);
            mSourcePostTitle = view.findViewById(R.id.tv_source_post_title);
            mSourcePlateName = view.findViewById(R.id.tv_source_plate_name);
        }
    }

    static class FooterHolder extends RecyclerView.ViewHolder {
        private ProgressBar mLoading;

        public FooterHolder(View view) {
            super(view);
            mLoading = view.findViewById(R.id.pb_loading);
        }
    }
}
