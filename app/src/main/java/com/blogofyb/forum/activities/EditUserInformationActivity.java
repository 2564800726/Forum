package com.blogofyb.forum.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.utils.http.Post;

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
        mFemale = findViewById(R.id.tv_female);

        Intent intent = getIntent();
        if (intent != null) {
            mNickName.setHint(intent.getStringExtra("nickName"));
            mBody.put(Keys.NIC_NAME, intent.getStringExtra("nickName"));
            if ("female".equals(intent.getStringExtra(Keys.GENDER))) {
                mMale.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.un_select), null);
                mFemale.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.selected), null);
                mBody.put(Keys.GENDER, "female");
            } else {
                mMale.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.selected), null);
                mFemale.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.un_select), null);
                mBody.put(Keys.GENDER, "male");
            }
            if (!"".equals(intent.getStringExtra(Keys.BIRTHDAY)) && intent.getStringExtra(Keys.BIRTHDAY) != null) {
                mYear.setHint(intent.getStringExtra(Keys.BIRTHDAY).split("-")[0]);
                mMonth.setHint(intent.getStringExtra(Keys.BIRTHDAY).split("-")[1]);
                mDay.setHint(intent.getStringExtra(Keys.BIRTHDAY).split("-")[2]);
                mBody.put(Keys.BIRTHDAY, intent.getStringExtra(Keys.BIRTHDAY));
            }
            mSignature.setHint(intent.getStringExtra(Keys.SIGNATURE));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save_user_information:
                check();
                break;
            case R.id.tv_male:
                mMale.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.selected), null);
                mFemale.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.un_select), null);
                mBody.put(Keys.GENDER, "male");
                break;
            case R.id.tv_female:
                mMale.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.un_select), null);
                mFemale.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.selected), null);
                mBody.put(Keys.GENDER, "female");
                break;
        }
    }

    private void saveUserInformation() {
        Post.sendHttpRequest(ServerInformation.UPDATE_USER_INFORMATION, mBody, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Message message = new Message();
                message.what = SAVE_USER_INFORMATION_SUCCESS;
                handler.sendMessage(message);
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
        finish();
    }

    private void check() {
        if (mNickName.getText() == null || "".equals(mNickName.getText().toString())) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
        } else if (mYear.getText() == null || "".equals(mYear.getText().toString())
                || mMonth.getText() == null || "".equals(mMonth.getText().toString())
                || mDay.getText() == null || "".equals(mDay.getText().toString())) {  // 检查日期是否合法（待）
            Toast.makeText(this, "出生日期不能为空", Toast.LENGTH_SHORT).show();
        } else {
            mBody.put(Keys.NIC_NAME, mNickName.getText().toString());
            mBody.put(Keys.BIRTHDAY, mYear.getText().toString() + "-"
                    + mMonth.getText().toString() + "-"
                    + mDay.getText().toString());
            if (mSignature.getText() == null || "".equals(mSignature.getText().toString())) {
                mBody.put(Keys.SIGNATURE, getResources().getString(R.string.user_signature));
            } else {
                mBody.put(Keys.SIGNATURE, mSignature.getText().toString());
            }
            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentDay = calendar.get(Calendar.DATE);
            int year = Integer.parseInt(mYear.getText().toString());
            int month = Integer.parseInt(mMonth.getText().toString());
            int day = Integer.parseInt(mDay.getText().toString());
            if (currentYear >= year && currentMonth >= month && currentDay >= day) {
                mBody.put(Keys.AGE, (currentYear - year) + "");
            } else {
                mBody.put(Keys.AGE, (currentYear - year) + "");
            }
            saveUserInformation();
        }
    }
}
