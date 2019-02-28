package com.blogofyb.forum.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;
import com.blogofyb.forum.utils.http.Post;
import com.blogofyb.forum.utils.img.ImageLoader;
import com.blogofyb.forum.utils.json.ToHashMap;

import java.util.HashMap;
import java.util.List;

public class WritePostActivity extends BaseActivity {
    private final int POST_POST_SUCCESS = 0;
    private final int POST_POST_FAILED = 1;
    private final int SET_POST_ICON = 2;

    private String mPlateId;
    private String mAccount;
    private String mPassword;
    private String mPostIconUrl;
    private boolean isHavePic = false;
    private List<HashMap<String, String>> mPlates;

    private EditText mPostTitle;
    private EditText mPostContent;
    private TextView mPlateName;
    private ImageView mPostIcon;
    private Button mPostPost;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case POST_POST_SUCCESS:
                    setButtonClickable(true);
                    ActivitiesManager.removeActivity(WritePostActivity.this);
                    break;
                case POST_POST_FAILED:
                    setButtonClickable(true);
                    Toast.makeText(WritePostActivity.this, "提交失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_write_post);
        final Intent intent = getIntent();
        mPlateName = findViewById(R.id.tv_plate);
        if (intent != null) {
            mPlateId = intent.getStringExtra(Keys.ID);
            mPlateName.setText(intent.getStringExtra(Keys.PLATE_NAME));
            SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(this);
            Cursor cursor = database.query(SQLite.TABLE_NAME, new String[] {SQLite.ACCOUNT, SQLite.PASSWORD},
                    null, null    , null, null, null  );
            while (cursor.moveToNext()) {
                mAccount = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
                mPassword = cursor.getString(cursor.getColumnIndex(SQLite.PASSWORD));
            }
            cursor.close();
        }
        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(WritePostActivity.this);
            }
        });

        mPostTitle = findViewById(R.id.et_post_title);
        mPostContent = findViewById(R.id.et_post_content);
        mPostIcon = findViewById(R.id.iv_post_pic);
        mPostIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WritePostActivity.this, SelectImageActivity.class);
                intent.putExtra("key", "POS");
                startActivityForResult(intent, SET_POST_ICON);
            }
        });
        mPostPost = findViewById(R.id.btn_post_post);
        mPostPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonClickable(false);
                String title = mPostTitle.getText().toString();
                String content = mPostContent.getText().toString();
                if ("".equals(title)) {
                    Toast.makeText(WritePostActivity.this, "标题不能为空", Toast.LENGTH_SHORT).show();
                } else if ("".equals(content)) {
                    Toast.makeText(WritePostActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                } else if (title.length() < 5) {
                    Toast.makeText(WritePostActivity.this, "标题长度不能小于5个字符", Toast.LENGTH_SHORT).show();
                } else if (content.length() < 5) {
                    Toast.makeText(WritePostActivity.this, "内容不能小于5个字符", Toast.LENGTH_SHORT).show();
                }
                postPost(title, content);
            }
        });
    }

    private void postPost(String title, String content) {
        final HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.PASSWORD, mPassword);
        body.put(Keys.ID, mPlateId);
        body.put(Keys.POST_CONTENT, content);
        body.put(Keys.POST_TITLE, title);
        if (isHavePic) {
            body.put(Keys.ICON, mPostIconUrl);
        } else {
            body.put(Keys.ICON, "");
        }
        Post.sendHttpRequest(ServerInformation.POST_POST, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                if (response != null) {
                    if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                        Message message = new Message();
                        message.what = POST_POST_SUCCESS;
                        handler.sendMessage(message);
                        return;
                    }
                }
                onFailure(null);
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                message.what = POST_POST_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void setButtonClickable(boolean value) {
        mPostPost.setClickable(value);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case SET_POST_ICON:
                if (resultCode == RESULT_OK && data != null) {
                    mPostIconUrl = "http://129.204.3.245/" + data.getStringExtra("md5") + ".png";
                    isHavePic = true;
                    new ImageLoader(this).set(mPostIcon, mPostIconUrl);
                }
                break;
        }
    }
}
