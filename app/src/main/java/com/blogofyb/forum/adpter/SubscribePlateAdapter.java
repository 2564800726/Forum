package com.blogofyb.forum.adpter;

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
import com.blogofyb.forum.utils.img.ImageLoader;

import java.util.List;

public class SubscribePlateAdapter extends RecyclerView.Adapter<SubscribePlateAdapter.PlateHolder> {
    private List<PlateBean> mPlates;
    private ImageLoader mImageLoader;

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
    public void onBindViewHolder(@NonNull PlateHolder plateHolder, int i) {
        plateHolder.mTextView.setText(mPlates.get(i).getPlateName());

        mImageLoader.set(plateHolder.mImageView, mPlates.get(i).getIcon());

        plateHolder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 读取数据库
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlates.size();
    }

    static class PlateHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;
        private Button mButton;

        public PlateHolder(View view) {
            super(view);
            mImageView = view.findViewById(R.id.iv_plate_icon_Subscribe);
            mTextView = view.findViewById(R.id.tv_plate_name_Subscribe);
            mButton = view.findViewById(R.id.btn_Subscribe_plate);
        }
    }
}
