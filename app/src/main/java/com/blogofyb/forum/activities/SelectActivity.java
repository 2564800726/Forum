package com.blogofyb.forum.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blogofyb.forum.R;

public class SelectActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.layout_select);
        ActivitiesManager.addActivity(this);
        Button login = findViewById(R.id.btn_login);
        login.setOnClickListener(this);
        Button register = findViewById(R.id.btn_register);
        register.setOnClickListener(this);
        TextView touristLogin = findViewById(R.id.tv_tourist_login);
        touristLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                Intent intentForLogin = new Intent(this, LoginActivity.class);
                startActivity(intentForLogin);
                break;
            case R.id.btn_register:
                Intent intentForRegister = new Intent(this, RegisterActivity.class);
                startActivity(intentForRegister);
                break;
            case R.id.tv_tourist_login:
                Intent intent = new Intent(this, ForumActivity.class);
                intent.putExtra("tourist", true);
                startActivity(intent);
                ActivitiesManager.finishAllActivities();
                break;
        }
    }
}
