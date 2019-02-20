package com.blogofyb.forum.interfaces;

public interface HttpCallbackListener {

    void onFinish(String response);

    void onFailure(Exception e);
}
