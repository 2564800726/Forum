package com.blogofyb.forum.activities;

import android.content.Intent;
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
import com.blogofyb.forum.utils.constant.Keys;
import com.blogofyb.forum.utils.constant.Messages;
import com.blogofyb.forum.utils.constant.ServerInformation;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.http.Post;
import com.blogofyb.forum.utils.json.ToHashMap;

import java.util.HashMap;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private EditText account;
    private EditText password;
    private EditText confirmPassword;
    private EditText verificationCode;
    private TextView getVerificationCode;
    private Button register;

    private Handler handler = new Handler() {
        private void showMessage(Message message) {
            Toast.makeText(RegisterActivity.this, message.getData().getString(Keys.MESSAGE), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case Messages.REGISTER_SUCCESS:
                    // use database to save data
                    Intent intent = new Intent(RegisterActivity.this, ForumActivity.class);
                    startActivity(intent);
                    ActivitiesManager.finishAllActivities();
                    break;
                case Messages.REGISTER_FAILED:
                    setButtonClickable(true);
                    showMessage(message);
                    break;
                case Messages.GET_VERIFICATION_CODE_FAILED:
                    setTextViewClickable(true);
                    showMessage(message);
                    break;
                case Messages.GET_VERIFICATION_CODE_SUCCESS:
                    countDown();
                    showMessage(message);
                    break;
                case Messages.REFRESH:
                    refreshTextView(message.arg1);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.layout_register);
        ActivitiesManager.addActivity(this);
        account = findViewById(R.id.et_account);
        password = findViewById(R.id.et_password);
        confirmPassword = findViewById(R.id.et_confirm_password);
        verificationCode = findViewById(R.id.et_verification_code);
        getVerificationCode = findViewById(R.id.tv_get_verification_code);
        getVerificationCode.setOnClickListener(this);
        register = findViewById(R.id.btn_register);
        register.setOnClickListener(this);
        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivitiesManager.removeActivity(RegisterActivity.this);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register:
                if (account.getText().toString().length() < 11) {
                    Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    break;
                } else if (verificationCode.getText().toString().length() < 6) {
                    Toast.makeText(this, "请输入正确的验证码", Toast.LENGTH_SHORT).show();
                    break;
                } else if ("".equals(password.getText().toString())) {
                    Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    break;
                } else if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
                    Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    break;
                }
                setButtonClickable(false);
                register();
                break;
            case R.id.tv_get_verification_code:
                if (account.getText().toString().length() < 11) {
                    Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    break;
                }
                setTextViewClickable(false);
                getVerificationCode();
                break;
        }
    }

    private void register() {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.VERIFICATION_CODE, verificationCode.getText().toString());
        body.put(Keys.ACCOUNT, account.getText().toString());
        body.put(Keys.PASSWORD, password.getText().toString());
        Post.sendHttpRequest(ServerInformation.ADDRESS + "register", body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                HashMap keyValues = ToHashMap.getInstance().transform(response);
                if (keyValues != null) {
                    if (ServerInformation.SUCCESS.equals(keyValues.get(Keys.STATUS))) {
                        message.what = Messages.REGISTER_SUCCESS;
                    } else {
                        message.what = Messages.REGISTER_FAILED;
                        bundle.putString(Keys.MESSAGE, (String) keyValues.get(Keys.MESSAGE));
                    }
                } else {
                    message.what = Messages.REGISTER_FAILED;
                    bundle.putString(Keys.MESSAGE, "服务器异常");
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                message.what = Messages.REGISTER_FAILED;
                bundle.putString(Keys.MESSAGE, "注册失败");
                message.setData(bundle);
                handler.sendMessage(message);
            }
        });
    }

    private void getVerificationCode() {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ACCOUNT, account.getText().toString());
        Post.sendHttpRequest(ServerInformation.ADDRESS + "getVerificationCode", body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                HashMap keyValues = ToHashMap.getInstance().transform(response);
                if (keyValues != null) {
                    if (ServerInformation.SUCCESS.equals(keyValues.get(Keys.STATUS))) {
                        message.what = Messages.GET_VERIFICATION_CODE_SUCCESS;
                    } else {
                        message.what = Messages.GET_VERIFICATION_CODE_FAILED;
                    }
                    bundle.putString(Keys.MESSAGE, (String) keyValues.get(Keys.MESSAGE));
                } else {
                    message.what = Messages.GET_VERIFICATION_CODE_FAILED;
                    bundle.putString(Keys.MESSAGE, "服务器异常");
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Message message = new Message();
                Bundle bundle = new Bundle();
                message.what = Messages.GET_VERIFICATION_CODE_FAILED;
                bundle.putString(Keys.MESSAGE, "获取验证码失败");
                message.setData(bundle);
                handler.sendMessage(message);
            }
        });
    }

    private void countDown() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 59; i >= 0; i--) {
                    Message message = new Message();
                    message.what = Messages.REFRESH;
                    message.arg1 = i;
                    handler.sendMessage(message);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void refreshTextView(int seconds) {
        getVerificationCode.setText(seconds + "s");
        if (seconds == 0) {
            setTextViewClickable(true);
            getVerificationCode.setText("获取验证码");
        }
    }

    private void setTextViewClickable(boolean value) {
        getVerificationCode.setClickable(value);
    }

    private void setButtonClickable(boolean value) {
        register.setClickable(value);
    }
}
