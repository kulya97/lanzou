package com.kulya.lanzou.listview;
/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/6 20:10
*/
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.kulya.lanzou.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import androidx.annotation.NonNull;

public class FileListAdapter extends BaseQuickAdapter<FileItem,BaseViewHolder> {
    private List<FileItem> mFileList;
    public FileListAdapter(int layoutResId, @Nullable List<FileItem> data) {
        super(layoutResId, data);
    }
    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, FileItem fileItem) {
        if (fileItem.getFileORHolder() == FileItem.ISFILE)
            baseViewHolder.setImageResource(R.id.iron,R.drawable.p2);
        else if (fileItem.getFileORHolder() == FileItem.ISHOLDER)
            baseViewHolder.setImageResource(R.id.iron,R.drawable.p1);
        baseViewHolder.setText(R.id.filename,fileItem.getFilename());
        baseViewHolder.setEnabled(R.id.checkbox,fileItem.getIsCheck());
    }


}