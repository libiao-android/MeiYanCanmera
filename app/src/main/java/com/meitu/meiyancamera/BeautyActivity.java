package com.meitu.meiyancamera;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.meitu.meiyancamera.fragment.SelectPhotoFragment;
import com.meitu.meiyancamera.policy.BeautyPhotoOptionManager;

/**
 * Created by libiao on 2017/6/5.
 * 主activity，全屏显示
 */
public class BeautyActivity extends AppCompatActivity {
    //美颜操作的管理类对象
    private BeautyPhotoOptionManager mOptionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.beauty_activity);
        mOptionManager = new BeautyPhotoOptionManager(this);
        switchFragment(new SelectPhotoFragment(), SelectPhotoFragment.class.getSimpleName(), false);
    }

    /**
     * 跳转到另一个Fragment
     * @param fragment 跳转的下一个fragment
     * @param tag 标识一个Fragment
     * @param addToBackStack 是否添加到BackStack中
     */
    public void switchFragment(Fragment fragment, String tag, boolean addToBackStack){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_content, fragment);
        if(addToBackStack){
            transaction.addToBackStack(tag);
        }
        transaction.commit();
    }

    /**
     *
     * @return 返回美颜操作管理类对象
     */
    public BeautyPhotoOptionManager getOptionManager(){
        return mOptionManager;
    }

}
