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
import android.widget.Button;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.adpter.SubscribePlateAdapter;
import com.blogofyb.forum.beans.PlateBean;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.http.Get;
import com.blogofyb.forum.utils.json.ToHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubscribePlateActivity extends BaseActivity {
    private final int GET_PLATES_FAILED = 1;
    private final int REFRESH_SUCCESS = 2;
    private final int ALL_SUCCESS = 3;
    private final int SUBSCRIBE_SUCCESS = 0;

    private List<PlateBean> mPlates;
    private List<String> mSubscribePlates;
    private SubscribePlateAdapter mAdapter;
    private boolean isGetSubscribeSuccess = false;
    private boolean isGetPlatesSuccess = false;
    private String mAccount;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Button mSubscribe;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case GET_PLATES_FAILED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(SubscribePlateActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    break;
                case REFRESH_SUCCESS:
                    mSwipeRefreshLayout.setRefreshing(false);
                    mAdapter.refreshData(mPlates, mSubscribePlates.toString());
                case ALL_SUCCESS:
                    showData();
                    break;
                case SUBSCRIBE_SUCCESS:
                    mSwipeRefreshLayout.setRefreshing(true);
                    loadBeans();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_subscribe_plate);

        Intent intent = getIntent();
        if (intent != null) {
            mAccount = intent.getStringExtra(Keys.ACCOUNT);
        }

        mRecyclerView = findViewById(R.id.rv_Subscribe_plate);
        mSwipeRefreshLayout = findViewById(R.id.srl_refresh_plates);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                loadBeans();
            }
        });
        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setTitle("订阅板块");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(SubscribePlateActivity.this);
            }
        });

        loadBeans();
    }

    private void loadBeans() {
        getPlates();
        getSubscribePlate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while ((!isGetPlatesSuccess || !isGetSubscribeSuccess) && count < 50) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
                Message message = new Message();
                if (isGetPlatesSuccess && isGetSubscribeSuccess) {
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        message.what = REFRESH_SUCCESS;
                    } else {
                        message.what = ALL_SUCCESS;
                    }
                } else {
                    message.what = GET_PLATES_FAILED;
                }
                handler.sendMessage(message);
            }
        }).start();
    }

    private void getSubscribePlate() {
        isGetSubscribeSuccess = false;
        mSubscribePlates = new ArrayList<>();
        Get.sendHttpRequest(ServerInformation.GET_PLATES_WITH_ACCOUNT + mAccount, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONArray jsonArray = new JSONObject(response).getJSONArray(Keys.RETURN_DATA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        mSubscribePlates.add(";" + object.getString(Keys.ID) + ";");
                    }
                    isGetSubscribeSuccess = true;
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                isGetSubscribeSuccess = false;
                Message message = new Message();
                message.what = GET_PLATES_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void getPlates() {
        isGetPlatesSuccess = false;
        mPlates = new ArrayList<>();
        Get.sendHttpRequest(ServerInformation.GET_PLATES_WITHOUT_ACCOUNT, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray(Keys.RETURN_DATA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        PlateBean plateBean = new PlateBean();
                        plateBean.setPlateName(object.getString(Keys.PLATE_NAME));
                        plateBean.setIcon(object.getString(Keys.ICON));
                        plateBean.setId(object.getString(Keys.ID));
                        mPlates.add(plateBean);
                    }
                    isGetPlatesSuccess = true;
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                isGetPlatesSuccess = false;
                Message message = new Message();
                message.what = GET_PLATES_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showData() {
        mAdapter = new SubscribePlateAdapter(this, mPlates, mSubscribePlates.toString(), handler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }
}
