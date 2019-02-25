package com.blogofyb.forum.adpter;

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
import com.blogofyb.forum.utils.constant.ServerInformation;
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
    private Button mSubscribePlate;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case SUBSCRIBE_SUCCESS:
                    setButtonClickable(true);
                    mSubscribePlate.setText("已订阅");
                    break;
                case SUBSCRIBE_FAILED:
                    setButtonClickable(true);
                    break;
            }
        }
    };

    public SubscribePlateAdapter(List<PlateBean> mPlates) {
        this.mPlates = mPlates;
        mImageLoader = new ImageLoader();

    }

    @NonNull
    @Override
    public PlateHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subscribe_plate, viewGroup, false);
        return new PlateHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlateHolder plateHolder, final int i) {
        plateHolder.mTextView.setText(mPlates.get(i).getPlateName());

        mImageLoader.set(plateHolder.mImageView, mPlates.get(i).getIcon());

        mSubscribePlate = plateHolder.mButton;

        plateHolder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonClickable(false);
                subscribePlate(mPlates.get(i).getId());
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
                    handler.sendMessage(message);
                    return;
                }
                onFailure(null);
            }

            @Override
            public void onFailure(Exception e) {
                Message message = new Message();
                message.what = SUBSCRIBE_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private void setButtonClickable(boolean value) {
        mSubscribePlate.setClickable(value);
    }

    public void refreshData(List<PlateBean> mPlates) {
        this.mPlates = mPlates;
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
