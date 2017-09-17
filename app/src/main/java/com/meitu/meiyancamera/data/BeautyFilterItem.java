package com.meitu.meiyancamera.data;

import android.graphics.Bitmap;

import com.meitu.meiyancamera.util.Constant;

/**
 * 美颜效果列表的Item对象
 * 每一个Item代表一种美颜效果信息
 * Created by libiao on 2017/6/7.
 */
public class BeautyFilterItem {
    //美颜模式
    private BeautyMode mBeautyMode;
    //显示透明度，取值0-255
    private int mImageAlpha;
    //是否要被缓存
    private boolean mCache;

    public BeautyFilterItem(BeautyMode mode){
        mBeautyMode = mode;
        mImageAlpha = mode.getAlpha() * Constant.ALPHA_MAX / Constant.PROGRESS_MAX ;
        mCache = false;
    }

    public BeautyMode getBeautyMode() {
        return mBeautyMode;
    }

    public int getImageAlpha() {
        return mImageAlpha;
    }

    public void setImageAlpha(int alpha) {
        this.mImageAlpha = alpha;
    }


    public boolean isCache() {
        return mCache;
    }

    public void setIsCache(boolean cache) {
        this.mCache = cache;
    }
}
