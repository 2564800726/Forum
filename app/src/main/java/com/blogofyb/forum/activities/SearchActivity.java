package com.blogofyb.forum.activities;

import android.os.Bundle;

public class SearchActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
    }
}
