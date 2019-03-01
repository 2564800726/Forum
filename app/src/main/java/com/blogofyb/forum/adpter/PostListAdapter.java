package com.blogofyb.forum.adpter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;
import com.blogofyb.forum.utils.http.Post;
import com.blogofyb.forum.utils.img.ImageLoader;
import com.blogofyb.forum.utils.json.ToHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_INFORMATION = 0;
    private final int TYPE_TOP = 1;
    private final int TYPE_ANOTHER_NO_PIC = 2;
    private final int TYPE_ANOTHER_HAVE_PIC = 5;
    private final int SIGN_IN_SUCCESS = 3;
    private final int SIGN_IN_FAILED = 4;

    private List<PostBean> mPosts;
    private List<TopPostBean> mTopPosts;
    private List<PlateInformationBean> mPlateInformation;
    private String mAccount;
    private String mPassword;
    private boolean mHaveUser;
    private String mPlateId;

    private ImageLoader mImageLoader;
    private Button mSignIn;
    private Context mContext;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case SIGN_IN_SUCCESS:
                    mSignIn.setClickable(false);
                    mSignIn.setText("已签到");
                    break;
                case SIGN_IN_FAILED:
                    mSignIn.setClickable(true);
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
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        mHaveUser = sharedPreferences.getBoolean("haveUser", false);
        SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(mContext);
        Cursor cursor = database.query(SQLite.TABLE_NAME, new String[]{SQLite.ACCOUNT, SQLite.PASSWORD},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            mAccount = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
            mPassword = cursor.getString(cursor.getColumnIndex(SQLite.PASSWORD));
        }
        cursor.close();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_INFORMATION;
        } else if (position <= mTopPosts.size()) {
            return TYPE_TOP;
        } else {
            if ("".equals(mPosts.get(position - mTopPosts.size() - 1).getIcon()) ||
                    mPosts.get(position - mTopPosts.size() - 1).getIcon() == null) {
                return  TYPE_ANOTHER_NO_PIC;
            } else {
                return TYPE_ANOTHER_HAVE_PIC;
            }
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
        } else if (i == TYPE_ANOTHER_HAVE_PIC){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recommend_post_have_pic, viewGroup, false);
            return new AnotherPostsHavePicHolder(view);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recommend_post_no_pic, viewGroup, false);
            return new AnotherPostsNoPicHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (getItemViewType(i) == TYPE_INFORMATION) {
            PlateInformationHolder holder = (PlateInformationHolder) viewHolder;
            holder.mPlateName.setText(mPlateInformation.get(i).getName());

            mImageLoader.set(holder.mPlateIcon, mPlateInformation.get(i).getIcon());

            mSignIn = holder.mSignIn;
            mPlateId = mPlateInformation.get(i).getId();
            signIn(ServerInformation.CHECK_SIGN_IN);
            holder.mSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            signIn(ServerInformation.SIGN_IN);
                        }
                    }).start();
                }
            });
            if (!mHaveUser) {
                holder.mSearch.setClickable(false);
            }
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
        } else if (getItemViewType(i) == TYPE_ANOTHER_HAVE_PIC){
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
    }

    @Override
    public int getItemCount() {
        return mPlateInformation.size() + mPosts.size() + mTopPosts.size();
    }

    public void refreshData(List<PlateInformationBean> mPlateInformation, List<TopPostBean> mTopPosts, List<PostBean> mPosts) {
        this.mPlateInformation = mPlateInformation;
        this.mTopPosts = mTopPosts;
        this.mPosts = mPosts;
        notifyDataSetChanged();
    }

    private void signIn(String api) {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.PASSWORD, mPassword);
        body.put(Keys.ID, mPlateId);
        Post.sendHttpRequest(api, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                Message message = new Message();
                if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                    message.what = SIGN_IN_SUCCESS;
                    handler.sendMessage(message);
                } else {
                    onFailure(null);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what = SIGN_IN_FAILED;
                handler.sendMessage(message);
            }
        });
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

    // 加载中
    static class FooterHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;

        public FooterHolder(View view) {
            super(view);
            mProgressBar = view.findViewById(R.id.pb_loading);
        }
    }
}
