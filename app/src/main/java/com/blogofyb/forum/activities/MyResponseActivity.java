package com.blogofyb.forum.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.blogofyb.forum.R;

public class MyResponseActivity extends BaseActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_subscribe_user);

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
    }
}
