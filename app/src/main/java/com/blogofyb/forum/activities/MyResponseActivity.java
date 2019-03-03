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
import android.widget.ImageView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.adpter.MyResponseAdapter;
import com.blogofyb.forum.beans.CommentBean;
import com.blogofyb.forum.decoration.FloorDecoration;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.http.Get;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyResponseActivity extends BaseActivity {
    private final int REFRESH_FINISH = 0;
    private final int LOAD_BEAN_SUCCESS = 1;
    private final int LOAD_BEAN_FAILED = 2;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mCommentsList;
    private MyResponseAdapter mAdapter;

    private String mAccount;
    private List<CommentBean> mComments;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case REFRESH_FINISH:
                    mSwipeRefreshLayout.setRefreshing(false);
                    mAdapter.refreshData(mComments);
                    break;
                case LOAD_BEAN_FAILED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MyResponseActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    break;
                case LOAD_BEAN_SUCCESS:
                    showData();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_subscribe_user);

        Intent intent = getIntent();
        if (intent != null) {
            mAccount = intent.getStringExtra(Keys.ACCOUNT);
        }

        mSwipeRefreshLayout = findViewById(R.id.srl_refresh_subscribe_user);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                loadBean();
            }
        });
        mCommentsList = findViewById(R.id.rv_subscribe_user_list);

        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        toolbar.setTitle(getResources().getString(R.string.my_response));
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(MyResponseActivity.this);
            }
        });

        loadBean();
    }

    private void loadBean() {
        mComments = new ArrayList<>();
        Get.sendHttpRequest(ServerInformation.MY_RESPONSE + mAccount + "&index=0", new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONArray jsonArray = new JSONObject(response).getJSONArray(Keys.RETURN_DATA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        CommentBean commentBean = new CommentBean();
                        commentBean.setContent(object.getString(Keys.POST_CONTENT));
                        commentBean.setDate(object.getString(Keys.POST_DATE));
                        commentBean.setPlateName(object.getString(Keys.PLATE_NAME));
                        commentBean.setPostId(object.getString(Keys.ID));
                        commentBean.setPostTitle(object.getString(Keys.POST_TITLE));
                        commentBean.setDescription(object.getString(Keys.POST_DESCRIPTION));
                        commentBean.setUserName(object.getString(Keys.POST_AUTHOR));
                        mComments.add(commentBean);
                    }
                    Message message = new Message();
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        message.what = REFRESH_FINISH;
                    } else {
                        message.what = LOAD_BEAN_SUCCESS;
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
                message.what = LOAD_BEAN_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showData() {
        mAdapter = new MyResponseAdapter(this, mComments, mAccount);
        mCommentsList.setAdapter(mAdapter);
        mCommentsList.setLayoutManager(new LinearLayoutManager(this));
        mCommentsList.addItemDecoration(new FloorDecoration(this));
    }
}
