package com.blogofyb.forum.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.interfaces.GetPostId;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;
import com.blogofyb.forum.utils.http.Post;
import com.blogofyb.forum.utils.json.ToHashMap;

import java.util.HashMap;

public class CommentActivity extends BaseActivity {
    private final int POST_COMMENT_SUCCESS = 0;
    private final int POST_COMMENT_FAILED = 1;

    private TextView mAnotherUserName;
    private TextView mAnotherContent;
    private EditText mContent;
    private Button mSendComment;

    private String mPostId;
    private String mFloor;
    private String mAccount;
    private String mPassword;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case POST_COMMENT_SUCCESS:
                    ActivitiesManager.removeActivity(CommentActivity.this);
                    break;
                case POST_COMMENT_FAILED:
                    Toast.makeText(CommentActivity.this, "提交失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_comment);

        mAnotherContent = findViewById(R.id.tv_another_content);
        mAnotherUserName = findViewById(R.id.tv_another_user_name);
        Intent intent = getIntent();
        if (intent != null) {
            mPostId = ((GetPostId) ActivitiesManager.getPrior()).getPostId();
            mFloor = intent.getStringExtra(Keys.FLOOR);
            mAnotherUserName.setText(intent.getStringExtra(Keys.ANOTHER_USER_NAME));
            mAnotherContent.setText(intent.getStringExtra(Keys.ANOTHER_CONTENT));
        }
        SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(this);
        Cursor cursor = database.query(SQLite.TABLE_NAME, new String[]{SQLite.ACCOUNT, SQLite.PASSWORD},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            mAccount = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
            mPassword = cursor.getString(cursor.getColumnIndex(SQLite.PASSWORD));
        }
        cursor.close();

        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(CommentActivity.this);
            }
        });
        mContent = findViewById(R.id.et_comment);
        mSendComment = findViewById(R.id.btn_post_comment);
        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mContent.getText().toString();
                if ("".equals(content)) {
                    Toast.makeText(CommentActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                } else if (content.length() < 5) {
                    Toast.makeText(CommentActivity.this, "内容不能少于5个字", Toast.LENGTH_SHORT).show();
                } else {
                    postComment(content);
                }
            }
        });
    }

    private void postComment(String content) {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.FLOOR, mFloor);
        body.put(Keys.ID, mPostId);
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.PASSWORD, mPassword);
        body.put(Keys.POST_CONTENT, content);
        Post.sendHttpRequest(ServerInformation.POST_COMMENT, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                if (response != null) {
                    Message message = new Message();
                    if (ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                        message.what = POST_COMMENT_SUCCESS;
                    } else {
                        message.what = POST_COMMENT_FAILED;
                    }
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = POST_COMMENT_FAILED;
                handler.sendMessage(message);
            }
        });
    }
}
