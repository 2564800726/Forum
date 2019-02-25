package com.blogofyb.forum.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.adpter.PostAdapterHomePage;
import com.blogofyb.forum.adpter.PostAdapterOtherPage;
import com.blogofyb.forum.decoration.FloorDecoration;
import com.blogofyb.forum.interfaces.GetPostId;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;
import com.blogofyb.forum.utils.http.Get;
import com.blogofyb.forum.utils.http.Post;
import com.blogofyb.forum.utils.json.ToHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PostActivity extends BaseActivity implements View.OnClickListener, GetPostId {
    private final int FINISH = 0;
    private final int GET_POST_DETAIL_SUCCESS = 1;
    private final int FAILED = 2;
    private final int GET_POST_TOTAL_PAGES_SUCCESS = 3;
    private final int REFRESH_FINISH = 4;
    private final int PRAISE_FINISH = 5;

    private String mPostId;
    private String mAccount;
    private String mPassword;
    private boolean mHaveUser;
    private String mPostTitle;
    private String mAuthor;
    private String mContent;
    private int mIndex = 0;
    private int mTotalPages = 0;
    private boolean isStar = false;
    private boolean isPraise = false;
    private List<HashMap<String, String>> details;

    private ImageView mNavigationIconLeft;
    private ImageView mNavigationIconRight;
    private RecyclerView mFloorList;
    private PostAdapterHomePage mAdapterHomePage;
    private PostAdapterOtherPage mAdapterOtherPage;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ImageView mPraise;
    private LinearLayout mPager;
    private ImageView mForward;
    private TextView mCurrentPage;
    private TextView mTotalPage;
    private ImageView mBackward;
    private Button mComment;
    private TextView mDivider;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case PRAISE_FINISH:
                    refreshData();
                    setItemIcon();
                    break;
                case FINISH:
                    setItemIcon();
                    break;
                case GET_POST_DETAIL_SUCCESS:
                    mSwipeRefreshLayout.setRefreshing(false);
                    showData();
                    break;
                case FAILED:
                    setPagerClickable(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(PostActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    break;
                case GET_POST_TOTAL_PAGES_SUCCESS:
                    setPagerClickable(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    showPager();
                    break;
                case REFRESH_FINISH:
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (mIndex == 0) {
                        mAdapterHomePage.refreshData(details);
                    } else {
                        mAdapterOtherPage.refreshData(details);
                    }
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
            mPostTitle = intent.getStringExtra(Keys.POST_TITLE);
            mAuthor = intent.getStringExtra(Keys.POST_AUTHOR);
            mContent = intent.getStringExtra(Keys.POST_CONTENT);
        }
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        if (sharedPreferences != null) {
            mHaveUser = sharedPreferences.getBoolean("haveUser", false);
            if (mHaveUser) {
                SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(this);
                Cursor cursor = database.query(SQLite.TABLE_NAME, new String[]{SQLite.ACCOUNT, SQLite.PASSWORD},
                        null, null, null, null, null);
                while (cursor.moveToNext()) {
                    mAccount = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
                    mPassword = cursor.getString(cursor.getColumnIndex(SQLite.PASSWORD));
                }
                cursor.close();
            }
        }
        mFloorList = findViewById(R.id.rv_floor_list);
        mFloorList.addItemDecoration(new FloorDecoration(this));
        mSwipeRefreshLayout = findViewById(R.id.srl_refresh_floor);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        mNavigationIconLeft = findViewById(R.id.iv_navigation_icon_left);
        mNavigationIconLeft.setImageResource(R.drawable.back);
        mNavigationIconLeft.setOnClickListener(this);
        mNavigationIconRight = findViewById(R.id.iv_navigation_icon_right);
        mNavigationIconRight.setOnClickListener(this);
        TextView title = findViewById(R.id.tv_title);
        title.setText(mPostTitle);

        // 底部
        mPraise = findViewById(R.id.iv_praise);
        mPraise.setOnClickListener(this);
        mPager = findViewById(R.id.ll_pager);
        mPager.setOnClickListener(this);
        mForward = findViewById(R.id.iv_forward);
        mForward.setOnClickListener(this);
        mCurrentPage = findViewById(R.id.tv_current_page);
        mTotalPage = findViewById(R.id.tv_total_page);
        mBackward = findViewById(R.id.iv_backward);
        mBackward.setOnClickListener(this);
        mComment = findViewById(R.id.btn_comment);
        mComment.setOnClickListener(this);
        mDivider = findViewById(R.id.tv_divider);
        setItemIcon();
        if (mHaveUser) {
            checkStar();
            checkPraise();
        }
        getPostDetails();
        setPagerClickable(false);
    }

    private void refreshData() {
        mSwipeRefreshLayout.setRefreshing(true);
        getTotalPages();
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
                    message.what = REFRESH_FINISH;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void getTotalPages() {
        Get.sendHttpRequest(ServerInformation.GET_POST_TOTAL_PAGES + mPostId, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                Message message = new Message();
                if (returnData != null &&
                        (!"".equals(returnData.get(Keys.TOTAL_PAGES)) && returnData.get(Keys.TOTAL_PAGES) != null)) {
                    mTotalPages = Integer.parseInt((String) returnData.get(Keys.TOTAL_PAGES));
                    message.what = GET_POST_TOTAL_PAGES_SUCCESS;
                } else {
                    message.what = FAILED;
                }
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                message.what = FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showPager() {
        mCurrentPage.setText(String.valueOf(mIndex + 1));
        mTotalPage.setText(String.valueOf(mTotalPages));
    }

    private void starPost() {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ID, mPostId);
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.PASSWORD, mPassword);
        Post.sendHttpRequest(ServerInformation.STAR_POST, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                if (returnData != null) {
                    if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                        isStar = true;
                    } else {
                        isStar = false;
                    }
                }
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
        if (isPraise) {
            mPraise.setImageResource(R.drawable.praise);
        } else {
            mPraise.setImageResource(R.drawable.praise_normal);
        }
    }

    private void getPostDetails() {
        mSwipeRefreshLayout.setRefreshing(true);
        getTotalPages();
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
                message.what = FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void showData() {
        mAdapterHomePage = new PostAdapterHomePage(this, details);
        mAdapterOtherPage = new PostAdapterOtherPage(this, details);
        if (mIndex == 0) {
            mFloorList.setAdapter(mAdapterHomePage);
        } else {
            mFloorList.setAdapter(mAdapterOtherPage);
        }
        mFloorList.setLayoutManager(new LinearLayoutManager(this));
        if (mIndex == 0) {
            mForward.setImageResource(R.drawable.forward_can_not);
        } else {
            mForward.setImageResource(R.drawable.forward_can);
        }
        if (mIndex < mTotalPages - 1) {
            mBackward.setImageResource(R.drawable.backward_can);
        } else {
            mBackward.setImageResource(R.drawable.backward_can_not);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_navigation_icon_right:
                if (mHaveUser) {
                    starPost();
                } else {
                    Intent intent = new Intent(this, SelectActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.iv_navigation_icon_left:
                ActivitiesManager.removeActivity(this);
                break;
            case R.id.iv_praise:
                if (mHaveUser) {
                    praisePost();
                } else {
                    Intent intent = new Intent(this, SelectActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.iv_forward:
                setPagerClickable(false);
                if (mIndex > 0) {
                    mIndex--;
                    getPostDetails();
                    getTotalPages();
                }
                break;
            case R.id.iv_backward:
                setPagerClickable(false);
                if (mIndex < mTotalPages - 1) {
                    mIndex++;
                    getPostDetails();
                    getTotalPages();
                }
                break;
            case R.id.ll_pager:
                // 页码跳转...（待）
                break;
            case R.id.btn_comment:
                if (mHaveUser) {
                    Intent intent = new Intent(this, CommentActivity.class);
                    intent.putExtra(Keys.ID, mPostId);
                    intent.putExtra(Keys.FLOOR, "0");
                    intent.putExtra(Keys.ANOTHER_USER_NAME, mAuthor);
                    intent.putExtra(Keys.ANOTHER_CONTENT, mContent);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, SelectActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private void praisePost() {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.PASSWORD, mPassword);
        body.put(Keys.ID, mPostId);
        Post.sendHttpRequest(ServerInformation.PRAISE_POST, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    HashMap returnData = ToHashMap.getInstance().transform(response);
                    if (returnData != null) {
                        if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                            isPraise = true;
                        } else {
                            isPraise = false;
                        }
                    }
                    Message message = new Message();
                    message.what = PRAISE_FINISH;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                isPraise = false;
                Message message = new Message();
                message.what = PRAISE_FINISH;
                handler.sendMessage(message);
            }
        });
    }

    private void checkPraise() {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.ID, mPostId);
        body.put(Keys.PASSWORD, mPassword);
        Post.sendHttpRequest(ServerInformation.CHECK_PRAISE, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                if (returnData != null) {
                    if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                        isPraise = true;
                    } else {
                        isPraise = false;
                    }
                }
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(Exception e) {
                isPraise = false;
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }
        });
    }

    private void checkStar() {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ID, mPostId);
        body.put(Keys.PASSWORD, mPassword);
        body.put(Keys.ACCOUNT, mAccount);
        Post.sendHttpRequest(ServerInformation.CHECK_STAR, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    HashMap returnData = ToHashMap.getInstance().transform(response);
                    if (returnData != null) {
                        if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                            isStar = true;
                        } else {
                            isStar = false;
                        }
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

    @Override
    public String getPostId() {
        return mPostId;
    }

    private void setPagerClickable(boolean value) {
        mBackward.setClickable(value);
        mForward.setClickable(value);
        if (mIndex == 0) {
            mForward.setClickable(false);
        }
        if (mIndex == (mTotalPages - 1)) {
            mBackward.setClickable(false);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        refreshData();
    }
}
