package com.blogofyb.forum.adpter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogofyb.forum.R;
import com.blogofyb.forum.activities.CommentActivity;
import com.blogofyb.forum.activities.SelectActivity;
import com.blogofyb.forum.activities.UserInformationActivity;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.img.ImageLoader;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapterHomePage extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_CONTENT = 0;
    private final int TYPE_TO_AUTHOR = 1;
    private final int TYPE_TO_ANOTHER = 2;

    private List<HashMap<String, String>> data;
    private Context mContext;
    private ImageLoader mImageLoader;
    private boolean mHaveUser;

    public PostAdapterHomePage(Context mContext, List<HashMap<String, String>> data) {
        this.data = data;
        this.mContext = mContext;
        mImageLoader = new ImageLoader(mContext);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        mHaveUser = sharedPreferences.getBoolean("haveUser", false);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_CONTENT;
        } else if (!"".equals(data.get(position).get(Keys.ANOTHER_USER_NAME))
                && data.get(position).get(Keys.ANOTHER_USER_NAME) != null) {
            return TYPE_TO_ANOTHER;
        } else {
            return TYPE_TO_AUTHOR;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == TYPE_CONTENT) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_content, viewGroup, false);
            return new ContentHolder(view);
        } else if (i == TYPE_TO_ANOTHER) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.to_another, viewGroup, false);
            return new ToAnotherHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.to_author, viewGroup, false);
            return new ToAuthorHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof ContentHolder) {
            ContentHolder holder = (ContentHolder) viewHolder;
            final HashMap<String, String> content = data.get(i);
            holder.mContent.setText(content.get(Keys.POST_CONTENT));
            holder.mPraise.setText(content.get(Keys.PRAISE));
            holder.mUserName.setText(content.get(Keys.USER_NAME));
            holder.mAge.setText(content.get(Keys.AGE));
            if ("true".equals(content.get(Keys.IS_AUTHOR))) {
                holder.mIsAuthor.setImageResource(R.drawable.author);
            } else {
                holder.mIsAuthor.setVisibility(View.GONE);
            }
            setLevelBackground(holder.mLevel, content.get(Keys.LEVEL));
            if ("male".equals(content.get(Keys.GENDER))) {
                holder.mGender.setImageResource(R.drawable.male);
                holder.mGenderAge.setBackgroundResource(R.drawable.bg_male_age);
            } else {
                holder.mGender.setImageResource(R.drawable.female);
                holder.mGenderAge.setBackgroundResource(R.drawable.bg_female_age);
            }
            if (mHaveUser) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, CommentActivity.class);
                        intent.putExtra(Keys.ANOTHER_CONTENT, content.get(Keys.POST_CONTENT));
                        intent.putExtra(Keys.ANOTHER_USER_NAME, content.get(Keys.USER_NAME));
                        intent.putExtra(Keys.FLOOR, "0");
                        mContext.startActivity(intent);
                    }
                });
            } else {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SelectActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            }
            holder.mHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, UserInformationActivity.class);
                    intent.putExtra(Keys.ACCOUNT, content.get(Keys.ACCOUNT));
                    mContext.startActivity(intent);
                }
            });
            mImageLoader.set(holder.mHead, content.get(Keys.HEAD));
        } else if (viewHolder instanceof ToAnotherHolder) {
            ToAnotherHolder holder = (ToAnotherHolder) viewHolder;
            final HashMap<String, String> anotherContent = data.get(i);
            holder.mAnotherUserName.setText(anotherContent.get(Keys.ANOTHER_USER_NAME));
            holder.mAnotherContent.setText(anotherContent.get(Keys.ANOTHER_CONTENT));
            holder.mAge.setText(anotherContent.get(Keys.AGE));
            holder.mContent.setText(anotherContent.get(Keys.POST_CONTENT));
            holder.mFloor.setText("#" + anotherContent.get(Keys.FLOOR));
            setLevelBackground(holder.mLevel, anotherContent.get(Keys.LEVEL));
            holder.mUserName.setText(anotherContent.get(Keys.USER_NAME));
            if ("male".equals(anotherContent.get(Keys.GENDER))) {
                holder.mGender.setImageResource(R.drawable.male);
                holder.mGenderAge.setBackgroundResource(R.drawable.bg_male_age);
            } else {
                holder.mGender.setImageResource(R.drawable.female);
                holder.mGenderAge.setBackgroundResource(R.drawable.bg_female_age);
            }
            if ("true".equals(anotherContent.get(Keys.IS_AUTHOR))) {
                holder.mIsAuthor.setImageResource(R.drawable.author);
            } else {
                holder.mIsAuthor.setVisibility(View.GONE);
            }
            if (mHaveUser) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, CommentActivity.class);
                        intent.putExtra(Keys.ANOTHER_CONTENT, anotherContent.get(Keys.POST_CONTENT));
                        intent.putExtra(Keys.ANOTHER_USER_NAME, anotherContent.get(Keys.USER_NAME));
                        intent.putExtra(Keys.FLOOR, anotherContent.get(Keys.FLOOR));
                        mContext.startActivity(intent);
                    }
                });
            } else {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SelectActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            }
            holder.mHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, UserInformationActivity.class);
                    intent.putExtra(Keys.ACCOUNT, anotherContent.get(Keys.ACCOUNT));
                    mContext.startActivity(intent);
                }
            });
            mImageLoader.set(holder.mHead, anotherContent.get(Keys.HEAD));
        } else {
            ToAuthorHolder holder = (ToAuthorHolder) viewHolder;

            final HashMap<String, String> authorContent = data.get(i);
            holder.mFloor.setText("#" + authorContent.get(Keys.FLOOR));
            holder.mUserName.setText(authorContent.get(Keys.USER_NAME));
            setLevelBackground(holder.mLevel, authorContent.get(Keys.LEVEL));
            holder.mAge.setText(authorContent.get(Keys.AGE));
            holder.mContent.setText(authorContent.get(Keys.POST_CONTENT));
            if ("male".equals(authorContent.get(Keys.GENDER))) {
                holder.mGender.setImageResource(R.drawable.male);
                holder.mGenderAge.setBackgroundResource(R.drawable.bg_male_age);
            } else {
                holder.mGender.setImageResource(R.drawable.female);
                holder.mGenderAge.setBackgroundResource(R.drawable.bg_female_age);
            }
            if ("true".equals(authorContent.get(Keys.IS_AUTHOR))) {
                holder.mIsAuthor.setImageResource(R.drawable.author);
            } else {
                holder.mIsAuthor.setVisibility(View.GONE);
            }
            if (mHaveUser) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, CommentActivity.class);
                        intent.putExtra(Keys.ANOTHER_CONTENT, authorContent.get(Keys.POST_CONTENT));
                        intent.putExtra(Keys.ANOTHER_USER_NAME, authorContent.get(Keys.USER_NAME));
                        intent.putExtra(Keys.FLOOR, authorContent.get(Keys.FLOOR));
                        mContext.startActivity(intent);
                    }
                });
            } else {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SelectActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            }
            holder.mHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, UserInformationActivity.class);
                    intent.putExtra(Keys.ACCOUNT, authorContent.get(Keys.ACCOUNT));
                    mContext.startActivity(intent);
                }
            });
            mImageLoader.set(holder.mHead, authorContent.get(Keys.HEAD));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void setLevelBackground(TextView mLevel, String level) {
        switch (level) {
            case "1":
                mLevel.setBackgroundResource(R.drawable.bg_level_1);
                mLevel.setText(R.string.level_1);
                break;
            case "2":
                mLevel.setBackgroundResource(R.drawable.bg_level_2);
                mLevel.setText(R.string.level_2);
                break;
            case "3":
                mLevel.setBackgroundResource(R.drawable.bg_level_3);
                mLevel.setText(R.string.level_3);
                break;
            case "4":
                mLevel.setBackgroundResource(R.drawable.bg_level_4);
                mLevel.setText(R.string.level_4);
                break;
            case "5":
                mLevel.setBackgroundResource(R.drawable.bg_level_5);
                mLevel.setText(R.string.level_5);
                break;
            case "6":
                mLevel.setBackgroundResource(R.drawable.bg_level_6);
                mLevel.setText(R.string.level_6);
                break;
        }
    }

    public void refreshData(List<HashMap<String, String>> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    static class ContentHolder extends RecyclerView.ViewHolder {
        private CircleImageView mHead;
        private TextView mUserName;
        private ImageView mGender;
        private TextView mAge;
        private TextView mLevel;
        private ImageView mIsAuthor;
        private TextView mPraise;
        private TextView mContent;
        private LinearLayout mGenderAge;

        public ContentHolder(View view) {
            super(view);
            mHead = view.findViewById(R.id.civ_head);
            mUserName = view.findViewById(R.id.tv_post_author);
            mGender = view.findViewById(R.id.iv_user_gender);
            mAge = view.findViewById(R.id.tv_user_age);
            mLevel = view.findViewById(R.id.tv_user_level);
            mIsAuthor = view.findViewById(R.id.iv_is_author);
            mPraise = view.findViewById(R.id.tv_praise);
            mContent = view.findViewById(R.id.tv_post_content);
            mGenderAge = view.findViewById(R.id.ll_gender_age);
        }
    }

    static class ToAuthorHolder extends RecyclerView.ViewHolder {
        private CircleImageView mHead;
        private TextView mUserName;
        private ImageView mGender;
        private TextView mAge;
        private TextView mLevel;
        private ImageView mIsAuthor;
        private TextView mFloor;
        private TextView mContent;
        private LinearLayout mGenderAge;

        public ToAuthorHolder(View view) {
            super(view);
            mHead = view.findViewById(R.id.civ_head);
            mUserName = view.findViewById(R.id.tv_post_author);
            mGender = view.findViewById(R.id.iv_user_gender);
            mAge = view.findViewById(R.id.tv_user_age);
            mLevel = view.findViewById(R.id.tv_user_level);
            mIsAuthor = view.findViewById(R.id.iv_is_author);
            mFloor = view.findViewById(R.id.tv_floor);
            mContent = view.findViewById(R.id.tv_content);
            mGenderAge = view.findViewById(R.id.ll_gender_age);
        }
    }

    static class ToAnotherHolder extends RecyclerView.ViewHolder {
        private TextView mAnotherUserName;
        private TextView mAnotherContent;
        private CircleImageView mHead;
        private TextView mUserName;
        private ImageView mGender;
        private TextView mAge;
        private TextView mLevel;
        private ImageView mIsAuthor;
        private TextView mFloor;
        private TextView mContent;
        private LinearLayout mGenderAge;

        public ToAnotherHolder(View view) {
            super(view);
            mAnotherUserName = view.findViewById(R.id.tv_another_user_name);
            mAnotherContent = view.findViewById(R.id.tv_another_content);
            mHead = view.findViewById(R.id.civ_head);
            mUserName = view.findViewById(R.id.tv_post_author);
            mGender = view.findViewById(R.id.iv_user_gender);
            mAge = view.findViewById(R.id.tv_user_age);
            mLevel = view.findViewById(R.id.tv_user_level);
            mIsAuthor = view.findViewById(R.id.iv_is_author);
            mFloor = view.findViewById(R.id.tv_floor);
            mContent = view.findViewById(R.id.tv_content);
            mGenderAge = view.findViewById(R.id.ll_gender_age);
        }
    }
}
