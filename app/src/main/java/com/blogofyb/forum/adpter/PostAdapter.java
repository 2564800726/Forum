package com.blogofyb.forum.adpter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogofyb.forum.R;
import com.blogofyb.forum.activities.PostActivity;
import com.blogofyb.forum.beans.PostBean;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.img.ImageLoader;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_HAVE_PIC = 0;
    private final int TYPE_NO_PIC = 1;

    private List<PostBean> mPosts;
    private Context mContext;
    private ImageLoader mImageLoader;

    public PostAdapter(List<PostBean> mPosts, Context mContext) {
        this.mPosts = mPosts;
        this.mContext = mContext;
        mImageLoader = new ImageLoader();
    }

    @Override
    public int getItemViewType(int position) {
        if (mPosts.get(position).getIcon() == null || "".equals(mPosts.get(position).getIcon())) {
            return TYPE_NO_PIC;
        } else {
            return TYPE_HAVE_PIC;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (i == TYPE_HAVE_PIC) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recommend_post_have_pic, viewGroup, false);
            return new PostHavePicHolder(view);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recommend_post_no_pic, viewGroup, false);
            return new PostNoPicHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (getItemViewType(i) == TYPE_HAVE_PIC) {
            PostHavePicHolder holder = (PostHavePicHolder) viewHolder;
            final PostBean postBean = mPosts.get(i);
            holder.mPostTitle.setText(postBean.getTitle());
            holder.mPostDescription.setText(postBean.getDescription());
            holder.mPostAuthor.setText(postBean.getAuthor());
            holder.mPostVisit.setText(postBean.getVisit());
            holder.mPostDiscuss.setText(postBean.getDiscuss());
            holder.mPostEditDate.setText(postBean.getDate());
            mImageLoader.set(holder.mPostIcon, postBean.getIcon());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PostActivity.class);
                    intent.putExtra("id", postBean.getId());
                    intent.putExtra("title", postBean.getTitle());
                    intent.putExtra(Keys.POST_AUTHOR, postBean.getAuthor());
                    intent.putExtra(Keys.POST_CONTENT, postBean.getDescription());
                    mContext.startActivity(intent);
                }
            });
        } else {
            PostNoPicHolder holder = (PostNoPicHolder) viewHolder;
            final PostBean postBean = mPosts.get(i);
            holder.mPostTitle.setText(postBean.getTitle());
            holder.mPostDescription.setText(postBean.getDescription());
            holder.mPostAuthor.setText(postBean.getAuthor());
            holder.mPostVisit.setText(postBean.getVisit());
            holder.mPostDiscuss.setText(postBean.getDiscuss());
            holder.mPostEditDate.setText(postBean.getDate());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PostActivity.class);
                    intent.putExtra("id", postBean.getId());
                    intent.putExtra("title", postBean.getTitle());
                    intent.putExtra(Keys.POST_AUTHOR, postBean.getAuthor());
                    intent.putExtra(Keys.POST_CONTENT, postBean.getDescription());
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public void refreshData(List<PostBean> mPosts) {
        this.mPosts = mPosts;
        notifyDataSetChanged();
    }

    static class PostHavePicHolder extends RecyclerView.ViewHolder {
        private ImageView mPostIcon;

        private TextView mPostTitle;
        private TextView mPostDescription;
        private TextView mPostAuthor;
        private TextView mPostVisit;
        private TextView mPostDiscuss;
        private TextView mPostEditDate;

        public PostHavePicHolder(View view) {
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

    static class PostNoPicHolder extends RecyclerView.ViewHolder {
        private TextView mPostTitle;
        private TextView mPostDescription;
        private TextView mPostAuthor;
        private TextView mPostVisit;
        private TextView mPostDiscuss;
        private TextView mPostEditDate;

        public PostNoPicHolder(View view) {
            super(view);
            mPostTitle = itemView.findViewById(R.id.tv_post_title);
            mPostDescription = itemView.findViewById(R.id.tv_post_description);
            mPostAuthor = itemView.findViewById(R.id.tv_post_author);
            mPostVisit = itemView.findViewById(R.id.tv_post_visit);
            mPostDiscuss = itemView.findViewById(R.id.tv_post_discuss);
            mPostEditDate = itemView.findViewById(R.id.tv_post_edit_date);
        }
    }
}
