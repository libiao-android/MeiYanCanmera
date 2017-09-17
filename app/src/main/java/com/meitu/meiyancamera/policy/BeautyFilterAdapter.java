package com.meitu.meiyancamera.policy;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.meitu.core.processor.FilterProcessor;
import com.meitu.core.types.NativeBitmap;
import com.meitu.meiyancamera.R;
import com.meitu.meiyancamera.data.BeautyFilterItem;
import com.meitu.meiyancamera.data.BeautyMode;

import java.util.ArrayList;

/**
 * 美颜效果列表的适配器
 * Created by libiao on 2017/6/6.
 */
public class BeautyFilterAdapter extends RecyclerView.Adapter<BeautyFilterAdapter.FilterViewHolder> implements View.OnClickListener{
    private Context mContext;
    //列表填充的数据
    private ArrayList<BeautyFilterItem> mFilterList;
    //美颜操作的管理类对象
    private BeautyPhotoOptionManager mOptionManager;
    //原图Bitmap对象
    private Bitmap mSrcThumbnail;
    //原图NativeBitmap对象
    private NativeBitmap mThumbnailNativeBitmap;
    //Item的点击回调
    private OnItemClickListener mOnItemClickListener;
    //当前选择的Item对象
    private BeautyFilterItem mSeletedItem;
    //列表滑动状态
    private int mScrollState = RecyclerView.SCROLL_STATE_IDLE;

    //内存管理，缓存美颜效果缩略图
    private LruCache<BeautyMode, Bitmap> mBitmapLruCache;
    private static final float LOAD_ALPHA = 1;
    //缓存大小为应用总内存的1/16
    private static final int MEMORY_SCALE = 16;

    /**
     * 根据枚举类@see #BeautyMode 初始化 mFilterList
     * @param context
     * @param manager
     */
    public BeautyFilterAdapter(Context context, BeautyPhotoOptionManager manager){
        mContext = context;
        mOptionManager = manager;

        mSrcThumbnail = mOptionManager.getCurrentThumbnail();
        mThumbnailNativeBitmap = NativeBitmap.createBitmap();
        mThumbnailNativeBitmap.setImage(mSrcThumbnail);
        mFilterList = new ArrayList<>(BeautyMode.values().length);
        for(BeautyMode mode : BeautyMode.values()){
            mFilterList.add(new BeautyFilterItem(mode));
        }
        mSeletedItem = mFilterList.get(0);
        //获取系统分配给每个应用程序的最大内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / MEMORY_SCALE;
        //给LruCache分配内存1/16
        mBitmapLruCache = new LruCache<BeautyMode, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(BeautyMode key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }
    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FilterViewHolder holder = new FilterViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.beauty_filter_item, parent,false));
        holder.itemFl.setOnClickListener(this);
        return holder;
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }

    @Override
    public void onBindViewHolder(FilterViewHolder holder, int position) {
        BeautyFilterItem item = mFilterList.get(position);
        BeautyMode mode = item.getBeautyMode();
        holder.itemTev.setText(mode.getMessage(mContext));
        holder.itemTev.setBackgroundResource(mode.getColor());
        holder.itemFl.setTag(position);
        //第一次BindViewHolder时item.getBeautyThumbnail()为空，通过FilterImageLoadTask加载
        Bitmap image = mBitmapLruCache.get(mode);
        if(image == null){
            holder.itemImv.setImageBitmap(mSrcThumbnail);
            holder.selectedIv.setVisibility(View.GONE);
            holder.itemImvCover.setVisibility(View.GONE);
            //列表正在滑动，不刷新，停止滑动再刷新
            if(mScrollState == RecyclerView.SCROLL_STATE_IDLE){
                FilterImageLoadTask task = new FilterImageLoadTask();
                task.execute(item);
            }
        }else{
            holder.itemImv.setImageBitmap(image);
            //点击Item相关UI变化
            if(mSeletedItem.getBeautyMode() == mode){
                holder.itemImvCover.setBackgroundResource(mode.getColor());
                holder.selectedIv.setVisibility(View.VISIBLE);
                holder.itemImvCover.setVisibility(View.VISIBLE);
            }else{
                holder.selectedIv.setVisibility(View.GONE);
                holder.itemImvCover.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v,mFilterList.get((int)v.getTag()));
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    /**
     * 设置滑动状态，停止滑动后，刷新列表
     * @param scrollState
     */
    public void setScrollState(int scrollState){
        mScrollState = scrollState;
        if(scrollState == RecyclerView.SCROLL_STATE_IDLE){
            notifyDataSetChanged();
        }
    }
    public void setSeletedItem(BeautyFilterItem item){
        mSeletedItem = item;
    }

    public BeautyFilterItem getSeletedItem(){
        return mSeletedItem;
    }

    class FilterViewHolder extends RecyclerView.ViewHolder{
        //item
        View itemFl;
        //美颜效果缩略图
        ImageView itemImv;
        //被点击时覆盖在美颜效果缩略图上的大图
        ImageView itemImvCover;
        //被点击时覆盖在美颜效果缩略图上的小图
        ImageView selectedIv;
        //美颜效果描述文字
        TextView itemTev;

        public FilterViewHolder(View view){
            super(view);
            itemFl = view.findViewById(R.id.fl_item);
            itemImv = (ImageView) view.findViewById(R.id.iv_item_photo);
            selectedIv = (ImageView) view.findViewById(R.id.iv_item_selected);
            itemTev = (TextView) view.findViewById(R.id.tv_item_describle);
            itemImvCover = (ImageView) view.findViewById(R.id.iv_item_photo_cover);
        }
    }

    /**
     * 美颜效果缩略图的加载器，加载完后刷新列表
     */
    private class FilterImageLoadTask extends AsyncTask<BeautyFilterItem, Void, Integer>{

        @Override
        protected Integer doInBackground(BeautyFilterItem... params) {
            BeautyFilterItem item = params[0];
            BeautyMode mode = item.getBeautyMode();
            NativeBitmap nBitmap = mThumbnailNativeBitmap.copy();
            FilterProcessor.renderProc(nBitmap, mode.getFilterId(), LOAD_ALPHA);
            mBitmapLruCache.put(mode, nBitmap.getImage());
            return mode.getId();
        }

        @Override
        protected void onPostExecute(Integer id) {
            notifyItemChanged(id);
        }
    }

    /**
     * item点击监听接口
     */
    public interface OnItemClickListener {
        void onItemClick(View view , BeautyFilterItem item);
    }
}
