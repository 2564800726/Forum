package com.blogofyb.forum.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SearchActivity extends BaseActivity {
    private final int SEARCH_SUCCESS = 0;
    private final int SEARCH_FAILED = 1;
    private final int REFRESH_FINISH = 2;

    private RecyclerView mResultList;
    private EditText mTarget;
    private Button mSearch;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private PostAdapter mAdapter;
    private List<PostBean> mPosts;
    private String mTargetString;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case SEARCH_FAILED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    setButtonClickable(true);
                    Toast.makeText(SearchActivity.this, "搜索失败", Toast.LENGTH_SHORT).show();
                    break;
                case SEARCH_SUCCESS:
                    setButtonClickable(true);
                    showData();
                    break;
                case REFRESH_FINISH:
                    setButtonClickable(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mAdapter.refreshData(mPosts);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_search);

        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        toolbar.setTitle(getResources().getString(R.string.search));
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(SearchActivity.this);
            }
        });

        mTarget = findViewById(R.id.et_target);


        mSwipeRefreshLayout = findViewById(R.id.srl_refresh_result);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                setButtonClickable(false);
                loadBean();
            }
        });

        mSearch = findViewById(R.id.btn_search_post);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(mTarget.getText().toString())) {
                    Toast.makeText(SearchActivity.this, "关键词不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    mTargetString = mTarget.getText().toString();
                    setButtonClickable(false);
                    mSwipeRefreshLayout.setRefreshing(true);
                    loadBean();
                }
            }
        });
        mResultList = findViewById(R.id.rv_result_list);
    }

    private void loadBean() {
        mPosts = new ArrayList<>();
        Get.sendHttpRequest(ServerInformation.SEARCH + mTargetString, new HttpCallbackListener() {
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
                        message.what = SEARCH_SUCCESS;
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
                message.what = SEARCH_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showData() {
        mAdapter = new PostAdapter(mPosts, this);
        mResultList.setAdapter(mAdapter);
        mResultList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setButtonClickable(boolean value) {
        mSearch.setClickable(value);
    }
}
