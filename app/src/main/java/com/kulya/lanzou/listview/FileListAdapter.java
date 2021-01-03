package com.kulya.lanzou.listview;
/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/6 20:10
*/

import android.view.View;
import android.widget.CheckBox;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.kulya.lanzou.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FileListAdapter extends BaseQuickAdapter<FileItem, BaseViewHolder> {
    private List<FileItem> mFileList;
    private boolean selected = false;
    private OnClickListener clickListener;

    public interface OnClickListener {
        void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position);

        boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position);
    }

    public void setOnItemClickListener2(OnClickListener listener) {
        clickListener = listener;
    }

    public FileListAdapter(int layoutResId, @Nullable List<FileItem> data) {
        super(layoutResId, data);
        mFileList = data;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, FileItem fileItem) {
        if (fileItem.getFileORHolder() == FileItem.ISFILE) {
            baseViewHolder.setImageResource(R.id.iron, R.drawable.p2);
            baseViewHolder.setText(R.id.time, fileItem.getTime());
            baseViewHolder.setText(R.id.size, fileItem.getSizes());
            baseViewHolder.setText(R.id.downs, fileItem.getDowns());
        } else if (fileItem.getFileORHolder() == FileItem.ISHOLDER)
            baseViewHolder.setImageResource(R.id.iron, R.drawable.p1);
        baseViewHolder.setText(R.id.filename, fileItem.getFilename());
        CheckBox box = baseViewHolder.getView(R.id.checkbox);
        baseViewHolder.setText(R.id.time, fileItem.getTime());
        baseViewHolder.setText(R.id.size, fileItem.getSizes());
        baseViewHolder.setText(R.id.downs, fileItem.getDowns());
        if (selected) {
            box.setVisibility(View.VISIBLE);
        } else {
            fileItem.setIsCheck(false);
            mFileList.get(getItemPosition(fileItem)).setIsCheck(false);
            box.setVisibility(View.INVISIBLE);
        }
        box.setChecked(fileItem.getIsCheck());
        setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                selected = true;
                notifyDataSetChanged();
                return false;
            }
        });
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (selected) {
                    CheckBox box2 = (CheckBox) adapter.getViewByPosition(position, R.id.checkbox);
                    mFileList.get(position).setIsCheck(!box2.isChecked());
                    box2.setChecked(!box2.isChecked());
                } else {
                    clickListener.onItemClick(adapter, view, position);
                }
            }
        });

    }

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
    }

    public List<FileItem> getSelectedItem() {
        //忍不住了要睡了，返回选中的item供activity调用下载删除
        return mFileList;
    }

    public void setSelectedMode(boolean Mode) {
        selected = Mode;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public boolean getSelectedMode() {
        return selected;
    }

}
