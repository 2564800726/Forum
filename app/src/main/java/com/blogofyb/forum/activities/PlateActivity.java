package com.blogofyb.forum.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.adpter.PlateListAdapter;
import com.blogofyb.forum.adpter.PostListAdapter;
import com.blogofyb.forum.beans.PlateInformationBean;
import com.blogofyb.forum.beans.PostBean;
import com.blogofyb.forum.beans.TopPostBean;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.http.Get;
import com.blogofyb.forum.utils.json.ToHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlateActivity extends BaseActivity {
    private final int GET_POSTS_SUCCESS = 0;
    private final int GET_POSTS_FAILED = 1;
    private final int GET_PLATE_INFORMATION_SUCCESS = 2;
    private final int GET_PLATE_INFORMATION_FAILED = 3;
    private final int GET_TOP_POSTS_SUCCESS = 4;
    private final int GET_TOP_POSTS_FAILED = 5;
    private final int ALL_SUCCESS = 6;
    private final int ALL_FAILED = 7;

    private boolean getPostsFinish = false;
    private boolean getPlateInformationFinish = false;
    private boolean getTopPostsFinish = false;

    private String mPlateId;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mWritePost;
    private PostListAdapter mAdapter;

    private List<PostBean> mPosts;
    private List<TopPostBean> mTopPosts;
    private List<PlateInformationBean> mPlateInformation;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case GET_POSTS_SUCCESS:
                    getPostsFinish = true;
                    break;
                case GET_PLATE_INFORMATION_SUCCESS:
                    getPlateInformationFinish = true;
                    break;
                case GET_TOP_POSTS_SUCCESS:
                    getTopPostsFinish = true;
                    break;
                case GET_POSTS_FAILED:
                    getPostsFinish = false;
                    break;
                case GET_PLATE_INFORMATION_FAILED:
                    getPlateInformationFinish = false;
                    break;
                case GET_TOP_POSTS_FAILED:
                    getTopPostsFinish = false;
                    break;
                case ALL_SUCCESS:
                    mSwipeRefreshLayout.setRefreshing(false);
                    setData();
                    break;
                case ALL_FAILED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(PlateActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        Intent intent = getIntent();
        if (intent != null) {
            mPlateId = intent.getStringExtra("id");
        }
        setContentView(R.layout.layout_plate);
        mRecyclerView = findViewById(R.id.rv_post_list);
        mAdapter = new PostListAdapter(mPosts, mTopPosts, mPlateInformation, this);
        mSwipeRefreshLayout = findViewById(R.id.srl_refresh_post);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                loadBeans();
            }
        });
        mWritePost = findViewById(R.id.fab_write_post);
        mWritePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlateActivity.this, WritePostActivity.class);
                intent.putExtra(Keys.ID, mPlateId);
                intent.putExtra(Keys.PLATE_NAME, mPlateInformation.get(0).getName());
                startActivity(intent);
            }
        });
        loadBeans();
        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(PlateActivity.this);
            }
        });
    }

    private void loadBeans() {
        mPosts = new ArrayList<>();
        mPlateInformation = new ArrayList<>();
        mTopPosts = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while ((!getPostsFinish || !getPlateInformationFinish || !getTopPostsFinish) && count < 50) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
                Message message = new Message();
                if (getPostsFinish && getPlateInformationFinish && getTopPostsFinish) {
                    message.what = ALL_SUCCESS;
                } else {
                    message.what = ALL_FAILED;
                }
                handler.sendMessage(message);
            }
        }).start();
        getPosts();
        getPlateInformation();
        getTopPosts();
    }

    private void getPosts() {
        Get.sendHttpRequest(ServerInformation.GET_POSTS + mPlateId, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray(Keys.RETURN_DATA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        PostBean postBean = new PostBean();
                        postBean.setDate(object.getString(Keys.POST_DATE));
                        postBean.setId(object.getString(Keys.ID));
                        postBean.setDiscuss(object.getString(Keys.POST_DISCUSS));
                        postBean.setVisit(object.getString(Keys.POST_VISIT));
                        postBean.setIcon(object.getString(Keys.ICON));
                        postBean.setDescription(object.getString(Keys.POST_DESCRIPTION));
                        postBean.setAuthor(object.getString(Keys.POST_AUTHOR));
                        postBean.setTitle(object.getString(Keys.POST_TITLE));
                        mPosts.add(postBean);
                        Message message = new Message();
                        message.what = GET_POSTS_SUCCESS;
                        handler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                message.what = GET_POSTS_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void getPlateInformation() {
        Get.sendHttpRequest(ServerInformation.GET_PLATE_INFORMATION + mPlateId, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                if (returnData != null) {
                    PlateInformationBean plateInformationBean = new PlateInformationBean();
                    plateInformationBean.setIcon((String) returnData.get(Keys.ICON));
                    plateInformationBean.setName((String) returnData.get(Keys.PLATE_NAME));
                    plateInformationBean.setId((String) returnData.get(Keys.ID));
                    mPlateInformation.add(plateInformationBean);
                    Message message = new Message();
                    message.what = GET_PLATE_INFORMATION_SUCCESS;
                    handler.sendMessage(message);
                    return;
                }
                onFailure(null);
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                message.what = GET_PLATE_INFORMATION_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void getTopPosts() {
        Get.sendHttpRequest(ServerInformation.GET_RECOMMEND_POST_WITH_PLATE + mPlateId, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                if (returnData != null) {
                    TopPostBean topPostBean = new TopPostBean();
                    topPostBean.setTitle((String) returnData.get(Keys.POST_TITLE));
                    topPostBean.setId((String) returnData.get(Keys.ID));
                    mTopPosts.add(topPostBean);
                    Message message = new Message();
                    message.what = GET_TOP_POSTS_SUCCESS;
                    handler.sendMessage(message);
                    return;
                }
                onFailure(null);
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                message.what = GET_TOP_POSTS_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void setData() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }
}
