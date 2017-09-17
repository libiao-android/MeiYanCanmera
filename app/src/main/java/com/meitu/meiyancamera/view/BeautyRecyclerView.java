package com.meitu.meiyancamera.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.meitu.meiyancamera.policy.BeautyFilterAdapter;

/**
 * 可以监听列表滑动状态的 RecyclerView
 * Created by libiao on 2017/6/12.
 */
public class BeautyRecyclerView extends RecyclerView{
    public BeautyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //将滑动状态传到adapter中
                ((BeautyFilterAdapter)getAdapter()).setScrollState(newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }
}
