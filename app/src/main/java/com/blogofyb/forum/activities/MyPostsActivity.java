package com.blogofyb.forum.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.adpter.PostAdapter;
import com.blogofyb.forum.beans.PostBean;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.http.Get;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends BaseActivity {
    private final int REFRESH_FINISH = 0;
    private final int GET_POST_SUCCESS = 1;
    private final int GET_POST_FAILED = 2;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mPostList;

    private String mAccount;
    private PostAdapter mAdapter;
    private List<PostBean> mPosts;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case REFRESH_FINISH:
                    mSwipeRefreshLayout.setRefreshing(false);
                    mAdapter.refreshData(mPosts);
                    break;
                case GET_POST_FAILED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MyPostsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    break;
                case GET_POST_SUCCESS:
                    showData();
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
            mAccount = intent.getStringExtra(Keys.ACCOUNT);
        }

        setContentView(R.layout.layout_subscribe_user);
        mSwipeRefreshLayout = findViewById(R.id.srl_refresh_subscribe_user);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                loadBean();
            }
        });

        mPostList = findViewById(R.id.rv_post_list);

        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setTitle(getResources().getString(R.string.my_posts));
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(MyPostsActivity.this);
            }
        });

        loadBean();
    }

    private void loadBean() {
        mPosts = new ArrayList<>();
        Get.sendHttpRequest(ServerInformation.USER_POSTS + mAccount, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONArray jsonArray = new JSONObject(response).getJSONArray(Keys.RETURN_DATA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        PostBean postBean = new PostBean();
                        postBean.setTitle((String) object.get(Keys.POST_TITLE));
                        postBean.setAuthor((String) object.get(Keys.POST_AUTHOR));
                        postBean.setDescription((String) object.get(Keys.POST_DESCRIPTION));
                        postBean.setIcon((String) object.get(Keys.ICON));
                        postBean.setVisit((String) object.get(Keys.POST_VISIT));
                        postBean.setDiscuss((String) object.get(Keys.POST_DISCUSS));
                        postBean.setId((String) object.get(Keys.ID));
                        postBean.setDate((String) object.get(Keys.POST_DATE));
                        postBean.setTime((String) object.get(Keys.TIME));
                        mPosts.add(postBean);
                    }
                    Message message = new Message();
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        message.what = REFRESH_FINISH;
                    } else {
                        message.what = GET_POST_SUCCESS;
                    }
                    handler.sendMessage(message);
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = GET_POST_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showData() {
        mAdapter = new PostAdapter(mPosts, this);
        mPostList.setAdapter(mAdapter);
        mPostList.setLayoutManager(new LinearLayoutManager(this));
    }
}
