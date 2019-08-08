package com.kulya.lanzou.util;
/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/6 20:10
*/

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kulya.lanzou.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> implements View.OnClickListener {
    private List<FileItem> mFileList;
    public OnItemClickListener mOnItemClickListener;

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.ItemClick(v, (Integer) v.getTag());
        }
    }

    public interface OnItemClickListener {
        void ItemClick(View view, int position);
    }

    public void setMOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;

    }

    /****************创建一个内部类，用作缓存*******************************/
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView; //缓存类属性
        View itemview;

        public ViewHolder(View view) { //构造方法，传入的view继承属性
            super(view);
            this.itemview = view;
            imageView = view.findViewById(R.id.iron);
            textView = view.findViewById(R.id.filename);
        }
    }

    /******************act7adapter,将传入的list转存入act7_list*****************************/
    public FileListAdapter(List<FileItem> list) {
        mFileList = list;
    }

    @NonNull
    @Override
    /*****************创建viewholder实例，在这里加载布局******************************/
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fileitem,
                viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemview.setOnClickListener(this);
        return holder;
    }

    /********************对recyclerview子项数据进行赋值***************************/
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        FileItem item = mFileList.get(i);
        viewHolder.imageView.setImageResource(item.getSrc());
        viewHolder.textView.setText(item.getFilename());
        viewHolder.itemview.setTag(i);
    }

    /*******************告诉recyclerview有多少子项****************************/
    @Override
    public int getItemCount() {
        return mFileList.size();
    }


}