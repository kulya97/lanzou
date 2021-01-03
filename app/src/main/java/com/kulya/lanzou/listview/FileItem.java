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
    private String id;
    private boolean isCheck=false;
    private String downs;
    private String time;

    public String getDowns() {
        return downs;
    }

    public void setDowns(String downs) {
        this.downs = downs;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSizes() {
        return sizes;
    }

    public void setSizes(String sizes) {
        this.sizes = sizes;
    }

    private String sizes;


    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    private String fileUrl;

    public FileItem(String filename, int fileORHolder, String id) {
        this.filename = filename;
        this.fileORHolder = fileORHolder;
        this.id = id;
        isCheck = false;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
