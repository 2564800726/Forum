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
import com.blogofyb.forum.activities.UserInformationActivity;
import com.blogofyb.forum.beans.UserBean;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.img.ImageLoader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserHolder> {
    private List<UserBean> mUsers;
    private Context mContext;

    private ImageLoader mImageLoader;

    public UserListAdapter(List<UserBean> mUsers, Context mContext) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        mImageLoader = new ImageLoader();
    }

    @NonNull
    @Override
    public UserListAdapter.UserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new UserHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subscribe_user, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserListAdapter.UserHolder userHolder, final int i) {
        final UserBean userBean = mUsers.get(i);
        setLevelBackground(userHolder.mUserLevel, userBean.getLevel());
        userHolder.mUserAge.setText(userBean.getAge());
        if ("male".equals(userBean.getGender())) {
            userHolder.mUserGender.setImageResource(R.drawable.male);
            userHolder.mUserGenderAge.setBackgroundResource(R.drawable.bg_male_age);
        } else {
            userHolder.mUserGender.setImageResource(R.drawable.female);
            userHolder.mUserGenderAge.setBackgroundResource(R.drawable.bg_female_age);
        }
        userHolder.mUserName.setText(userBean.getUserName());
        mImageLoader.set(userHolder.mUserHead, userBean.getHead());
        userHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserInformationActivity.class);
                intent.putExtra(Keys.ACCOUNT, userBean.getAccount());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
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

    public void refreshData(List<UserBean> mUsers) {
        this.mUsers = mUsers;
        notifyDataSetChanged();
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        private CircleImageView mUserHead;
        private TextView mUserName;
        private LinearLayout mUserGenderAge;
        private ImageView mUserGender;
        private TextView mUserAge;
        private TextView mUserLevel;

        public UserHolder(View view) {
            super(view);
            mUserHead = view.findViewById(R.id.civ_head);
            mUserName = view.findViewById(R.id.tv_post_author);
            mUserGenderAge = view.findViewById(R.id.ll_gender_age);
            mUserGender = view.findViewById(R.id.iv_user_gender);
            mUserAge = view.findViewById(R.id.tv_user_age);
            mUserLevel = view.findViewById(R.id.tv_user_level);
            view.findViewById(R.id.iv_is_author).setVisibility(View.GONE);
        }
    }
}
