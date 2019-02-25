package com.blogofyb.forum.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.activities.SearchActivity;
import com.blogofyb.forum.adpter.PlateListAdapter;
import com.blogofyb.forum.beans.PlateBean;
import com.blogofyb.forum.beans.PostBean;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;
import com.blogofyb.forum.utils.http.Get;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.json.ToHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ForumFragment extends Fragment {
    private final int GET_RECOMMEND_SUCCESS = 0;
    private final int GET_PLATES_SUCCESS = 1;
    private final int FAILED = 2;
    private final int ALL_FINISH = 4;
    private final int REFRESH_FINISH = 5;

    private List<PlateBean> mPlates;
    private List<PostBean> mPost;
    private boolean mHaveUser;
    private String mAccount;
    private RecyclerView mRecyclerView;
    private PlateListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mSearch;

    private boolean isFinishGetRecommend = false;
    private boolean isFinishGetPlates = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case GET_RECOMMEND_SUCCESS:
                    isFinishGetRecommend = true;
                    break;
                case GET_PLATES_SUCCESS:
                    isFinishGetPlates = true;
                    break;
                case FAILED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    break;
                case ALL_FINISH:
                    isFinishGetPlates = false;
                    isFinishGetRecommend = false;
                    showData();
                    break;
                case REFRESH_FINISH:
                    mSwipeRefreshLayout.setRefreshing(false);
                    mAdapter.refreshData(mPlates, mPost);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_forum, container, false);
        Activity activity = getActivity();
        if (activity != null) {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("user", Context.MODE_PRIVATE);
            mHaveUser = sharedPreferences.getBoolean("haveUser", false);
            if (mHaveUser) {
                SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(getContext());
                Cursor cursor = database.query(SQLite.TABLE_NAME, new String[]{SQLite.ACCOUNT, SQLite.PASSWORD},
                        null, null, null, null, null);
                while (cursor.moveToNext()) {
                    mAccount = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
                }
                cursor.close();
            }
        }
        mRecyclerView = view.findViewById(R.id.rv_plate_list);
        mAdapter = new PlateListAdapter(mPlates, mPost, getContext());
        mSwipeRefreshLayout = view.findViewById(R.id.srl_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                mPlates = new ArrayList<>();
                mPost =new ArrayList<>();
                getPlates();
                getRecommendPost();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int count = 0;
                        while ((!isFinishGetPlates || !isFinishGetRecommend) && count < 50) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            count++;
                        }
                        Message message = new Message();
                        if (isFinishGetRecommend && isFinishGetPlates) {
                            isFinishGetPlates = false;
                            isFinishGetRecommend = false;
                            message.what = REFRESH_FINISH;
                            handler.sendMessage(message);
                        } else {
                            message.what = FAILED;
                            handler.sendMessage(message);
                        }
                    }
                }).start();
            }
        });
        mSearch = view.findViewById(R.id.fab_search);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });
        loadBeans();
        return view;
    }

    private void showData() {
        if (mPost != null && mPlates != null) {
            mAdapter.refreshData(mPlates, mPost);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }

    private void loadBeans() {
        mPlates = new ArrayList<>();
        mPost = new ArrayList<>();
        getRecommendPost();
        getPlates();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (!isFinishGetPlates || !isFinishGetRecommend && count < 50) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
                Message message = new Message();
                message.what = ALL_FINISH;
                handler.sendMessage(message);
            }
        }).start();
    }

    private void getRecommendPost() {
        Get.sendHttpRequest(ServerInformation.GET_RECOMMEND_POST, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    HashMap recommend = ToHashMap.getInstance().transform(response);
                    if (recommend != null) {
                        PostBean postBean = new PostBean();
                        postBean.setTitle((String) recommend.get(Keys.POST_TITLE));
                        postBean.setAuthor((String) recommend.get(Keys.POST_AUTHOR));
                        postBean.setDescription((String) recommend.get(Keys.POST_DESCRIPTION));
                        postBean.setIcon((String) recommend.get(Keys.ICON));
                        postBean.setVisit((String) recommend.get(Keys.POST_VISIT));
                        postBean.setDiscuss((String) recommend.get(Keys.POST_DISCUSS));
                        postBean.setId((String) recommend.get(Keys.ID));
                        postBean.setDate((String) recommend.get(Keys.POST_DATE));
                        mPost.add(postBean);
                        isFinishGetRecommend = true;
                        Message message = new Message();
                        message.what = GET_RECOMMEND_SUCCESS;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                isFinishGetRecommend = false;
                Message message = new Message();
                message.what = FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void getPlates() {
        HttpCallbackListener listener = new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("returnData");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                        mPlates.add(new PlateBean());
                        PlateBean plateBean = mPlates.get(mPlates.size() - 1);
                        plateBean.setPlateName(jsonObject1.getString(Keys.PLATE_NAME));
                        plateBean.setIcon(jsonObject1.getString(Keys.ICON));
                        plateBean.setId(jsonObject1.getString(Keys.ID));
                    }
                    isFinishGetPlates = true;
                    Message message = new Message();
                    message.what = GET_PLATES_SUCCESS;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                isFinishGetPlates = false;
                Message message = new Message();
                message.what = FAILED;
                handler.sendMessage(message);
            }
        };
        if (!mHaveUser) {
            Get.sendHttpRequest(ServerInformation.GET_PLATES_WITHOUT_ACCOUNT, listener);
        } else {
            Get.sendHttpRequest(ServerInformation.GET_PLATES_WITH_ACCOUNT + mAccount, listener);
        }
    }
}
