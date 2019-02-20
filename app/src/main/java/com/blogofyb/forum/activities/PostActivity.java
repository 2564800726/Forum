package com.blogofyb.forum.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.adpter.PostAdapter;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.http.Get;
import com.blogofyb.forum.utils.http.Post;
import com.blogofyb.forum.utils.json.ToHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PostActivity extends BaseActivity {
    private final int FINISH = 0;
    private final int GET_POST_DETAIL_SUCCESS = 1;
    private final int GET_POST_DETAIL_FAILED = 2;

    private String mPostId;
    private String mAccount;
    private String mPassword;
    private String mPlateName;
    private int mIndex = 0;
    private boolean isStar = false;
    private List<HashMap<String, String>> details;

    private ImageView mNavigationIconLeft;
    private ImageView mNavigationIconRight;
    private TextView mTitle;
    private RecyclerView mFloorList;
    private ImageView mPraise;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case FINISH:
                    setItemIcon();
                    break;
                case GET_POST_DETAIL_SUCCESS:
                    showData();
                    break;
                case GET_POST_DETAIL_FAILED:
                    Toast.makeText(PostActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_post);
        Intent intent = getIntent();
        if (intent != null) {
            mPostId = intent.getStringExtra(Keys.ID);
            mPlateName = intent.getStringExtra(Keys.PLATE_NAME);
        }
        mFloorList = findViewById(R.id.rv_floor_list);
        mNavigationIconLeft = findViewById(R.id.iv_navigation_icon_left);
        mNavigationIconLeft.setImageResource(R.drawable.back);
        mNavigationIconLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mNavigationIconRight = findViewById(R.id.iv_navigation_icon_right);
        setItemIcon();
        mNavigationIconRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starPost();
            }
        });
//        mNavigationIconRight.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    v.setBackgroundResource(R.color.colorBackground);
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    v.setBackgroundResource(R.color.colorNull);
//                }
//                return false;
//            }
//        });
        mTitle = findViewById(R.id.tv_title);
        mTitle.setText(mPlateName);
        checkStar();
        getPostDetails();
    }

    private void starPost() {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ID, mPostId);
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.PASSWORD, mPassword);
        Post.sendHttpRequest(ServerInformation.STAR_POST, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                isStar = true;
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(Exception e) {
                isStar = false;
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }
        });
    }

    private void setItemIcon() {
        if (isStar) {
            mNavigationIconRight.setImageResource(R.drawable.star);
        } else {
            mNavigationIconRight.setImageResource(R.drawable.un_star);
        }
    }

    private void checkStar() {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ID, mPostId);
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.PASSWORD, mPassword);
        Post.sendHttpRequest(ServerInformation.CHECK_STAR, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    HashMap returnData = ToHashMap.getInstance().transform(response);
                    if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                        isStar = true;
                    } else {
                        isStar = false;
                    }
                    Message message = new Message();
                    message.what = FINISH;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                isStar = false;
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }
        });
    }

    private void getPostDetails() {
        details = new ArrayList<>();
        Get.sendHttpRequest(ServerInformation.GET_POST_DETAIL + mPostId + "&index=" + mIndex, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONArray jsonArray = new JSONObject(response).getJSONArray(Keys.RETURN_DATA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        HashMap<String, String> data = new HashMap<>();
                        for (Iterator<String> iterator = jsonObject.keys(); iterator.hasNext(); ) {
                            String key = iterator.next();
                            data.put(key, jsonObject.getString(key));
                        }
                        details.add(data);
                    }
                    Message message = new Message();
                    message.what = GET_POST_DETAIL_SUCCESS;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = GET_POST_DETAIL_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showData() {
        mFloorList.setAdapter(new PostAdapter(details));
        mFloorList.setLayoutManager(new LinearLayoutManager(this));
    }
}
