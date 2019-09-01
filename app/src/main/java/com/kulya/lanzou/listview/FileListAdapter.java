package com.kulya.lanzou.listview;
/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/6 20:10
*/

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.kulya.lanzou.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private List<FileItem> mFileList;
    public OnItemClickListener mOnItemClickListener;
    private AllSelectListener onAllSelectListener;

    public interface AllSelectListener {

        void onCheckClick(View v,int num);

    }

    public void setOnCheckClickListener(AllSelectListener onAllSelectListener) {

        this.onAllSelectListener = onAllSelectListener;

    }

    //定义接口
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
        CheckBox checkBox;
        View itemView;

        public ViewHolder(View view) { //构造方法，传入的view继承属性
            super(view);
            this.itemView = view;
            imageView = view.findViewById(R.id.iron);
            textView = view.findViewById(R.id.filename);
            checkBox = view.findViewById(R.id.checkbox);
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
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    /********************对recyclerview子项数据进行赋值***************************/
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        FileItem item = mFileList.get(i);
        if (item.getFileORHolder() == FileItem.ISFILE)
            viewHolder.imageView.setImageResource(R.drawable.p2);
        else if (item.getFileORHolder() == FileItem.ISHOLDER)
            viewHolder.imageView.setImageResource(R.drawable.p1);
        viewHolder.checkBox.setChecked(item.getIsCheck());
        viewHolder.textView.setText(item.getFilename());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.ItemClick(v, i);
                }
            }
        });
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileItem item = mFileList.get(i);
                if (viewHolder.checkBox.isChecked())
                    item.setIsCheck(true);
                else
                    item.setIsCheck(false);

            }
        });
    }

    /*******************告诉recyclerview有多少子项****************************/
    @Override
    public int getItemCount() {
        return mFileList.size();
    }


}