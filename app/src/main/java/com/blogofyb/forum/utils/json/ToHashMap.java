package com.blogofyb.forum.utils.json;

import com.blogofyb.forum.utils.constant.Keys;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class ToHashMap {
    public static ToHashMap transformer = null;

    public synchronized static ToHashMap getInstance() {
        if (transformer == null) {
            transformer = new ToHashMap();
        }
        return transformer;
    }

    public HashMap transform(String json) {
        HashMap<String, String> response = new HashMap<>();
        try {
            JSONObject object = new JSONObject(json);
            response.put(Keys.STATUS, object.getString(Keys.STATUS));
            response.put(Keys.MESSAGE, object.getString(Keys.MESSAGE));
            JSONObject jsonObject = object.getJSONObject(Keys.RETURN_DATA);
            for (Iterator<String> iterator = jsonObject.keys(); iterator.hasNext(); ) {
                String key = iterator.next();
                response.put(key, jsonObject.getString(key));
            }
            return response;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ToHashMap() {}
}
