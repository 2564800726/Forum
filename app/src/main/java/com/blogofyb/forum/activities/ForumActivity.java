package com.blogofyb.forum.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.adpter.MyFragmentPagerAdapter;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;

public class ForumActivity extends BaseActivity implements View.OnClickListener {
    private ViewPager mViewPager;
    boolean flag = false;

    private ImageView mImgForum;
    private ImageView mImgSubscribe;
    private ImageView mImgMessage;
    private ImageView mImgZone;

    private TextView mTextForum;
    private TextView mTextSubscribe;
    private TextView mTextMessage;
    private TextView mTextZone;

    private Toolbar mToolbar;
    private FragmentManager manager;

    private boolean mIsTourist;
    private String mAccount;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.layout_forum);
        Intent intent = getIntent();
        if (intent != null) {
            mIsTourist = intent.getBooleanExtra("tourist", false);
        }
        SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(this);
        Cursor cursor = database.query(SQLite.TABLE_NAME, new String[] {SQLite.ACCOUNT},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            mAccount = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
        }
        cursor.close();
        ActivitiesManager.addActivity(this);
        initView();
    }

    @Override
    public void onClick(View view) {
        reset();
        switch (view.getId()) {
            case R.id.iv_forum:
            case R.id.tv_forum:
                selectItem(0);
                break;
            case R.id.iv_Subscribe:
            case R.id.tv_subscribe:
                selectItem(1);
                break;
            case R.id.iv_message:
            case R.id.tv_message:
                selectItem(2);
                break;
            case R.id.iv_zone:
            case R.id.tv_zone:
                selectItem(3);
                break;
        }
    }

    private void initView() {
        mToolbar = findViewById(R.id.tb_app);

        manager = getSupportFragmentManager();
        mViewPager = findViewById(R.id.vp_app);
        mViewPager.setAdapter(new MyFragmentPagerAdapter(manager));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                reset();
                selectItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mImgForum = findViewById(R.id.iv_forum);
        mImgForum.setOnClickListener(this);
        mImgSubscribe = findViewById(R.id.iv_Subscribe);
        mImgSubscribe.setOnClickListener(this);
        mImgMessage = findViewById(R.id.iv_message);
        mImgMessage.setOnClickListener(this);
        mImgZone = findViewById(R.id.iv_zone);
        mImgZone.setOnClickListener(this);

        mTextForum = findViewById(R.id.tv_forum);
        mTextForum.setOnClickListener(this);
        mTextSubscribe = findViewById(R.id.tv_subscribe);
        mTextSubscribe.setOnClickListener(this);
        mTextMessage = findViewById(R.id.tv_message);
        mTextMessage.setOnClickListener(this);
        mTextZone = findViewById(R.id.tv_zone);
        mTextZone.setOnClickListener(this);

        selectItem(0);
    }

    private void reset() {
        mImgForum.setImageResource(R.drawable.forum_normal);
        mImgSubscribe.setImageResource(R.drawable.subscribe_normal);
        mImgMessage.setImageResource(R.drawable.message_normal);
        mImgZone.setImageResource(R.drawable.zone_normal);

        mTextForum.setTextColor(getResources().getColor(R.color.colorText));
        mTextSubscribe.setTextColor(getResources().getColor(R.color.colorText));
        mTextMessage.setTextColor(getResources().getColor(R.color.colorText));
        mTextZone.setTextColor(getResources().getColor(R.color.colorText));
    }

    // 设置tab
    private void selectItem(int id) {
        switch (id) {
            case 0:
                mImgForum.setImageResource(R.drawable.forum_selected);
                mTextForum.setTextColor(getResources().getColor(R.color.colorTheme));
                mViewPager.setCurrentItem(id);
                setToolbar(id);
                break;
            case 1:
                mImgSubscribe.setImageResource(R.drawable.subscribe_selected);
                mTextSubscribe.setTextColor(getResources().getColor(R.color.colorTheme));
                mViewPager.setCurrentItem(id);
                setToolbar(id);
                break;
            case 2:
                mImgMessage.setImageResource(R.drawable.message_selected);
                mTextMessage.setTextColor(getResources().getColor(R.color.colorTheme));
                mViewPager.setCurrentItem(id);
                setToolbar(id);
                break;
            case 3:
                mImgZone.setImageResource(R.drawable.zone_selected);
                mTextZone.setTextColor(getResources().getColor(R.color.colorTheme));
                mViewPager.setCurrentItem(id);
                setToolbar(id);
                break;
        }
    }

    // 设置Toolbar的NavigationIcon和点击事件

    private void setToolbar(int id) {
        switch (id) {
            case 0:
                if (!mIsTourist) {
                    mToolbar.setNavigationIcon(R.drawable.subscribe);
                    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ForumActivity.this, SubscribePlateActivity.class);
                            intent.putExtra(Keys.ACCOUNT, mAccount);
                            startActivity(intent);
                        }
                    });
                } else {
                    mToolbar.removeAllViews();
                }
                break;
            case 1:
                mToolbar.removeAllViews();
                break;
            case 2:
                mToolbar.removeAllViews();
                break;
            case 3:
                if (mIsTourist) {
                    mToolbar.removeAllViews();
                } else {
                    mToolbar.setNavigationIcon(R.drawable.edit);
                    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ForumActivity.this, EditUserInformationActivity.class);
                            startActivity(intent);
                        }
                    });
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (flag) {
            ActivitiesManager.finishAllActivities();
        } else {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            flag = true;
        }
    }
}
