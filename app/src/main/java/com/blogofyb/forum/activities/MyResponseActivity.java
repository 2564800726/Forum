package com.blogofyb.forum.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ImageView;

import com.blogofyb.forum.R;

public class MyResponseActivity extends BaseActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_my_star);
    }
}
