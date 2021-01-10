package com.kulya.lanzou.listview;
/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/6 20:10
*/

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
    private boolean selected = false;//是否全选状态，如果是则显示checkbox，否则隐藏。
    private OnClickListener clickListener;


    public interface OnClickListener {
        void onItemClick(View view, int position);

        boolean onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnClickListener listener) {
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
            downs = view.findViewById(R.id.downs);
            size = view.findViewById(R.id.size);
            time = view.findViewById(R.id.time);
        }
    }

    public FileListAdapter(List<FileItem> list) {
        mFileList = list;
    }

    @NonNull
    @Override
    /*****************创建viewholder实例，在这里加载布局注册点击事件******************************/
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fileitem,
                viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (selected) {
                CheckBox box = holder.checkBox;
                box.setChecked(!box.isChecked());
                mFileList.get(pos).setIsCheck(box.isChecked());
            } else {
                clickListener.onItemClick(v, pos);
            }
        });
        view.setOnLongClickListener(v -> {
            selected = true;
            notifyDataSetChanged();
            return false;
        });
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
            baseViewHolder.downs.setText(fileItem.getDowns());
        } else if (fileItem.getFileORHolder() == FileItem.ISHOLDER) {
            baseViewHolder.imageView.setImageResource(R.drawable.p1);
            baseViewHolder.time.setText("");
            baseViewHolder.size.setText("");
            baseViewHolder.downs.setText("");
        }
        baseViewHolder.textView.setText(fileItem.getFilename());
        CheckBox box = baseViewHolder.checkBox;
        if (selected) {
            box.setVisibility(View.VISIBLE);
            box.setChecked(fileItem.getIsCheck());
        } else {
            fileItem.setIsCheck(false);
            box.setVisibility(View.INVISIBLE);
        }
    }

    /*
    刷新数据
     */
    public void updateData(List<FileItem> list) {
        mFileList = list;
        notifyDataSetChanged();

    }

    /*******************获取item个数****************************/
    @Override
    public int getItemCount() {
        return mFileList.size();
    }

    /*
    获取选中的项
     */
    public List<FileItem> getSelectedItem() {
        //忍不住了要睡了，返回选中的item供activity调用下载删除
        List<FileItem> mList = new ArrayList<>();
        for (FileItem item : mFileList) {
            if (item.getIsCheck()) {
                mList.add(item);
            }
        }
        return mList;
    }

    /*
    设置全选状态
     */
    public void setSelectedMode(boolean Mode) {
        selected = Mode;
        notifyDataSetChanged();
    }

    /*
    获取全选状态
     */
    public boolean getSelectedMode() {
        return selected;
    }
}
