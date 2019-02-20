package com.blogofyb.forum.activities;

import android.os.Bundle;

public class MyResponseActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
    }
}
