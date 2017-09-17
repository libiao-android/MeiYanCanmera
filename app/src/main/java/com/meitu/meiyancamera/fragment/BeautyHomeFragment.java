package com.meitu.meiyancamera.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.meitu.meiyancamera.BeautyActivity;
import com.meitu.meiyancamera.R;
import com.meitu.meiyancamera.policy.BeautyPhotoOptionManager;
import com.meitu.meiyancamera.view.FileChooseLinearLayout;

import java.io.File;

/**
 * 美颜主页界面，提供展示美颜后的图片、撤销、重做、保存、返回、进入美颜特效页面的功能
 * Created by libiao on 2017/6/5.
 */
public class BeautyHomeFragment extends Fragment implements View.OnClickListener{
    //返回按钮
    private Button mBackBtn;
    //撤销按钮
    private Button mWithdrawBtn;
    //重做按钮
    private Button mRedoBtn;
    //保存按钮
    private Button mSaveBtn;
    //进入美颜特效页面
    private Button mSpecialBeautyBtn;
    //显示美颜图片的ImageView
    private ImageView mPhotoIv;
    //加载图片的进度条
    private ProgressBar mProgressBar;
    //加载图片任务对象
    private LoadPhotoTask mLoadPhotoTask;
    //美颜操作的管理类对象
    private BeautyPhotoOptionManager mOptionManager;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mOptionManager = ((BeautyActivity)getActivity()).getOptionManager();
        mContext = getContext();
        View view = inflater.inflate(R.layout.beauty_home_fragment, container, false);
        initUI(view);
        String photoUri = mOptionManager.getPhotoUri();
        //美颜操作的管理类中已经存在处理的图片，直接使用，否则通过线程去加载
        if(mOptionManager.getCurrentNativeBitmap() != null){
            mPhotoIv.setImageBitmap(mOptionManager.getCurrentNativeBitmap().getImage());
        }else{
            loadPhoto(photoUri);
        }
        return view;
    }

    /**
     * 此页面销毁之前清空所有操作
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mOptionManager.clearAllOptionCache();
    }

    /**
     * 启动加载图片任务
     * @param photoUri 通过URI加载
     */
    private void loadPhoto(String photoUri) {
        if(mLoadPhotoTask != null){
            mLoadPhotoTask.cancel(false);
            mLoadPhotoTask = null;
        }
        mLoadPhotoTask = new LoadPhotoTask();
        mLoadPhotoTask.execute(photoUri);
    }

    /**
     * 初始化UI
     * @param view
     */
    private void initUI(View view) {
        mBackBtn = (Button) view.findViewById(R.id.btn_back);
        mWithdrawBtn = (Button) view.findViewById(R.id.btn_withdraw);
        mRedoBtn = (Button) view.findViewById(R.id.btn_redo);
        mSaveBtn = (Button) view.findViewById(R.id.btn_save);
        mSpecialBeautyBtn = (Button) view.findViewById(R.id.btn_special_beauty);
        mPhotoIv = (ImageView) view.findViewById(R.id.iv_photo);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_home_fragment);

        mBackBtn.setOnClickListener(this);
        mWithdrawBtn.setOnClickListener(this);
        mRedoBtn.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);
        mSpecialBeautyBtn.setOnClickListener(this);

        refreshOptionMenu();
    }

    /**
     * 各按钮的点击操作实现
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.btn_withdraw:
                //撤销操作具体由管理类实现
                Bitmap bitmap = mOptionManager.withdrawOption();
                if(bitmap != null){
                    mPhotoIv.setImageBitmap(bitmap);
                }else{
                    mPhotoIv.setImageBitmap(mOptionManager.getCurrentNativeBitmap().getImage());
                }
                refreshOptionMenu();
                break;
            case R.id.btn_redo:
                //重做操作具体由管理类实现
                Bitmap redoBitmap = mOptionManager.redoOption();
                if(redoBitmap != null){
                    mPhotoIv.setImageBitmap(redoBitmap);
                }else{
                    mPhotoIv.setImageBitmap(mOptionManager.getCurrentNativeBitmap().getImage());
                }
                refreshOptionMenu();
                break;
            case R.id.btn_save:
                showFileChooser();
                break;
            case R.id.btn_special_beauty:
                //进入美颜特效页面前，先清空重做操作
                mOptionManager.clearRedoOption();
                BeautySpecialFragment fragment = new BeautySpecialFragment();
                BeautyActivity activity = (BeautyActivity) getActivity();
                activity.switchFragment(fragment , BeautySpecialFragment.class.getSimpleName(), true);
                break;
        }
    }

    /**
     * 打开文件选择器，用Dialog呈现
     */
    private void showFileChooser() {
        AlertDialog.Builder chooseFileDialog =
                new AlertDialog.Builder(mContext);
        final FileChooseLinearLayout fileChooseView = (FileChooseLinearLayout)LayoutInflater.from(mContext)
                .inflate(R.layout.file_choose_view,null);
        chooseFileDialog.setTitle(mContext.getResources().getString(R.string.fragment_home_choose_path));
        chooseFileDialog.setView(fileChooseView);
        chooseFileDialog.setPositiveButton(mContext.getResources().getString(R.string.fragment_home_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //以当前时间命名图片
                        String path = fileChooseView.getCurPath()+ File.separator + System.currentTimeMillis() + ".png";
                        mOptionManager.saveImage(path);
                    }
                });
        chooseFileDialog.setNegativeButton(mContext.getResources().getString(R.string.fragment_home_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        chooseFileDialog.show();
    }

    /**
     * 判断撤销和重做按钮是否可用
     * 通过管理类中是否保存了撤销和重做操作来判断
     */
    private void refreshOptionMenu() {
        mWithdrawBtn.setEnabled(mOptionManager.haveWithdrawOption());
        mRedoBtn.setEnabled(mOptionManager.haveRedoOption());
    }

    /**
     * 通过phototUri加载一张图片
     */
    private class LoadPhotoTask extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Uri photoUri = Uri.parse(params[0]);
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = mContext.getContentResolver().query(photoUri, filePathColumns, null, null, null);
            if(c != null && c.moveToFirst()){
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String imagePath = c.getString(columnIndex);
                c.close();
                //将当前处理的图片缓存到管理类中
                mOptionManager.initCurrentNativeBitmap(imagePath);
                return mOptionManager.getCurrentNativeBitmap().getImage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                mPhotoIv.setImageBitmap(bitmap);
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }
}
