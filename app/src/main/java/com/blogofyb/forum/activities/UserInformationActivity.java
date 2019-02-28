package com.blogofyb.forum.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.beans.UserBean;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;
import com.blogofyb.forum.utils.http.Get;
import com.blogofyb.forum.utils.http.Post;
import com.blogofyb.forum.utils.img.ImageLoader;
import com.blogofyb.forum.utils.json.ToHashMap;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInformationActivity extends BaseActivity implements View.OnClickListener {
    private final int FINISH = 2;
    private final int GET_DATA_SUCCESS = 0;
    private final int GET_DATA_FAILED = 1;

    private String mUser;
    private String mAccount;
    private String mPassword;
    private boolean isSubscribe = false;
    private UserBean mUserBean;
    private ImageLoader mImageLoader;

    private Button mSubscribe;
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case FINISH:
                    setButtonClickable(true);
                    setButton();
                    break;
                case GET_DATA_FAILED:
                    setButtonClickable(true);
                    Toast.makeText(UserInformationActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    break;
                case GET_DATA_SUCCESS:
                    setButtonClickable(true);
                    showData();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_user_information);

        mImageLoader = new ImageLoader(this);

        Intent intent = getIntent();
        if (intent != null) {
            mUser = intent.getStringExtra(Keys.ACCOUNT);
            SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(this);
            Cursor cursor = database.query(SQLite.TABLE_NAME, new String[]{SQLite.ACCOUNT, SQLite.PASSWORD},
                    null, null, null, null, null);
            while (cursor.moveToNext()) {
                mAccount = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
                mPassword = cursor.getString(cursor.getColumnIndex(SQLite.PASSWORD));
            }
            cursor.close();
        }

        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(UserInformationActivity.this);
            }
        });

        mUserHead = findViewById(R.id.civ_user_head);
        mUserName = findViewById(R.id.tv_post_author);
        mGender = findViewById(R.id.iv_user_gender);
        mAge = findViewById(R.id.tv_user_age);
        mLevel = findViewById(R.id.tv_user_level);
        mSignature = findViewById(R.id.tv_signature);
        mSubscribeCount = findViewById(R.id.tv_subscribe_count);
        mFansCount = findViewById(R.id.tv_fans_count);
        mStarCount = findViewById(R.id.tv_star_count);
        mBackground = findViewById(R.id.iv_user_background);
        mGenderAge = findViewById(R.id.ll_gender_age);

        mSubscribe = findViewById(R.id.btn_subscribe_user);
        findViewById(R.id.ll_subscribe_count).setOnClickListener(this);
        findViewById(R.id.ll_star_count).setOnClickListener(this);
        findViewById(R.id.ll_fans_count).setOnClickListener(this);
        findViewById(R.id.tv_him_post).setOnClickListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("haveUser", false) && !mAccount.equals(mUser)) {
            mSubscribe.setOnClickListener(this);
            checkSubscribe();
        } else {
            mSubscribe.setVisibility(View.GONE);
        }
        getData();
        checkSubscribe();
    }

    private void setButton() {
        if (isSubscribe) {
            mSubscribe.setText("已关注");
        } else {
            mSubscribe.setText(R.string.subscribe);
        }
    }

    private void checkSubscribe() {
        isSubscribe = false;
        setButtonClickable(false);
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.PASSWORD, mPassword);
        body.put(Keys.USER, mUser);
        Post.sendHttpRequest(ServerInformation.CHECK_SUBSCRIBE, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                if (returnData != null) {
                    if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                        isSubscribe = true;
                        Message message = new Message();
                        message.what = FINISH;
                        handler.sendMessage(message);
                        return;
                    }
                }
                isSubscribe = false;
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                isSubscribe = false;
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_subscribe_count:
                Intent intent = new Intent(this, SubscribeUserActivity.class);
                intent.putExtra(Keys.ACCOUNT, mUser);
                startActivity(intent);
                break;
            case R.id.ll_fans_count:
                Intent intent1 = new Intent(UserInformationActivity.this, FansActivity.class);
                intent1.putExtra(Keys.ACCOUNT, mUser);
                startActivity(intent1);
                break;
            case R.id.ll_star_count:
                Intent intent2 = new Intent(UserInformationActivity.this, StarActivity.class);
                intent2.putExtra(Keys.ACCOUNT, mUser);
                startActivity(intent2);
                break;
            case R.id.btn_subscribe_user:
                setButtonClickable(false);
                subscribeUser();
                break;
            case R.id.tv_him_post:
                Intent intent3 = new Intent(UserInformationActivity.this, MyPostsActivity.class);
                intent3.putExtra(Keys.ACCOUNT, mUser);
                startActivity(intent3);
                break;
        }
    }

    private void subscribeUser() {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.PASSWORD, mPassword);
        body.put(Keys.USER, mUser);
        Post.sendHttpRequest(ServerInformation.SUBSCRIBE, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                if (returnData != null) {
                    if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                        isSubscribe = true;
                        Message message = new Message();
                        message.what = FINISH;
                        handler.sendMessage(message);
                        return;
                    }
                }
                onFailure(null);
            }

            @Override
            public void onFailure(Exception e) {
                isSubscribe = false;
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }
        });
    }

    private void getData() {
        mUserBean = new UserBean();
        Get.sendHttpRequest(ServerInformation.GET_USER_INFORMATION + mUser, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    HashMap returnData = ToHashMap.getInstance().transform(response);
                    mUserBean.setAccount(mUser);
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
    }

    private void setButtonClickable(boolean value) {
        mSubscribe.setClickable(value);
    }
}
