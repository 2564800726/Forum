package com.blogofyb.forum.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText account;
    private EditText password;
    private EditText verificationCode;
    private TextView getVerificationCode;
    private Button login;

    private Handler handler = new Handler() {
        private void showMessage(Message message) {
            Toast.makeText(LoginActivity.this, message.getData().getString(Keys.MESSAGE), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case Messages.GET_VERIFICATION_CODE_SUCCESS:
                    countDown();
                    showMessage(message);
                    break;
                case Messages.GET_VERIFICATION_CODE_FAILED:
                    setTextViewClickable(true);
                    showMessage(message);
                    break;
                case Messages.LOGIN_SUCCESS:
                    SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                    editor.putBoolean("haveUser", true);
                    editor.apply();
                    // use database to save data
                    Intent intent = new Intent(LoginActivity.this, ForumActivity.class);
                    startActivity(intent);
                    ActivitiesManager.finishAllActivities();
                    break;
                case Messages.LOGIN_FAILED:
                    setButtonClickable(true);
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
        setContentView(R.layout.layout_login);
        Intent intent = getIntent();
        ActivitiesManager.addActivity(this);
        account = findViewById(R.id.et_account);
        password = findViewById(R.id.et_password);
        verificationCode = findViewById(R.id.et_verification_code);
        getVerificationCode = findViewById(R.id.tv_get_verification_code);
        getVerificationCode.setOnClickListener(this);
        login = findViewById(R.id.btn_login);
        login.setOnClickListener(this);
        TextView forgetPassword = findViewById(R.id.tv_forget_password);
        forgetPassword.setOnClickListener(this);
        Toolbar toolbar = findViewById(R.id.tb_app);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivitiesManager.removeActivity(LoginActivity.this);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                if (account.getText().toString().length() < 11) {
                    Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    break;
                } else if (verificationCode.getText().toString().length() < 6) {
                    Toast.makeText(this, "请输入正确的验证码", Toast.LENGTH_SHORT).show();
                    break;
                }
                setButtonClickable(false);
                login();
                break;
            case R.id.tv_get_verification_code:
                if (account.getText().toString().length() < 11) {
                    Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    break;
                }
                setTextViewClickable(false);
                getVerificationCode();
                break;
            case R.id.tv_forget_password:
                Intent intent = new Intent(this, FindPasswordActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void login() {
        final Message message = new Message();
        final Bundle bundle = new Bundle();
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ACCOUNT, account.getText().toString());
        body.put(Keys.PASSWORD, password.getText().toString());
        Post.sendHttpRequest(ServerInformation.ADDRESS + "login", body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap keyValues = ToHashMap.getInstance().transform(response);
                if (keyValues != null) {
                    if (ServerInformation.SUCCESS.equals(keyValues.get(Keys.STATUS))) {
                        message.what = Messages.LOGIN_SUCCESS;
                    } else {
                        setTextViewClickable(true);
                        message.what = Messages.LOGIN_FAILED;
                        bundle.putString(Keys.MESSAGE, (String) keyValues.get(Keys.MESSAGE));
                    }
                } else {
                    message.what = Messages.LOGIN_FAILED;
                    bundle.putString(Keys.MESSAGE, "服务器异常");
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(Exception e) {
                message.what = Messages.LOGIN_FAILED;
                bundle.putString(Keys.MESSAGE, "登陆失败");
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
                        bundle.putString(Keys.MESSAGE, (String) keyValues.get(Keys.MESSAGE));
                    }
                } else {
                    message.what = Messages.GET_VERIFICATION_CODE_FAILED;
                    bundle.putString(Keys.MESSAGE, "服务器异常");
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                message.what = Messages.GET_VERIFICATION_CODE_FAILED;
                bundle.putString(Keys.MESSAGE, "获取验证码失败");
                message.setData(bundle);
                handler.sendMessage(message);
            }
        });
    }

    private void setButtonClickable(boolean value) {
        login.setClickable(value);
    }

    private void setTextViewClickable(boolean value) {
        getVerificationCode.setClickable(value);
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
}
