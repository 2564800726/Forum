package com.blogofyb.forum.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.blogofyb.forum.R;

public class FloorDecoration extends RecyclerView.ItemDecoration {
    private Context mContext;
    private Paint mPaint;

    public FloorDecoration(Context mContext) {
        this.mContext = mContext;
        mPaint = new Paint();
        mPaint.setColor(mContext.getResources().getColor(R.color.colorBackground));
        mPaint.setStrokeWidth(dpToPx(1));
        mPaint.setAntiAlias(true);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        if (position < parent.getChildCount() - 1) {
            outRect.set(0, 0, 0, dpToPx(5));
        }
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        for (int i = 0; i < parent.getChildCount() - 1; i++) {
            View view = parent.getChildAt(i);
            c.drawLine(view.getLeft(), view.getBottom(), view.getRight(), view.getBottom(), mPaint);
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }
}
