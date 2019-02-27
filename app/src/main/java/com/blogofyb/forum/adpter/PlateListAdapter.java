package com.blogofyb.forum.adpter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogofyb.forum.R;
import com.blogofyb.forum.activities.PlateActivity;
import com.blogofyb.forum.activities.PostActivity;

import com.blogofyb.forum.beans.*;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.img.ImageLoader;

import java.util.List;

public class PlateListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_POST = 0;
    private final int TYPE_PLATE = 1;

    private List<PlateBean> mPlates;
    private List<PostBean> mRecommendPost;
    private Context mContext;
    private ImageLoader mImageLoader;

    public PlateListAdapter(List<PlateBean> mPlates, List<PostBean> mRecommendPost, Context mContext) {
        this.mPlates = mPlates;
        this.mRecommendPost = mRecommendPost;
        this.mContext = mContext;
        mImageLoader = new ImageLoader(mContext);
    }

    @Override
    public int getItemViewType(int i) {
        return i == 0 ? TYPE_POST : TYPE_PLATE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == TYPE_POST) {
            if ("".equals(mRecommendPost.get(0).getIcon()) || mRecommendPost.get(0).getIcon() == null) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recommend_post_no_pic, viewGroup, false);
                return new RecommendPostNoPicHolder(view);
            } else {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recommend_post_have_pic, viewGroup, false);
                return new RecommendPostHavePicHolder(view);
            }
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plate, viewGroup, false);
            return new PlateListHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (getItemViewType(i) == TYPE_POST) {
            if (viewHolder instanceof RecommendPostHavePicHolder) {
                final RecommendPostHavePicHolder holder = (RecommendPostHavePicHolder) viewHolder;
                final PostBean recommendPost = mRecommendPost.get(i);
                holder.mPostTitle.setText(recommendPost.getTitle());
                holder.mPostDescription.setText(recommendPost.getDescription());
                holder.mPostAuthor.setText(recommendPost.getAuthor());
                holder.mPostVisit.setText(recommendPost.getVisit());
                holder.mPostDiscuss.setText(recommendPost.getDiscuss());
                holder.mPostEditDate.setText(recommendPost.getDate());

//                holder.mPostIcon.setTag(recommendPost.getIcon());
                mImageLoader.set(holder.mPostIcon, recommendPost.getIcon());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, PostActivity.class);
                        intent.putExtra("id", recommendPost.getId());
                        intent.putExtra("title", recommendPost.getTitle());
                        intent.putExtra(Keys.POST_AUTHOR, recommendPost.getAuthor());
                        intent.putExtra(Keys.POST_CONTENT, recommendPost.getDescription());
                        mContext.startActivity(intent);
                    }
                });
            } else {
                final RecommendPostNoPicHolder holder = (RecommendPostNoPicHolder) viewHolder;
                final PostBean recommendPost = mRecommendPost.get(i);
                holder.mPostTitle.setText(recommendPost.getTitle());
                holder.mPostDescription.setText(recommendPost.getDescription());
                holder.mPostAuthor.setText(recommendPost.getAuthor());
                holder.mPostVisit.setText(recommendPost.getVisit());
                holder.mPostDiscuss.setText(recommendPost.getDiscuss());
                holder.mPostEditDate.setText(recommendPost.getDate());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, PostActivity.class);
                        intent.putExtra("id", recommendPost.getId());
                        intent.putExtra("title", recommendPost.getTitle());
                        intent.putExtra(Keys.POST_AUTHOR, recommendPost.getAuthor());
                        intent.putExtra(Keys.POST_CONTENT, recommendPost.getDescription());
                        mContext.startActivity(intent);
                    }
                });
            }
        } else {
            PlateListHolder holder = (PlateListHolder) viewHolder;
            final PlateBean plateBeanLeft = mPlates.get(2 * (i - 1));
            if (plateBeanLeft != null) {
                holder.mPlateNameLeft.setText(plateBeanLeft.getPlateName());
                mImageLoader.set(holder.mPlateIconLeft, plateBeanLeft.getIcon());
                LinearLayout linearLayoutLeft = holder.itemView.findViewById(R.id.ll_plate_left);
                linearLayoutLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, PlateActivity.class);
                        intent.putExtra("id", plateBeanLeft.getId());
                        mContext.startActivity(intent);
                    }
                });
            }
            int index = 2 * (i -1) + 1;
            if (index < mPlates.size()) {
                final PlateBean plateBeanRight = mPlates.get(index);
                if (plateBeanRight != null) {
                    holder.mPlateNAmeRight.setText(plateBeanRight.getPlateName());
                    mImageLoader.set(holder.mPLateIconRight, plateBeanRight.getIcon());
                    LinearLayout linearLayout = holder.itemView.findViewById(R.id.ll_plate_right);
                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, PlateActivity.class);
                            intent.putExtra("id", plateBeanRight.getId());
                            mContext.startActivity(intent);
                        }
                    });
                }
            } else {
                holder.mPlateNAmeRight.setText("");
                holder.mPLateIconRight.setImageBitmap(null);
                LinearLayout linearLayout = holder.itemView.findViewById(R.id.ll_plate_right);
                linearLayout.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mPlates.size() % 2 == 0) {
            return mPlates.size() / 2 + 1;
        } else {
            return mPlates.size() / 2 + 2;
        }
    }

    public void refreshData(List<PlateBean> mPlates, List<PostBean> mRecommendPost) {
        this.mPlates = mPlates;
        this.mRecommendPost = mRecommendPost;
        notifyDataSetChanged();
    }

    static class PlateListHolder extends RecyclerView.ViewHolder {
        private ImageView mPlateIconLeft;
        private TextView mPlateNameLeft;

        private ImageView mPLateIconRight;
        private TextView mPlateNAmeRight;

        public PlateListHolder(@NonNull View itemView) {
            super(itemView);
            mPlateIconLeft = itemView.findViewById(R.id.iv_plate_icon_left);
            mPlateNameLeft = itemView.findViewById(R.id.tv_plate_name_left);
            mPLateIconRight = itemView.findViewById(R.id.iv_plate_icon_right);
            mPlateNAmeRight = itemView.findViewById(R.id.tv_plate_name_right);
        }
    }

    static class RecommendPostHavePicHolder extends RecyclerView.ViewHolder {
        private ImageView mPostIcon;

        private TextView mPostTitle;
        private TextView mPostDescription;
        private TextView mPostAuthor;
        private TextView mPostVisit;
        private TextView mPostDiscuss;
        private TextView mPostEditDate;

        public RecommendPostHavePicHolder(@NonNull View itemView) {
            super(itemView);
            mPostIcon = itemView.findViewById(R.id.iv_post_have_pic);
            mPostTitle = itemView.findViewById(R.id.tv_post_title);
            mPostDescription = itemView.findViewById(R.id.tv_post_description);
            mPostAuthor = itemView.findViewById(R.id.tv_post_author);
            mPostVisit = itemView.findViewById(R.id.tv_post_visit);
            mPostDiscuss = itemView.findViewById(R.id.tv_post_discuss);
            mPostEditDate = itemView.findViewById(R.id.tv_post_edit_date);
        }
    }

    static class RecommendPostNoPicHolder extends RecyclerView.ViewHolder {
        private TextView mPostTitle;
        private TextView mPostDescription;
        private TextView mPostAuthor;
        private TextView mPostVisit;
        private TextView mPostDiscuss;
        private TextView mPostEditDate;

        public RecommendPostNoPicHolder(View view) {
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
