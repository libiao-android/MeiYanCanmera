package com.meitu.meiyancamera.fragment;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.meitu.core.processor.FilterProcessor;
import com.meitu.core.types.NativeBitmap;
import com.meitu.core.util.CacheUtil;
import com.meitu.meiyancamera.BeautyActivity;
import com.meitu.meiyancamera.R;
import com.meitu.meiyancamera.data.BeautyFilterItem;
import com.meitu.meiyancamera.data.BeautyMode;
import com.meitu.meiyancamera.policy.BeautyFilterAdapter;
import com.meitu.meiyancamera.policy.BeautyPhotoOptionManager;
import com.meitu.meiyancamera.policy.ScrollSpeedLinearLayoutManager;
import com.meitu.meiyancamera.util.Constant;

/**
 * 美颜特效页面，提供多种特效选择
 * Created by libiao on 2017/6/6.
 */
public class BeautySpecialFragment extends Fragment{
    //展示各种特效的列表
    private RecyclerView mBeautyFilterRv;
    //RecyclerView适配器
    private BeautyFilterAdapter mFilterAdapter;
    //原图
    private ImageView mBeautyShowIv;
    //美颜后的效果图
    private ImageView mBeautyFilterShowIv;
    //完成按钮
    private Button mOkBtn;
    //取消按钮
    private Button mCancelBtn;
    //拖动条，用于控制美颜图片的透明度
    private SeekBar mBeautyAlphaSb;
    //加载图片的进度条
    private ProgressBar mProgressBar;
    //拖动条是否可见
    private boolean mBeautyAlphaSbVisible = true;
    //美颜操作的管理类对象
    private BeautyPhotoOptionManager mOptionManager;
    //美颜特效图加载任务
    private BigImageLoadTask mLoadTask;
    //完成和取消按钮的监听
    private BeautyMenuClickListener mMenuListener;
    //当前正在处理的美颜特效列表Item对象
    private BeautyFilterItem mCurrentFilterItem;
    //当前显示的美颜后的图片
    private Bitmap mCurrentBigBitmap;
    //美颜效果的透明度
    private static final float LOAD_ALPHA = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMenuListener = new BeautyMenuClickListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beauty_special_fragment, container, false);
        mOptionManager = ((BeautyActivity)getActivity()).getOptionManager();
        initUI(view);
        return view;
    }

    /**
     * 初始化UI
     * @param view
     */
    private void initUI(View view) {
        mBeautyShowIv = (ImageView) view.findViewById(R.id.iv_beauty_show);
        mBeautyShowIv.setImageBitmap(mOptionManager.getCurrentNativeBitmap().getImage());
        mBeautyFilterShowIv = (ImageView) view.findViewById(R.id.iv_beauty_filter_show);

        //recyclerview相关
        mBeautyFilterRv = (RecyclerView) view.findViewById(R.id.rv_beauty_filter_list);
        LinearLayoutManager linearLayoutManager = new ScrollSpeedLinearLayoutManager(getContext());
        //列表水平滚动
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBeautyFilterRv.setLayoutManager(linearLayoutManager);
        mFilterAdapter = new BeautyFilterAdapter(getContext(), mOptionManager);
        mBeautyFilterRv.setAdapter(mFilterAdapter);
        mFilterAdapter.setOnItemClickListener(new BeautyFilterItemClickListener());

        mBeautyAlphaSb = (SeekBar) view.findViewById(R.id.sb_beauty_filter_alpha);
        mBeautyAlphaSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //拖动条变化对应美颜图片透明度变化
                mBeautyFilterShowIv.setImageAlpha(progress * Constant.ALPHA_MAX / Constant.PROGRESS_MAX);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //保存当前透明度
                mCurrentFilterItem.setImageAlpha(seekBar.getProgress() * Constant.ALPHA_MAX / Constant.PROGRESS_MAX);
            }
        });

        mOkBtn = (Button) view.findViewById(R.id.btn_beauty_ok);
        mCancelBtn = (Button) view.findViewById(R.id.btn_beauty_cancel);
        mOkBtn.setOnClickListener(mMenuListener);
        mCancelBtn.setOnClickListener(mMenuListener);

        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_special_fragment);
    }

    //结束当前页面
    private void finish() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * 加载美颜图片
     * 如果将要加载的是原图，则不用加载，将美颜图片ImageView设为不可见
     * @param item
     */
    private void beginLoadTask(BeautyFilterItem item) {
        if(mLoadTask != null){
            mLoadTask.cancel(false);
            mLoadTask = null;
        }
        if(item.getBeautyMode()== BeautyMode.YUAN_TU){
            mBeautyFilterShowIv.setVisibility(View.GONE);
        }else{
            mLoadTask = new BigImageLoadTask();
            mLoadTask.execute(item);
        }
    }

    /**
     * 美颜特效大图加载
     * 完成按钮的美颜效果加载也是通过这，通过标志位isCache判断
     */
    private class BigImageLoadTask extends AsyncTask<BeautyFilterItem, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(BeautyFilterItem... params) {
            //item里有要加载的美颜图片的滤镜信息
            BeautyFilterItem item = params[0];
            NativeBitmap nbitmap = mOptionManager.getCurrentNativeBitmap().copy();
            if(item.isCache()){
                //完成按钮的加载
                FilterProcessor.renderProc(nbitmap, item.getBeautyMode().getFilterId(),
                        (float) (item.getImageAlpha() * Constant.PROGRESS_MAX / Constant.ALPHA_MAX) / Constant.PROGRESS_MAX);
                //先释放之前的内存
                mOptionManager.setCurrentNativeBitmap(null);
                //再赋值当前操作的bitmap
                mOptionManager.setCurrentNativeBitmap(nbitmap.copy());
                String cachePath = mOptionManager.getSingleCachePath();
                //将图片缓存到sdcard
                boolean success = CacheUtil.image2cache(nbitmap, cachePath);
                if(success){
                    //缓存成功，将这一操作记录下来
                    mOptionManager.addOption(cachePath);
                }
            }else{
                //美颜特效列表点击的美颜效果加载
                FilterProcessor.renderProc(nbitmap, item.getBeautyMode().getFilterId(), LOAD_ALPHA);
                mCurrentBigBitmap = nbitmap.getImage();
            }
            return item.isCache();
        }

        @Override
        protected void onPostExecute(Boolean save) {
            if(!save){
                //美颜特效列表点击的美颜效果加载,完成后直接显示即可
                mBeautyFilterShowIv.setVisibility(View.VISIBLE);
                mBeautyFilterShowIv.setImageAlpha(mCurrentFilterItem.getImageAlpha());
                mBeautyFilterShowIv.setImageBitmap(mCurrentBigBitmap);
            }else{
                //完成按钮的加载,完成后结束当前页面
                finish();
            }
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * 完成和取消按钮的点击处理
     */
    private class BeautyMenuClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_beauty_ok:
                    //根据当前的透明度去生成和保存特效图
                    if(mCurrentFilterItem != null && mCurrentFilterItem.getBeautyMode() != BeautyMode.YUAN_TU){
                        mCurrentFilterItem.setIsCache(true);
                        beginLoadTask(mCurrentFilterItem);
                    }else{
                        finish();
                    }
                    break;
                case R.id.btn_beauty_cancel:
                    finish();
                    break;
            }
        }
    }

    /**
     * 美颜特效选择列表item的点击处理
     */
    private class BeautyFilterItemClickListener implements BeautyFilterAdapter.OnItemClickListener{

        @Override
        public void onItemClick(View view, BeautyFilterItem item) {
            mCurrentFilterItem = item;
            int preUpdateId = mFilterAdapter.getSeletedItem().getBeautyMode().getId();
            int nowUpdateId = item.getBeautyMode().getId();
            if(preUpdateId != nowUpdateId){
                //两次点击的不是同一个item，刷新列表，并且开始加载
                mFilterAdapter.setSeletedItem(item);
                mFilterAdapter.notifyDataSetChanged();
                beginLoadTask(item);
            }
            if(item.getBeautyMode() == BeautyMode.YUAN_TU){
                //点击的是原图，将拖动条隐藏
                mBeautyAlphaSb.setVisibility(View.GONE);
            }else{
                //点击同一个item两次，拖动条消失，再点击，拖动条显示
                if(preUpdateId == nowUpdateId){
                    mBeautyAlphaSbVisible = !mBeautyAlphaSbVisible;
                }
                mBeautyAlphaSb.setProgress(item.getImageAlpha() * Constant.PROGRESS_MAX / Constant.ALPHA_MAX);
                mBeautyAlphaSb.setVisibility(mBeautyAlphaSbVisible ? View.VISIBLE : View.GONE);
            }
            View v = mBeautyFilterRv.getLayoutManager().findViewByPosition(nowUpdateId);
            //点击屏幕最后一个效果，自动推进下一个效果
            if(v.getX() > mOptionManager.getAdvanceSelfRight()){
                mBeautyFilterRv.smoothScrollToPosition(nowUpdateId + 1);
            }
            //点击屏幕最开始一个效果，自动推进上一个效果
            if(v.getX() < mOptionManager.getAdvanceSelfLeft()){
                mBeautyFilterRv.smoothScrollToPosition(nowUpdateId == 0 ? nowUpdateId : nowUpdateId - 1);
            }
        }
    }
}
