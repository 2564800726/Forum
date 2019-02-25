package com.blogofyb.forum.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.blogofyb.forum.R;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.Messages;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;
import com.blogofyb.forum.utils.http.Post;

import java.util.HashMap;

public class WelcomeActivity extends BaseActivity {
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case Messages.FINISH_CHECK:
                    Intent intent = new Intent(WelcomeActivity.this, SelectActivity.class);
                    startActivity(intent);
                    ActivitiesManager.removeActivity(WelcomeActivity.this);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.layout_welcome);
        ActivitiesManager.addActivity(this);
        checkUser();
    }

    private void checkUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                if (sharedPreferences.getBoolean("haveUser", false)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(WelcomeActivity.this);
                    Cursor cursor = database.query(SQLite.TABLE_NAME, new String[] {SQLite.ACCOUNT, SQLite.PASSWORD},
                            null, null, null, null, null);
                    String account = null;
                    String password = null;
                    while (cursor.moveToNext()) {
                        account = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
                        password = cursor.getString(cursor.getColumnIndex(SQLite.PASSWORD));
                    }
                    cursor.close();
                    autoLogin(account, password);
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    message.what = Messages.FINISH_CHECK;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    private void autoLogin(final String account, String password) {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ACCOUNT, account);
        body.put(Keys.PASSWORD, password);
        body.put(Keys.VERIFICATION_CODE, "forum");
        Post.sendHttpRequest(ServerInformation.ADDRESS + "login", body, new HttpCallbackListener() {
            private Intent intent = new Intent(WelcomeActivity.this, ForumActivity.class);
            @Override
            public void onFinish(String response) {
                // 非游客登陆
                intent.putExtra("tourist", false);
                intent.putExtra(Keys.ACCOUNT, account);
                startActivity(intent);
                ActivitiesManager.removeActivity(WelcomeActivity.this);
            }

            @Override
            public void onFailure(Exception e) {
                // 游客登陆
                intent.putExtra("tourist", true);
                startActivity(intent);
                ActivitiesManager.removeActivity(WelcomeActivity.this);
            }
        });
    }

    @Override
    protected void setNotificationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){

        }
    }
}
