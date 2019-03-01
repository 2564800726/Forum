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
import com.blogofyb.forum.adpter.UserListAdapter;
import com.blogofyb.forum.beans.UserBean;
import com.blogofyb.forum.decoration.FloorDecoration;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.http.Get;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FansActivity extends BaseActivity {
    private final int GET_FANS_SUCCESS = 0;
    private final int GET_FANS_FAILED = 1;
    private final int REFRESH_FINISH = 2;

    private RecyclerView mFansList;
    private String mAccount;
    private List<UserBean> mUsers;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private UserListAdapter mAdapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case GET_FANS_SUCCESS:
                    showData();
                    break;
                case GET_FANS_FAILED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(FansActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    break;
                case REFRESH_FINISH:
                    mSwipeRefreshLayout.setRefreshing(false);
                    mAdapter.refreshData(mUsers);
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

        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setTitle(getResources().getString(R.string.fans));
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager   .removeActivity(FansActivity.this);
            }
        });

        mSwipeRefreshLayout = findViewById(R.id.srl_refresh_subscribe_user);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                loadBean();
            }
        });
        mFansList = findViewById(R.id.rv_subscribe_user_list);
        loadBean();
    }

    private void loadBean() {
        mUsers = new ArrayList<>();
        Get.sendHttpRequest(ServerInformation.FANS + mAccount, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONArray jsonArray = new JSONObject(response).getJSONArray(Keys.RETURN_DATA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        UserBean userBean = new UserBean();
                        userBean.setUserName(object.getString(Keys.NIC_NAME));
                        userBean.setLevel(object.getString(Keys.LEVEL));
                        userBean.setHead(object.getString(Keys.HEAD));
                        userBean.setGender(object.getString(Keys.GENDER));
                        userBean.setAge(object.getString(Keys.AGE));
                        userBean.setAccount(object.getString(Keys.ACCOUNT));
                        mUsers.add(userBean);
                    }
                    Message message = new Message();
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        message.what = REFRESH_FINISH;
                    } else {
                        message.what = GET_FANS_SUCCESS;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = GET_FANS_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showData() {
        mAdapter = new UserListAdapter(mUsers, this);
        mFansList.setAdapter(mAdapter);
        mFansList.addItemDecoration(new FloorDecoration(this));
        mFansList.setLayoutManager(new LinearLayoutManager(this));
    }
}
