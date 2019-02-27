package com.blogofyb.forum.adpter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogofyb.forum.R;
import com.blogofyb.forum.beans.PlateBean;
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

public class SubscribePlateAdapter extends RecyclerView.Adapter<SubscribePlateAdapter.PlateHolder> {
    private final int SUBSCRIBE_SUCCESS = 0;
    private final int SUBSCRIBE_FAILED = 1;

    private List<PlateBean> mPlates;
    private ImageLoader mImageLoader;
    private String mAccount;
    private String mPassword;
    private String mSubscribe;
    private Handler mHandler;

    public SubscribePlateAdapter(Context mContext, List<PlateBean> mPlates, String mSubscribe, Handler mHandler) {
        this.mPlates = mPlates;
        mImageLoader = new ImageLoader(mContext);
        this.mSubscribe = mSubscribe;
        this.mHandler = mHandler;
    }

    @NonNull
    @Override
    public PlateHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subscribe_plate, viewGroup, false);
        SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(view.getContext());
        Cursor cursor = database.query(SQLite.TABLE_NAME, new String[]{SQLite.ACCOUNT, SQLite.PASSWORD},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            mAccount = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
            mPassword = cursor.getString(cursor.getColumnIndex(SQLite.PASSWORD));
        }
        cursor.close();
        return new PlateHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlateHolder plateHolder, final int i) {
        plateHolder.mTextView.setText(mPlates.get(i).getPlateName());

        mImageLoader.set(plateHolder.mImageView, mPlates.get(i).getIcon());
        if (mSubscribe != null && mSubscribe.contains(";" + mPlates.get(i).getId() + ";")) {
            plateHolder.mButton.setText("已订阅");
        } else {
            plateHolder.mButton.setText("订阅");
        }

        plateHolder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribePlate(mPlates.get(plateHolder.getAdapterPosition()).getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlates.size();
    }

    private void subscribePlate(String id) {
        HashMap<String, String> body = new HashMap<>();
        body.put(Keys.ACCOUNT, mAccount);
        body.put(Keys.PASSWORD, mPassword);
        body.put(Keys.ID, id);
        Post.sendHttpRequest(ServerInformation.SUBSCRIBE, body, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                HashMap returnData = ToHashMap.getInstance().transform(response);
                if (returnData != null && ServerInformation.SUCCESS.equals(returnData.get(Keys.STATUS))) {
                    Message message = new Message();
                    message.what = SUBSCRIBE_SUCCESS;
                    mHandler.sendMessage(message);
                    return;
                }
                onFailure(null);
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                message.what = SUBSCRIBE_FAILED;
                mHandler.sendMessage(message);
            }
        });
    }

    public void refreshData(List<PlateBean> mPlates, String mSubscribe) {
        this.mPlates = mPlates;
        this.mSubscribe = mSubscribe;
        notifyDataSetChanged();
    }

    static class PlateHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;
        private Button mButton;

        public PlateHolder(View view) {
            super(view);
            mImageView = view.findViewById(R.id.iv_plate_icon_Subscribe);
            mTextView = view.findViewById(R.id.tv_plate_name_Subscribe);
            mButton = view.findViewById(R.id.btn_subscribe_plate);
        }
    }
}
