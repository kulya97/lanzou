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



import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private List<FileItem> mFileList;
    private boolean selected = false;
    private OnClickListener clickListener;

    public interface OnClickListener {
        void onItemClick( View view, int position);

        boolean onItemLongClick( View view, int position);
    }

    public void setOnItemClickListener2(OnClickListener listener) {
        clickListener = listener;
    }



    /****************创建一个内部类，用作缓存*******************************/
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView; //缓存类属性
        CheckBox checkBox;
        View itemView;
        TextView downs;
        TextView size;
        TextView time;


        public ViewHolder(View view) { //构造方法，传入的view继承属性
            super(view);
            this.itemView = view;
            imageView = view.findViewById(R.id.iron);
            textView = view.findViewById(R.id.filename);
            checkBox = view.findViewById(R.id.checkbox);
            downs=view.findViewById(R.id.downs);
            size=view.findViewById(R.id.size);
            time=view.findViewById(R.id.time);
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
    public void onBindViewHolder(@NonNull final ViewHolder baseViewHolder, final int i) {
        FileItem fileItem = mFileList.get(i);
        if (fileItem.getFileORHolder() == FileItem.ISFILE) {
            baseViewHolder.imageView.setImageResource(R.drawable.p2);
            baseViewHolder.time.setText(fileItem.getTime());
            baseViewHolder.size.setText(fileItem.getSizes());
            baseViewHolder.downs.setText( fileItem.getDowns());
        } else if (fileItem.getFileORHolder() == FileItem.ISHOLDER)
            baseViewHolder.imageView.setImageResource( R.drawable.p1);
        baseViewHolder.textView.setText(fileItem.getFilename());
        CheckBox box = baseViewHolder.checkBox;
        if (selected) {
            box.setVisibility(View.VISIBLE);
            box.setChecked(fileItem.getIsCheck());
        } else {
            fileItem.setIsCheck(false);
            box.setVisibility(View.INVISIBLE);
        }
        baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected) {
                    box.setChecked(!box.isChecked());
                    fileItem.setIsCheck(box.isChecked());
                } else {
                    clickListener.onItemClick( v, i);
                }
            }
        });

        baseViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selected = true;
                notifyDataSetChanged();
                return false;
            }
        });

    }

    /*******************告诉recyclerview有多少子项****************************/
    @Override
    public int getItemCount() {
        return mFileList.size();
    }
    public List<FileItem> getSelectedItem() {
        //忍不住了要睡了，返回选中的item供activity调用下载删除
        List<FileItem> mList=new ArrayList<>();
        for(FileItem item:mFileList){
            if(item.getIsCheck()){
                mList.add(item);
            }
        }
        return mList;
    }

    public void setSelectedMode(boolean Mode) {
        selected = Mode;
        notifyDataSetChanged();
    }


    public boolean getSelectedMode() {
        return selected;
    }

}
