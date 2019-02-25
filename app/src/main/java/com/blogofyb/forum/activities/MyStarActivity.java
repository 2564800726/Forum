package com.blogofyb.forum.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.blogofyb.forum.R;

public class MyStarActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_my_star);
        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(MyStarActivity.this);
            }
        });
    }
}
