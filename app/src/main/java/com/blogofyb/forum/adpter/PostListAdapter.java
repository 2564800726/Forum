package com.blogofyb.forum.adpter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blogofyb.forum.R;
import com.blogofyb.forum.activities.PostActivity;
import com.blogofyb.forum.activities.SearchActivity;
import com.blogofyb.forum.beans.PlateInformationBean;
import com.blogofyb.forum.beans.PostBean;
import com.blogofyb.forum.beans.TopPostBean;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.http.Post;
import com.blogofyb.forum.utils.img.ImageLoader;
import com.blogofyb.forum.utils.json.ToHashMap;

import java.util.HashMap;
import java.util.List;

public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_INFORMATION = 0;
    private final int TYPE_TOP = 1;
    private final int TYPE_ANOTHER = 2;
    private final int TYPE_END = 5;
    private final int SIGN_IN_SUCCESS = 3;
    private final int SIGN_IN_FAILED = 4;

    private List<PostBean> mPosts;
    private List<TopPostBean> mTopPosts;
    private List<PlateInformationBean> mPlateInformation;
    private String mAccount;

    private ImageLoader mImageLoader;
    private Button mSignIn;
    private Context mContext;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case SIGN_IN_SUCCESS:
                    mSignIn.setText("已签到");
                    break;
                case SIGN_IN_FAILED:
                    mSignIn.setText("签到");
                    break;
            }
        }
    };

    public PostListAdapter(List<PostBean> mPosts, List<TopPostBean> mTopPosts,
                           List<PlateInformationBean> mPlateInformation, Context mContext) {
        this.mPosts = mPosts;
        this.mTopPosts = mTopPosts;
        this.mPlateInformation = mPlateInformation;
        mImageLoader = new ImageLoader(mContext);
        this.mContext = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_INFORMATION;
        } else if (position < mTopPosts.size() + 1) {
            return TYPE_TOP;
        } else if (position < mPosts.size() + mTopPosts.size() + 1){
            return TYPE_ANOTHER;
        } else {
            return TYPE_END;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (i == TYPE_INFORMATION) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plate_information, viewGroup, false);
            return new PlateInformationHolder(view);
        } else if (i == TYPE_TOP) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.top, viewGroup, false);
            return new TopPostHolder(view);
        } else if (i == TYPE_ANOTHER){
            if ("".equals(mPosts.get(0).getIcon()) || mPosts.get(0).getIcon() == null) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recommend_post_no_pic, viewGroup, false);
                return new AnotherPostsNoPicHolder(view);
            } else {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recommend_post_have_pic, viewGroup, false);
                return new AnotherPostsHavePicHolder(view);
            }
        } else {
            // 上拉刷新
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progress_bar, viewGroup, false);
            return new LoadingHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (getItemViewType(i) == TYPE_INFORMATION) {
            PlateInformationHolder holder = (PlateInformationHolder) viewHolder;
            holder.mPlateName.setText(mPlateInformation.get(i).getName());

            mImageLoader.set(holder.mPlateIcon, mPlateInformation.get(i).getIcon());

            mSignIn = holder.mSignIn;
            holder.mSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String, String> body = new HashMap<>();
                            body.put(Keys.ACCOUNT, mAccount);
                            Post.sendHttpRequest(ServerInformation.SIGN_IN, body, new HttpCallbackListener() {
                                @Override
                                public void onFinish(String response) {
                                    HashMap returnData = ToHashMap.getInstance().transform(response);
                                    Message message = new Message();
                                    if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                                        message.what = SIGN_IN_SUCCESS;
                                        handler.sendMessage(message);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Message message = new Message();
                                    message.what = SIGN_IN_FAILED;
                                    handler.sendMessage(message);
                                }
                            });
                        }
                    }).start();
                }
            });
            holder.mSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SearchActivity.class);
                    mContext.startActivity(intent);
                }
            });
        } else if (getItemViewType(i) == TYPE_TOP) {
            TopPostHolder holder = (TopPostHolder) viewHolder;
            holder.mTopTitle.setText(mTopPosts.get(i - 1).getTitle());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PostActivity.class);
                    intent.putExtra(Keys.ID, mTopPosts.get(i - 1).getId());
                    mContext.startActivity(intent);
                }
            });
        } else if (getItemViewType(i) == TYPE_ANOTHER){
            if (viewHolder instanceof AnotherPostsHavePicHolder) {
                final AnotherPostsHavePicHolder holder = (AnotherPostsHavePicHolder) viewHolder;
                final PostBean postBean = mPosts.get(i - mTopPosts.size() - 1);
                holder.mPostTitle.setText(postBean.getTitle());
                holder.mPostDescription.setText(postBean.getDescription());
                holder.mPostAuthor.setText(postBean.getAuthor());
                holder.mPostVisit.setText(postBean.getVisit());
                holder.mPostDiscuss.setText(postBean.getDiscuss());
                holder.mPostEditDate.setText(postBean.getDate());

                mImageLoader.set(holder.mPostIcon, postBean.getIcon());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, PostActivity.class);
                        intent.putExtra(Keys.ID, postBean.getId());
                        mContext.startActivity(intent);
                    }
                });
            } else {
                final AnotherPostsNoPicHolder holder = (AnotherPostsNoPicHolder) viewHolder;
                final PostBean postBean = mPosts.get(i - mTopPosts.size() - 1);
                holder.mPostTitle.setText(postBean.getTitle());
                holder.mPostDescription.setText(postBean.getDescription());
                holder.mPostAuthor.setText(postBean.getAuthor());
                holder.mPostVisit.setText(postBean.getVisit());
                holder.mPostDiscuss.setText(postBean.getDiscuss());
                holder.mPostEditDate.setText(postBean.getDate());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, PostActivity.class);
                        intent.putExtra(Keys.ID, postBean.getId());
                        mContext.startActivity(intent);
                    }
                });
            }
        } else {
            LoadingHolder holder = (LoadingHolder) viewHolder;

        }
    }

    @Override
    public int getItemCount() {
        return mPlateInformation.size() + mPosts.size() + mTopPosts.size() + 1;
    }

    public void refreshData(List<PlateInformationBean> mPlateInformation, List<TopPostBean> mTopPosts, List<PostBean> mPosts) {
        this.mPlateInformation = mPlateInformation;
        this.mTopPosts = mTopPosts;
        this.mPosts = mPosts;
        notifyDataSetChanged();
    }

    // 板块的基本信息
    static class PlateInformationHolder extends RecyclerView.ViewHolder {
        private ImageView mPlateIcon;
        private TextView mPlateName;
        private Button mSignIn;
        private Button mSearch;

        public PlateInformationHolder(View view) {
            super(view);
            mPlateIcon = view.findViewById(R.id.iv_plate_icon);
            mPlateName = view.findViewById(R.id.tv_plate_name);
            mSignIn = view.findViewById(R.id.btn_sign_in);
            mSearch = view.findViewById(R.id.btn_search);
        }
    }

    // 置顶
    static class TopPostHolder extends RecyclerView.ViewHolder {
        private TextView mTopTitle;

        public TopPostHolder(View view) {
            super(view);
            mTopTitle = view.findViewById(R.id.tv_top_post_title);
        }
    }

    // 一般帖子(有图)
    static class AnotherPostsHavePicHolder extends RecyclerView.ViewHolder {
        private ImageView mPostIcon;

        private TextView mPostTitle;
        private TextView mPostDescription;
        private TextView mPostAuthor;
        private TextView mPostVisit;
        private TextView mPostDiscuss;
        private TextView mPostEditDate;

        public AnotherPostsHavePicHolder(View view) {
            super(view);
            mPostIcon = itemView.findViewById(R.id.iv_post_have_pic);
            mPostTitle = itemView.findViewById(R.id.tv_post_title);
            mPostDescription = itemView.findViewById(R.id.tv_post_description);
            mPostAuthor = itemView.findViewById(R.id.tv_post_author);
            mPostVisit = itemView.findViewById(R.id.tv_post_visit);
            mPostDiscuss = itemView.findViewById(R.id.tv_post_discuss);
            mPostEditDate = itemView.findViewById(R.id.tv_post_edit_date);
        }
    }

    // 一般帖子(无图)
    static class AnotherPostsNoPicHolder extends RecyclerView.ViewHolder {
        private TextView mPostTitle;
        private TextView mPostDescription;
        private TextView mPostAuthor;
        private TextView mPostVisit;
        private TextView mPostDiscuss;
        private TextView mPostEditDate;

        public AnotherPostsNoPicHolder(View view) {
            super(view);
            mPostTitle = itemView.findViewById(R.id.tv_post_title);
            mPostDescription = itemView.findViewById(R.id.tv_post_description);
            mPostAuthor = itemView.findViewById(R.id.tv_post_author);
            mPostVisit = itemView.findViewById(R.id.tv_post_visit);
            mPostDiscuss = itemView.findViewById(R.id.tv_post_discuss);
            mPostEditDate = itemView.findViewById(R.id.tv_post_edit_date);
        }
    }

    // 加载的进度条
    static class LoadingHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;

        public LoadingHolder(View view) {
            super(view);
            mProgressBar = view.findViewById(R.id.pb_loading);
        }
    }
}
