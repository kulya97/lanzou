package com.kulya.lanzou.util;

/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/6 19:43
*/
public class FileItem {

    private String filename;
    private int src;
    private String href;

    public FileItem(String filename, int src,String href) {
        this.filename = filename;
        this.src = src;
        this.href=href;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
    }


    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
