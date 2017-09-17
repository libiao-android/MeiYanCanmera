package com.meitu.meiyancamera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.meitu.meiyancamera.R;
import com.meitu.meiyancamera.policy.FileChooseAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by libiao on 2017/6/12.
 * 选择图片保存路径的view
 */
public class FileChooseLinearLayout extends LinearLayout{
    //文件夹列表展示的ListView
    private ListView mChooseFileLv;
    //显示当前选择的路径的TextView
    private TextView mCurPathTv;
    //返回上一层目录的Button
    private Button mBackToNextBtn;
    //文件列表适配器
    private FileChooseAdapter mFileAdpter;
    //ListView显示的数据集合，集合里的对象是文件夹路径
    private ArrayList<String> mFilePathsList = new ArrayList<>();
    //表示根目录
    private String mRootPath = "/storage/emulated/0";
    //表示当前目录
    private String mCurPath = mRootPath;

    public FileChooseLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 控件初始化，和点击事件的处理
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCurPathTv = (TextView) findViewById(R.id.tv_cur_path);
        mBackToNextBtn = (Button) findViewById(R.id.btn_back_to_next);
        refreshUi();
        mChooseFileLv = (ListView)findViewById(R.id.lv_file_choose);
        refreshFileDir(mRootPath);
        mFileAdpter = new FileChooseAdapter(getContext(),mFilePathsList);
        mChooseFileLv.setAdapter(mFileAdpter);
        mChooseFileLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurPath = mFilePathsList.get(position);
                refreshUi();
                refreshFileDir(mCurPath);
                mFileAdpter.notifyDataSetChanged();
            }
        });
        mBackToNextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurPath = mCurPath.substring(0,mCurPath.lastIndexOf("/"));
                refreshUi();
                refreshFileDir(mCurPath);
                mFileAdpter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 返回当前的目录路径
     * @return
     */
    public String getCurPath(){
        return mCurPath;
    }

    /**
     * 当前目录是根目录时，mBackToNextBtn不可用；
     * 显示的当前目录中，用..替换根目录
     */
    private void refreshUi() {
        if(mCurPath.equals(mRootPath)){
            mBackToNextBtn.setEnabled(false);
        }else{
            mBackToNextBtn.setEnabled(true);
        }
        mCurPathTv.setText(mCurPath.replace(mRootPath, ".."));
    }

    /**
     * 获取每一个目录下的所有文件夹
     * @param filePath
     */
    private void refreshFileDir(String filePath) {
        mFilePathsList.clear();
        File f = new File(filePath);
        File[] files = f.listFiles();
        if (files.length <= 0)
            return;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && files[i].listFiles() != null) {
                mFilePathsList.add(files[i].getPath());
            }
        }
    }
}
