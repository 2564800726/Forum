package com.blogofyb.forum.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
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
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;
import com.blogofyb.forum.utils.http.Post;
import com.blogofyb.forum.utils.json.ToHashMap;

import java.util.Calendar;
import java.util.HashMap;

public class EditUserInformationActivity extends BaseActivity implements View.OnClickListener {
    private final int SAVE_USER_INFORMATION_SUCCESS = 0;
    private final int SAVE_USER_INFORMATION_FAILED = 1;

    private EditText mNickName;
    private EditText mYear;
    private EditText mMonth;
    private EditText mDay;
    private EditText mSignature;

    private TextView mMale;
    private TextView mFemale;

    private HashMap<String, String> mBody = new HashMap<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case SAVE_USER_INFORMATION_SUCCESS:
                    saveUserInformationSuccess();
                    break;
                case SAVE_USER_INFORMATION_FAILED:
                    Toast.makeText(EditUserInformationActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceBundle) {
        super.onCreate(saveInstanceBundle);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_edit_user_information);
        mNickName = findViewById(R.id.et_nic_name);
        mYear = findViewById(R.id.et_birthday_year);
        mMonth = findViewById(R.id.et_birthday_month);
        mDay = findViewById(R.id.et_birthday_day);
        mSignature = findViewById(R.id.et_signature);

        mMale = findViewById(R.id.tv_male);
        mMale.setOnClickListener(this);
        mFemale = findViewById(R.id.tv_female);
        mFemale.setOnClickListener(this);

        Button mSaveUserInformation = findViewById(R.id.btn_save_user_information);
        mSaveUserInformation.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setTitle("修改资料");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitiesManager.removeActivity(EditUserInformationActivity.this);
            }
        });

        String userName = null;
        String gender = null;
        String birthday = null;
        String signature = null;

        SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(this);
        Cursor cursor = database.query(SQLite.TABLE_NAME,
                new String[]{SQLite.USER_NAME, SQLite.GENDER, SQLite.BIRTHDAY, SQLite.SIGNATURE, SQLite.ACCOUNT, SQLite.PASSWORD},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            userName = cursor.getString(cursor.getColumnIndex(SQLite.USER_NAME));
            gender = cursor.getString(cursor.getColumnIndex(SQLite.GENDER));
            birthday = cursor.getString(cursor.getColumnIndex(SQLite.BIRTHDAY));
            signature = cursor.getString(cursor.getColumnIndex(SQLite.SIGNATURE));
            mBody.put(Keys.ACCOUNT, cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT)));
            mBody.put(Keys.PASSWORD, cursor.getString(cursor.getColumnIndex(SQLite.PASSWORD)));
        }
        cursor.close();

        mNickName.setText(userName);
        if ("female".equals(gender)) {
            Drawable drawable1 = getResources().getDrawable(R.drawable.un_select);
            drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());
            mMale.setCompoundDrawables(null, null, drawable1, null);
            Drawable drawable2 = getResources().getDrawable(R.drawable.selected);
            drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
            mFemale.setCompoundDrawables(null, null, drawable2, null);
            mBody.put(Keys.GENDER, "female");
        } else {
            Drawable drawable1 = getResources().getDrawable(R.drawable.selected);
            drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());
            mMale.setCompoundDrawables(null, null, drawable1, null);
            Drawable drawable2 = getResources().getDrawable(R.drawable.un_select);
            drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
            mFemale.setCompoundDrawables(null, null, drawable2, null);
            mBody.put(Keys.GENDER, "male");
        }
        if (birthday != null && birthday.contains("-")) {
            mYear.setText(birthday.split("-")[0]);
            mMonth.setText(birthday.split("-")[1]);
            mDay.setText(birthday.split("-")[2]);
        }
        mSignature.setText(signature);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save_user_information:
                mBody.put(Keys.NIC_NAME, mNickName.getText().toString());
                mBody.put(Keys.BIRTHDAY, mYear.getText().toString() + "-"
                        + mMonth.getText().toString() + "-"
                        + mDay.getText().toString());
                mBody.put(Keys.SIGNATURE, mSignature.getText().toString());
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentDay = calendar.get(Calendar.DATE);
                int year = Integer.parseInt(mYear.getText().toString());
                int month = Integer.parseInt(mMonth.getText().toString());
                int day = Integer.parseInt(mDay.getText().toString());
                // 年龄（待）
                if (currentYear >= year && currentMonth >= month && currentDay >= day) {
                    mBody.put(Keys.AGE, String.valueOf(currentYear - year));
                } else if (currentYear >= year && currentMonth > month) {
                    mBody.put(Keys.AGE, String.valueOf(currentYear - year));
                } else if (currentYear >= year) {
                    mBody.put(Keys.AGE, String.valueOf(currentYear - year));
                } else {
                    mBody.put(Keys.AGE, String.valueOf(currentYear - year - 1));
                }
                saveUserInformation();
                break;
            case R.id.tv_male:
                Drawable drawable1 = getResources().getDrawable(R.drawable.selected);
                drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());
                mMale.setCompoundDrawables(null, null, drawable1, null);
                Drawable drawable2 = getResources().getDrawable(R.drawable.un_select);
                drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
                mFemale.setCompoundDrawables(null, null, drawable2, null);
                mBody.put(Keys.GENDER, "male");
                break;
            case R.id.tv_female:
                Drawable drawable3 = getResources().getDrawable(R.drawable.un_select);
                drawable3.setBounds(0, 0, drawable3.getMinimumWidth(), drawable3.getMinimumHeight());
                mMale.setCompoundDrawables(null, null, drawable3, null);
                Drawable drawable4 = getResources().getDrawable(R.drawable.selected);
                drawable4.setBounds(0, 0, drawable4.getMinimumWidth(), drawable4.getMinimumHeight());
                mFemale.setCompoundDrawables(null, null, drawable4, null);
                mBody.put(Keys.GENDER, "female");
                break;
        }
    }

    private void saveUserInformation() {
        Post.sendHttpRequest(ServerInformation.UPDATE_USER_INFORMATION, mBody, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                if (response != null && ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                    Message message = new Message();
                    message.what = SAVE_USER_INFORMATION_SUCCESS;
                    handler.sendMessage(message);
                    return;
                }
                onFailure(null);
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                message.what = SAVE_USER_INFORMATION_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void saveUserInformationSuccess() {
        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        ActivitiesManager.removeActivity(this);
    }
}
