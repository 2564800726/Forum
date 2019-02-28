package com.blogofyb.forum.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.adpter.PostAdapter;
import com.blogofyb.forum.beans.PostBean;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;
import com.blogofyb.forum.utils.http.Get;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SubscribeFragment extends Fragment {
    private final int REFRESH_FINISH = 0;
    private final int LOAD_SUCCESS = 1;
    private final int LOAD_FAILED = 2;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mPostList;

    private String mAccount;
    private boolean mHaveUser;
    private List<PostBean> mPosts;
    private PostAdapter mAdapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case REFRESH_FINISH:
                    mSwipeRefreshLayout.setRefreshing(false);
                    mAdapter.refreshData(mPosts);
                    break;
                case LOAD_FAILED:
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    break;
                case LOAD_SUCCESS:
                    showData();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        mHaveUser = sharedPreferences.getBoolean("haveUser", false);
        View view;
        if (mHaveUser) {
            SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(getContext());
            Cursor cursor = database.query(SQLite.TABLE_NAME, new String[]{SQLite.ACCOUNT},
                    null, null, null, null, null);
            while (cursor.moveToNext()) {
                mAccount = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
            }
            cursor.close();
            view = inflater.inflate(R.layout.fg_subscribe, container, false);
            mSwipeRefreshLayout = view.findViewById(R.id.srl_refresh_subscribe_posts);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    loadBean();
                }
            });
            mPostList = view.findViewById(R.id.rv_subscribe_posts);
            loadBean();
        } else {
            view = inflater.inflate(R.layout.fg_please_login, container, false);
        }
        return view;
    }

    private void loadBean() {
        mPosts = new ArrayList<>();
        Get.sendHttpRequest(ServerInformation.SUBSCRIBE_POSTS + mAccount, new HttpCallbackListener() {
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
                        message.what = LOAD_SUCCESS;
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
                message.what = LOAD_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showData() {
        mAdapter = new PostAdapter(mPosts, getContext());
        mPostList.setLayoutManager(new LinearLayoutManager(getContext()));
        mPostList.setAdapter(mAdapter);
    }
}
