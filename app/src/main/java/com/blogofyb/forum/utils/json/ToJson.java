package com.blogofyb.forum.utils.json;

import android.os.Build;

import com.blogofyb.forum.utils.constant.Keys;

import org.json.JSONObject;

import java.util.HashMap;

public class ToJson {
    public static ToJson transformer = null;

    public synchronized static ToJson getInstance() {
        if (transformer == null) {
            transformer = new ToJson();
        }
        return transformer;
    }

    public String transform(HashMap<String, String> keyValues) {
        JSONObject object = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            object.put(Keys.BRAND, Build.BRAND);
            object.put(Keys.SDK, Build.VERSION.SDK_INT);
            for (String key : keyValues.keySet()) {
                jsonObject.put(key, keyValues.get(key));
            }
            object.put(Keys.UPDATE_DATA, jsonObject);
            return object.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private ToJson() {}
}
