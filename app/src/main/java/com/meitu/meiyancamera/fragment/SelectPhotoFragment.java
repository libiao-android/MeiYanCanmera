package com.meitu.meiyancamera.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.meitu.meiyancamera.BeautyActivity;
import com.meitu.meiyancamera.R;

/**
 * 选择一张要美颜处理的图片，
 * 点击选图按钮，会打开系统相册，从中选择一张图片
 * Created by libiao on 2017/6/5.
 */
public class SelectPhotoFragment extends Fragment {

    private static final int IMAGE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_photo_fragment, container, false);
        Button button = (Button) view.findViewById(R.id.btn_select_photo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开系统相册
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //获取图片路径，将图片URI保存在美颜操作管理类中，并启动美颜主页
        if(resultCode == Activity.RESULT_OK && data != null){
            switch (requestCode){
                case IMAGE:
                    String photoUri = data.getData().toString();
                    BeautyActivity activity = (BeautyActivity) getActivity();
                    activity.getOptionManager().setPhotoUri(photoUri);
                    activity.switchFragment(new BeautyHomeFragment(), BeautyHomeFragment.class.getSimpleName(), true);
                    break;
                default:
                    break;
            }
        }
    }
}
