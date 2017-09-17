package com.meitu.meiyancamera.policy;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * 控制自动推进速度@see #smoothScrollToPosition，
 * 重写@see #calculateSpeedPerPixel,返回移动每一个px的速度
 * Created by libiao on 2017/6/12.
 */
public class ScrollSpeedLinearLayoutManager extends LinearLayoutManager{

    private static final float MILLISECONDS_PER_INCH = 200;

    public ScrollSpeedLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext())
                {
                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                    }
                };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}
