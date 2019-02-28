package com.blogofyb.forum.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.activities.FansActivity;
import com.blogofyb.forum.activities.MyPostsActivity;
import com.blogofyb.forum.activities.MyResponseActivity;
import com.blogofyb.forum.activities.SelectActivity;
import com.blogofyb.forum.activities.SelectImageActivity;
import com.blogofyb.forum.activities.SettingActivity;
import com.blogofyb.forum.activities.StarActivity;
import com.blogofyb.forum.activities.SubscribeUserActivity;
import com.blogofyb.forum.activities.UserInformationActivity;
import com.blogofyb.forum.beans.UserBean;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;
import com.blogofyb.forum.utils.http.Get;
import com.blogofyb.forum.utils.img.ImageLoader;
import com.blogofyb.forum.utils.json.ToHashMap;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ZoneFragment extends Fragment {
    private final int GET_DATA_SUCCESS = 0;
    private final int GET_DATA_FAILED = 1;

    private UserBean mUserBean;
    private String mAccount;
    private ImageLoader mImageLoader;

    private CircleImageView mUserHead;
    private ImageView mGender;
    private ImageView mBackground;
    private TextView mUserName;
    private TextView mAge;
    private TextView mLevel;
    private TextView mSignature;
    private TextView mSubscribeCount;
    private TextView mFansCount;
    private TextView mStarCount;
    private LinearLayout mGenderAge;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case GET_DATA_SUCCESS:
                    mSwipeRefreshLayout.setRefreshing(false);
                    showData();
                    break;
                case GET_DATA_FAILED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity != null) {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("user", Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                boolean haveUser = sharedPreferences.getBoolean("haveUser", false);
                if (haveUser) {
                    SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(getContext());
                    Cursor cursor = database.query(SQLite.TABLE_NAME, new String[] {SQLite.ACCOUNT},
                            null, null, null, null, null);
                    while (cursor.moveToNext()) {
                        mAccount = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
                    }
                    cursor.close();
                }
            }
        } else {
            mAccount = null;
        }
        final View view;
        if (mAccount != null) {
            mImageLoader = new ImageLoader(getContext());
            view = inflater.inflate(R.layout.fg_zone, container, false);
            mUserHead = view.findViewById(R.id.civ_user_head);
            mUserHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), SelectImageActivity.class);
                    intent.putExtra("key", "HEA");
                    startActivity(intent);
                }
            });
            mUserName = view.findViewById(R.id.tv_post_author);
            mGender = view.findViewById(R.id.iv_user_gender);
            mAge = view.findViewById(R.id.tv_user_age);
            mLevel = view.findViewById(R.id.tv_user_level);
            mSignature = view.findViewById(R.id.tv_signature);
            mSubscribeCount = view.findViewById(R.id.tv_subscribe_count);
            mFansCount = view.findViewById(R.id.tv_fans_count);
            mStarCount = view.findViewById(R.id.tv_star_count);
            mBackground = view.findViewById(R.id.iv_user_background);
            mBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), SelectImageActivity.class);
                    intent.putExtra("key", "BAC");
                    startActivity(intent);
                }
            });
            mGenderAge = view.findViewById(R.id.ll_gender_age);

            mSwipeRefreshLayout = view.findViewById(R.id.srl_refresh_user_information);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    getData();
                }
            });

            TextView myPosts = view.findViewById(R.id.tv_my_posts);
            myPosts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), MyPostsActivity.class);
                    intent.putExtra(Keys.ACCOUNT, mAccount);
                    startActivity(intent);
                }
            });

            TextView myResponse = view.findViewById(R.id.tv_my_response);
            myResponse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), MyResponseActivity.class);
                    intent.putExtra(Keys.ACCOUNT, mAccount);
                    startActivity(intent);
                }
            });

            TextView setting = view.findViewById(R.id.tv_setting);
            setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), SettingActivity.class);
                    startActivity(intent);
                }
            });

            view.findViewById(R.id.ll_subscribe_count).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), SubscribeUserActivity.class);
                    intent.putExtra(Keys.ACCOUNT, mAccount);
                    startActivity(intent);
                }
            });

            view.findViewById(R.id.ll_fans_count).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), FansActivity.class);
                    intent.putExtra(Keys.ACCOUNT, mAccount);
                    startActivity(intent);
                }
            });

            view.findViewById(R.id.ll_star_count).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), StarActivity.class);
                    intent.putExtra(Keys.ACCOUNT, mAccount);
                    startActivity(intent);
                }
            });

            getData();
        } else  {
            view = inflater.inflate(R.layout.fg_please_login, container, false);
            Button toSelect = view.findViewById(R.id.btn_to_select);
            toSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), SelectActivity.class);
                    startActivity(intent);
                }
            });
        }
        return view;
    }

    private void getData() {
        mUserBean = new UserBean();
        Get.sendHttpRequest(ServerInformation.GET_USER_INFORMATION + mAccount, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    HashMap returnData = ToHashMap.getInstance().transform(response);
                    mUserBean.setAccount(mAccount);
                    mUserBean.setAge((String) returnData.get(Keys.AGE));
                    mUserBean.setBackground((String) returnData.get(Keys.BACKGROUND));
                    mUserBean.setFansCount((String) returnData.get(Keys.FANS_COUNT));
                    mUserBean.setGender((String) returnData.get(Keys.GENDER));
                    mUserBean.setHead((String) returnData.get(Keys.HEAD));
                    mUserBean.setLevel((String) returnData.get(Keys.LEVEL));
                    mUserBean.setSignature((String) returnData.get(Keys.SIGNATURE));
                    mUserBean.setStartCount((String) returnData.get(Keys.STAR_COUNT));
                    mUserBean.setSubscribeCount((String) returnData.get(Keys.SUBSCRIBE_COUNT));
                    mUserBean.setUserName((String) returnData.get(Keys.NIC_NAME));
                    mUserBean.setBirthday((String) returnData.get(Keys.BIRTHDAY));
                    Message message = new Message();
                    message.what = GET_DATA_SUCCESS;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = GET_DATA_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showData() {
        // 设置背景
//        mImageLoader.set(mBackground, mUserBean.getBackground());

        mImageLoader.set(mBackground, mUserBean.getBackground());
        // 设置头像
        mImageLoader.set(mUserHead, mUserBean.getHead());

        // 设置昵称
        mUserName.setText(mUserBean.getUserName());

        // 设置性别
        if ("male".equals(mUserBean.getGender())) {
            mGender.setImageResource(R.drawable.male);
            mGenderAge.setBackgroundResource(R.drawable.bg_male_age);
        } else if ("female".equals(mUserBean.getGender())) {
            mGender.setImageResource(R.drawable.female);
            mGenderAge.setBackgroundResource(R.drawable.bg_female_age);
        } else {
            mGender.setImageResource(R.drawable.gender);
            mGenderAge.setBackgroundResource(R.drawable.bg_gender_age);
        }

        // 设置年龄
        mAge.setText(mUserBean.getAge());

        // 设置等级
        switch (mUserBean.getLevel()) {
            case "2":
                mLevel.setText(getResources().getText(R.string.level_2));
                mLevel.setBackgroundResource(R.drawable.bg_level_2);
                break;
            case "3":
                mLevel.setText(getResources().getText(R.string.level_3));
                mLevel.setBackgroundResource(R.drawable.bg_level_3);
                break;
            case "4":
                mLevel.setText(getResources().getText(R.string.level_4));
                mLevel.setBackgroundResource(R.drawable.bg_level_4);
                break;
            case "5":
                mLevel.setText(getResources().getText(R.string.level_5));
                mLevel.setBackgroundResource(R.drawable.bg_level_5);
                break;
            case "6":
                mLevel.setText(getResources().getText(R.string.level_6));
                mLevel.setBackgroundResource(R.drawable.bg_level_6);
                break;
            default:
                mLevel.setText(getResources().getText(R.string.level_1));
                mLevel.setBackgroundResource(R.drawable.bg_level_1);
                break;
        }

        // 设置个性签名
        mSignature.setText(mUserBean.getSignature());

        // 设置关注数量
        mSubscribeCount.setText(mUserBean.getSubscribeCount());

        // 设置粉丝数量
        mFansCount.setText(mUserBean.getFansCount());

        // 设置收藏数量
        mStarCount.setText(mUserBean.getStartCount());

        saveUserInformation(SQLite.USER_NAME, mUserBean.getUserName());
        saveUserInformation(SQLite.GENDER, mUserBean.getGender());
        saveUserInformation(SQLite.BIRTHDAY, mUserBean.getBirthday());
        saveUserInformation(SQLite.SIGNATURE, mUserBean.getSignature());
    }

    private void saveUserInformation(String column, String newValue) {
        SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(getContext());
        String sql = "UPDATE " + SQLite.TABLE_NAME + " SET " + column + "='" + newValue + "';";
        database.execSQL(sql);
    }
}
