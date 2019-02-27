package com.blogofyb.forum.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.blogofyb.forum.R;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;

public class SettingActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_setting);

        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(SettingActivity.this);
            }
        });
        toolbar.setTitle(getResources().getString(R.string.setting));
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        TextView logout = findViewById(R.id.tv_logout);
        logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tb_app:
                ActivitiesManager.removeActivity(this);
                break;
            case R.id.tv_message_setting:
                // 消息设置（待）
                break;
            case R.id.tv_logout:
                logout();
                break;
        }
    }

    private void logout() {
        String sql = "DELETE FROM " + SQLite.TABLE_NAME + ";";
        SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(this);
        database.execSQL(sql);
        Intent intent = new Intent(this, SelectActivity.class);
        startActivity(intent);
        ActivitiesManager.finishAllActivities();
    }
}
