package com.kulya.lanzou.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.kulya.lanzou.R;


/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/29 13:11
*/
public class addFolderPop extends PopupWindow {

    private Activity mContext;
    private onClick click;
    private View contentView;

    public interface onClick {
        void onClick(String folderName, String folderDescription);
    }

    public addFolderPop(Activity mContext, final onClick click) {
        this.mContext = mContext;
        this.click = click;
        contentView = LayoutInflater.from(mContext).inflate(R.layout.newfloderpopwindow, null);
        initPop();
        initView();
    }

    public void initView() {
        final EditText name = contentView.findViewById(R.id.enter_name);
        final EditText description = contentView.findViewById(R.id.description);
        Button newfolder = contentView.findViewById(R.id.newFolder);
        newfolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = name.getText().toString();
                String folderDescription = description.getText().toString();
                if (folderName.equals("")) {
                    folderName = "";
                    folderDescription = "";
                } else {
                    click.onClick(folderName, folderDescription);
                }
                dismiss();
            }
        });
    }

    private void initPop() {
        this.setContentView(contentView);
        this.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setAnimationStyle(R.style.contextMenuAnim);

        View rootview = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null);
        this.showAtLocation(rootview, Gravity.BOTTOM, 0, 600);

        ColorDrawable dw = new ColorDrawable(0x00FFFFFF);
        this.setBackgroundDrawable(dw);
        backgroundAlpha(mContext, 0.5f);

        this.setOnDismissListener(new PopupWindow.OnDismissListener() {
            public void onDismiss() {
                WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
                lp.alpha = 1f;
                mContext.getWindow().setAttributes(lp);
            }
        });

    }

    public void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

}
