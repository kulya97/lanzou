package com.kulya.lanzou.listview;

/*
项目名称： lanzou
创建人：黄大神
类描述：子项属性
创建时间：2019/8/6 19:43
*/
public class FileItem {
    public static final int ISFILE = 0;
    public static final int ISHOLDER = 1;
    private String filename;
    private int fileORHolder;
    private String href;
    private boolean isCheck;

    public FileItem(String filename, int fileORHolder, String href) {
        this.filename = filename;
        this.fileORHolder = fileORHolder;
        this.href = href;
        isCheck = false;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public int getFileORHolder() {
        return fileORHolder;
    }

    public void setFileORHolder(int fileORHolder) {
        this.fileORHolder = fileORHolder;
    }

    public boolean getIsCheck() {
        return isCheck;
    }

    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }
}
