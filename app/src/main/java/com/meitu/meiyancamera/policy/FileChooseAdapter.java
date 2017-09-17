package com.meitu.meiyancamera.policy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.meitu.meiyancamera.R;

import java.io.File;
import java.util.List;

/**
 * 文件路径选择列表的适配器
 * Created by libiao on 2017/6/11.
 */
public class FileChooseAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private Bitmap mIcon;
    private List<String> paths;

    public FileChooseAdapter(Context context, List<String> paths) {
        this.mInflater = LayoutInflater.from(context);
        this.paths = paths;
        this.mIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.file_icon);
    }

    public int getCount() {
        return paths.size();
    }

    public Object getItem(int position) {
        return paths.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.file_choose_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.fileIconIv.setImageBitmap(mIcon);
        String absolutePath = paths.get(position);
        holder.filePathTv.setText(absolutePath.substring(absolutePath.lastIndexOf("/") + 1));
        return convertView;
    }

    private class ViewHolder {
        TextView filePathTv;
        ImageView fileIconIv;
        public ViewHolder(View view){
            filePathTv = (TextView) view.findViewById(R.id.tv_file_path);
            fileIconIv = (ImageView) view.findViewById(R.id.iv_file_icon);
        }
    }
}
