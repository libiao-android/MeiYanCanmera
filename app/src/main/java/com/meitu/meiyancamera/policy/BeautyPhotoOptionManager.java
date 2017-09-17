package com.meitu.meiyancamera.policy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.meitu.core.types.NativeBitmap;
import com.meitu.core.util.CacheUtil;
import com.meitu.meiyancamera.R;

import java.io.File;
import java.util.LinkedList;

/**
 * 这个类用来连接3个fragment，做数据交换，和fragment相关的、需要计算的都放这里面
 * 同时这个类还是美颜操作的管理类
 * 记录了当前处理图片的信息，记录了美颜操作信息
 * Created by libiao on 2017/6/6.
 */
public class BeautyPhotoOptionManager {

    //美颜缓存操作的次数，用这个值唯一标识缓存文件路径
    private int mSaveCount;
    //可撤销的步数，String代表图片缓存路径
    private LinkedList<String> mWithdrawCacheList = new LinkedList<>();
    //可重做的步数，String代表图片缓存路径
    private LinkedList<String> mRedoCacheList = new LinkedList<>();
    private Handler mHanlder = new Handler();
    //当前处理的图片的缩略图对象
    private Bitmap mCurrentThumbnail;
    //当前处理的图片对象
    private NativeBitmap mCurrentNativeBitmap;
    //当前图片缓存路径
    private String mCurrentCachePath;
    private Context mContext;
    //缩略图（正方形）长度
    private int mThumbnailLength;
    //初始化图片的size
    private int mDecodeBitmapSize;
    //右边自动推进的距离值
    private int mAdvanceSelfRight;
    //左边自动推进的距离值
    private int mAdvanceSelfLeft;
    //从相册选择图片的URI
    private String mPhotoUri;

    //撤销操作步数
    private static final int OPERATION_STEPS = 5;
    //保存图片的压缩率
    private static final int COMPRESS_LEVEL = 100;

    /**
     * 根据屏幕宽高和资源定义的值计算好一些值。
     * @param context
     */
    public BeautyPhotoOptionManager(Context context){
        mContext = context;
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        int height = point.y - (int)mContext.getResources().getDimension(R.dimen.fragment_special_list_height)
                - (int)mContext.getResources().getDimension(R.dimen.fragment_special_menu_button_height);
        mDecodeBitmapSize = Math.max(point.x, height);
        mAdvanceSelfLeft = (int)mContext.getResources().getDimension(R.dimen.filter_item_thumbnail_width)
                + (int)mContext.getResources().getDimension(R.dimen.filter_item_margin_left_right) * 2;
        mAdvanceSelfRight = point.x - (int)mContext.getResources().getDimension(R.dimen.filter_item_thumbnail_width) * 2
                - (int)mContext.getResources().getDimension(R.dimen.filter_item_margin_left_right) * 2;
        mThumbnailLength = (int)mContext.getResources().getDimension(R.dimen.filter_item_thumbnail_width);
    }

    /**
     * 通过图片路径加载图片
     * 初始化当前图片处理对象。
     * 将当前处理的图片临时缓存到sdcard中
     * @param imagePath 图片路径
     */
    public void initCurrentNativeBitmap(String imagePath) {
        Log.i("libiao", "imagePath = "+imagePath);
        mCurrentNativeBitmap = NativeBitmap.createBitmap(imagePath, mDecodeBitmapSize);
        String cachePath =  getSingleCachePath();
        mSaveCount++;
        boolean success = CacheUtil.image2cache(mCurrentNativeBitmap, cachePath);
        if(success){
            mCurrentCachePath = cachePath;
        }else{
            mCurrentCachePath = imagePath;
        }
        mCurrentThumbnail = centerSquareScaleBitmap(mCurrentNativeBitmap.getImage(), mThumbnailLength);
    }

    /**
     * 从图片中间截取正方形
     * @param bitmap 原图
     * @param edgeLength 截取正方形的宽
     * @return
     */
    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength){
        if(null == bitmap || edgeLength <= 0){
            return  null;
        }
        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();
        if(widthOrg > edgeLength && heightOrg > edgeLength){
            //压缩到一个最小长度是edgeLength的bitmap
            int longerEdge = edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg);
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;
            try{
                //等比压缩
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            }
            catch(Exception e){
                return null;
            }
            //从图中截取正中间的正方形部分。
            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;
            try{
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                scaledBitmap.recycle();
            }
            catch(Exception e){
                return null;
            }
        }
        return result;
    }
    /**
     * 返回当前处理的图片的缩略图
     * @return
     */
    public Bitmap getCurrentThumbnail(){
        return mCurrentThumbnail;
    }

    /**
     * 返回当前处理的图片对象
     * @return
     */
    public NativeBitmap getCurrentNativeBitmap(){
        return mCurrentNativeBitmap;
    }

    /**
     * @return 返回每个美颜效果图片缓存的路径
     */
    public String getSingleCachePath() {
        return getCacheFile().getAbsolutePath() + File.separator + mSaveCount + ".ppm";
    }

    /**
     * 返回临时缓存图片路径
     * @return
     */
    private File getCacheFile(){
        File file = new File(mContext.getExternalCacheDir().getAbsolutePath()+ "/temp");
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }

    /**
     * 删除临时缓存图片文件目录
     * @param file
     */
    private void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }
    /**
     * 特色美颜缓存成功后，将这一步操作记下来
     * @param cachePath 缓存路径
     */
    public void addOption(String cachePath) {
        mSaveCount++;
        if(mWithdrawCacheList.size() == OPERATION_STEPS){
            String path = mWithdrawCacheList.pollFirst();
            File file = new File(path);
            file.delete();
        }
        mWithdrawCacheList.add(mCurrentCachePath);
        mCurrentCachePath = cachePath;
    }

    /**
     * 改变当前处理的图片对象和缩略图对象
     * @param nBitmap
     */
    public void setCurrentNativeBitmap(NativeBitmap nBitmap) {
        mCurrentNativeBitmap = nBitmap;
        if(nBitmap != null){
            mCurrentThumbnail = centerSquareScaleBitmap(nBitmap.getImage(), mThumbnailLength);
        }else {
            mCurrentThumbnail = null;
        }
    }

    /**
     * 撤销操作逻辑,从缓存中拿到图片，再赋给当前对象，同时加入重做队列中
     * @return 返回上一个操作的图片
     */
    public Bitmap withdrawOption() {

        String path = mWithdrawCacheList.pollLast();
        mRedoCacheList.add(mCurrentCachePath);
        mCurrentCachePath = path;
        NativeBitmap nBitmap = CacheUtil.cache2image(path);
        Bitmap bitmap = nBitmap.getImage();
        if(bitmap != null){
            setCurrentNativeBitmap(nBitmap);
            return bitmap;
        }
        return null;
    }

    /**
     * 重做操作逻辑，从缓存中拿到图片，再赋给当前对象，同时放回撤销队列中
     * @return 返回上一个操作的图片
     */
    public Bitmap redoOption() {
        String path = mRedoCacheList.pollLast();
        if(path != null){
            NativeBitmap nBitmap = CacheUtil.cache2image(path);
            Bitmap bitmap = nBitmap.getImage();
            if(bitmap != null){
                mWithdrawCacheList.add(mCurrentCachePath);
                mCurrentCachePath = path;
                setCurrentNativeBitmap(nBitmap);
                return bitmap;
            }
        }
        return null;
    }

    /**
     * 是否有撤销操作
     * @return
     */
    public boolean haveWithdrawOption() {
        return mWithdrawCacheList.size() != 0;
    }

    /**
     * 是否有重做操作
     * @return
     */
    public boolean haveRedoOption() {
        return mRedoCacheList.size() != 0;
    }

    /**
     * 清空所有缓存
     */
    public void clearAllOptionCache() {
        delete(getCacheFile());
        mWithdrawCacheList.clear();
        mRedoCacheList.clear();
        mCurrentNativeBitmap = null;
        mCurrentThumbnail = null;
    }

    /**
     * 清空重做操作的缓存
     */
    public void clearRedoOption() {
        for(String path : mRedoCacheList){
            new File(path).delete();
        }
        mRedoCacheList.clear();
    }

    public int getAdvanceSelfRight(){
        return mAdvanceSelfRight;
    }

    public int getAdvanceSelfLeft(){
        return mAdvanceSelfLeft;
    }

    public void setPhotoUri(String photoUri) {
        this.mPhotoUri = photoUri;
    }

    public String getPhotoUri() {
        return mPhotoUri;
    }

    /**
     * 保存图片到本地
     * @param path 保存路径
     */
    public void saveImage(final String path) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final boolean save = CacheUtil.saveImageSD(getCurrentNativeBitmap(), path, COMPRESS_LEVEL);
                mHanlder.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mContext != null){
                            String saveState = save ? mContext.getResources().getString(R.string.fragment_home_save_success)
                                    : mContext.getResources().getString(R.string.fragment_home_save_fail);
                            Toast.makeText(mContext,
                                    saveState,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
