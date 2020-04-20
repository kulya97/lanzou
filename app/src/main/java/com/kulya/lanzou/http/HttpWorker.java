package com.kulya.lanzou.http;


import android.app.DownloadManager;
import android.content.Context;

import android.net.Uri;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.kulya.lanzou.listview.FileItem;
import com.kulya.lanzou.util.Myapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
项目名称： lanzou
创建人：黄大神
类描述：网络工作类
创建时间：2019/8/30 11:42
*/
public class HttpWorker {
    private static HttpWorker mHttpWorker;
    private static final MediaType FROM_DATA = MediaType.parse("multipart/form-data");

    private HttpWorker() {

    }

    private static HttpWorker getInstance() {
        if (mHttpWorker == null) {
            synchronized (OkHttpUtil.class) {
                if (mHttpWorker == null) {
                    mHttpWorker = new HttpWorker();
                }
            }
        }
        return mHttpWorker;
    }

    //登陆  formhash可能会变
    private boolean LoginSync(final String username, final String password) throws IOException, JSONException {
        //作废，暂时不用

        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[4];
        rs[0] = new OkHttpUtil.RequestData("formhash", "5dc76a08");
        rs[1] = new OkHttpUtil.RequestData("uid", username);
        rs[2] = new OkHttpUtil.RequestData("pwd", password);
        rs[3] = new OkHttpUtil.RequestData("task", "3");
        String info = OkHttpUtil.postSyncString(UriUtil.LOGIN, rs);
        String url = "";
        JSONObject jsonObject = new JSONArray("[" + info + "]").getJSONObject(0);
        url = jsonObject.getString("info");
        if (url.equals("成功登录")) {
            return true;
        } else if (url.equals("登陆失败")) {
            MyCookieJar.resetCookies();
            return false;
        } else {
            MyCookieJar.resetCookies();
            return false;
        }
    }

    //创建页面文件夹信息 task47
    private List<FileItem> getFolderInfoSync(final String folder_id) throws IOException, JSONException {
        List<FileItem> list = new ArrayList<>();
        list.clear();
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "47");
        rs[1] = new OkHttpUtil.RequestData("folder_id", folder_id);
        String response = OkHttpUtil.postSyncString(UriUtil.TASK, rs);
        JSONObject jsonObject = new JSONArray("[" + response + "]").getJSONObject(0);
        String folderInfo = jsonObject.getString("text");
        JSONArray folderJsonArray = new JSONArray(folderInfo);
        for (int i = 0; i < folderJsonArray.length(); i++) {
            JSONObject folderObject = folderJsonArray.getJSONObject(i);
            String name = folderObject.getString("name");
            String fol_id = folderObject.getString("fol_id");
            list.add(new FileItem(name, FileItem.ISHOLDER, fol_id));
        }
        return list;
    }

    //创建页面文件信息 task5
    private List<FileItem> getFileInfoSync(final String folder_id) throws IOException, JSONException {
        List<FileItem> list = new ArrayList<>();
        list.clear();
        for (int page = 1; page < 200; page++) {
            OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[3];
            rs[0] = new OkHttpUtil.RequestData("task", "5");
            rs[1] = new OkHttpUtil.RequestData("folder_id", folder_id);
            rs[2] = new OkHttpUtil.RequestData("pg", String.valueOf(page));//页数
            String data2 = OkHttpUtil.postSyncString(UriUtil.TASK, rs);
            JSONObject jsonObject = new JSONArray("[" + data2 + "]").getJSONObject(0);
            String text = jsonObject.getString("text");
            if (text.length() == 2) break;
            JSONArray jsonArray = new JSONArray(text);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject fileObject = jsonArray.getJSONObject(i);
                String name = fileObject.getString("name_all");
                String file_id = fileObject.getString("id");
                list.add(new FileItem(name, FileItem.ISFILE, file_id));
            }
        }
        return list;
    }

    //新建文件夹 task2
    private boolean setNewFolderSync(String uri, String folder_name, String folder_description) throws IOException, JSONException {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[4];
        String folder_ids[] = uri.split("=");
        String parent_id = folder_ids[folder_ids.length - 1];
        rs[0] = new OkHttpUtil.RequestData("task", "2");
        rs[1] = new OkHttpUtil.RequestData("parent_id", parent_id);
        rs[2] = new OkHttpUtil.RequestData("folder_name", folder_name);
        rs[3] = new OkHttpUtil.RequestData("folder_description", folder_description);
        String data = OkHttpUtil.postSyncString(UriUtil.TASK, rs);
        String info = "";
        JSONArray jsonArray = new JSONArray("[" + data + "]");
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        info = jsonObject.getString("info");
        if (info.equals("创建成功")) {
            return true;
        }
        return false;
    }

    //上传文件 task1
    private boolean uploadFileSync(String file_uri, String folder_id) throws IOException {
        OkHttpClient client = OkHttpUtil.getmOkHttpClient2();
        File file = new File(file_uri);
        String str[] = file_uri.split("/");
        String name = str[str.length - 1];
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"), file);

        //定义请求体，前面三个为表单中的string类型参数，第四个为需要上传的文件
        MultipartBody mBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("size", String.valueOf(file.length()))//***********
                .addFormDataPart("task", "1")
                .addFormDataPart("folder_id", folder_id)//*****************
                .addFormDataPart("name", name)//**********
                .addFormDataPart("upload_file", name, fileBody)//*******
                // .addFormDataPart("type", "text/plain")//********
                .build();
        Request request = new Request.Builder()
                .url(UriUtil.UPLOADFILE)
                .post(mBody)
                .build();
        //设置为post请求，url后面为请求路径，header设置请求头（可以设置多个），post后面设置请求体

        Response response = client.newCall(request).execute();
        String data = response.body().string();
        if (data.indexOf("\\u4e0a\\u4f20\\u6210\\u529f") != -1) {
            return true;
        }
        return false;

    }

    //task3 删除文件夹 task3
    private boolean deleteFolderSync(String holder_id) throws JSONException, IOException {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "3");
        rs[1] = new OkHttpUtil.RequestData("folder_id", holder_id);
        String data = OkHttpUtil.postSyncString(UriUtil.TASK, rs);
        String info = "";
        JSONArray jsonArray = new JSONArray("[" + data + "]");
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        info = jsonObject.getString("info");
        if (info.equals("删除成功")) {
            return true;
        }
        return false;

    }

    //task6 删除文件 task6
    private boolean deleteFileSync(String file_id) throws IOException, JSONException {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "6");
        rs[1] = new OkHttpUtil.RequestData("file_id", file_id);
        String data = OkHttpUtil.postSyncString(UriUtil.TASK, rs);
        String info = "";
        JSONArray jsonArray = new JSONArray("[" + data + "]");
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        info = jsonObject.getString("info");
        if (info.equals("已删除")) {
            return true;
        }
        return false;

    }

    //下载文件
    private void downFileSync(String fileName,String fileId) throws IOException, JSONException {
        String FileHref = getFileHrefSync(fileId);
        String FileSecondHref = getFileSecondHref(FileHref);
        String DownKey = GetDownKey(FileSecondHref);
        String DownUri = GetDownUri(FileSecondHref, DownKey);
        String DownSecondUri = GetDownSecondUri(DownUri);
        downLoadDatabase(DownSecondUri,fileName);
    }

    //获取文件短链接 task22
    private String getFileHrefSync(String file_id) throws IOException, JSONException {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "22");
        rs[1] = new OkHttpUtil.RequestData("file_id", file_id);
        String uri = "";
        String response = OkHttpUtil.postSyncString(UriUtil.TASK, rs);
        JSONObject jsonObject = new JSONArray("[" + response + "]").getJSONObject(0);
        String info = jsonObject.getString("info");
        JSONObject jsonObject2 = new JSONArray("[" + info + "]").getJSONObject(0);
        String f_id = jsonObject2.getString("f_id");
        uri = UriUtil.SHAREHEAD + f_id;
        return uri;
    }

    //获取二级短链接https://www.lanzous.com/ifdfhdsad,得到https://www.lanzous.com/?fnadjic_c
    private String getFileSecondHref(String file_href) throws IOException, JSONException {
        String data = OkHttpUtil.getSyncString(file_href);
        Document document = Jsoup.parse(data);
        Elements element = document.getElementsByClass("ifr2");
        return element.attr("src");
    }

    //解析https://www.lanzous.com/？fnadjic_c得到第二个
    private String GetDownKey(String fileSecondHref) throws IOException, JSONException {
        final String uri = "https://www.lanzous.com/" + fileSecondHref;
        String data = OkHttpUtil.getSyncString(uri);
        Document document = Jsoup.parse(data);
        String str = document.getElementsByTag("script").toString().trim();
        int a = str.indexOf("\t\tvar cots");
        int b = str.indexOf("c_c");
        return str.substring(a + "\t\tvar cots".length() + 4, b + 3);
    }

    //得到一级域名
    private String GetDownUri(String fileSecondHref, String sign) throws IOException, JSONException {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[3];
        rs[0] = new OkHttpUtil.RequestData("action", "downprocess");
        rs[1] = new OkHttpUtil.RequestData("sign", sign);
        rs[2] = new OkHttpUtil.RequestData("ves", "1");
        OkHttpUtil.RequestData[] header = new OkHttpUtil.RequestData[13];
        header[0] = new OkHttpUtil.RequestData("Accept", "application/json, text/javascript, */*");
        header[1] = new OkHttpUtil.RequestData("Accept-Encoding", "gzip, deflate, br");
        header[2] = new OkHttpUtil.RequestData("Accept-Language", "zh-CN,zh;q=0.9");
        header[3] = new OkHttpUtil.RequestData("Connection", "keep-alive");
        header[4] = new OkHttpUtil.RequestData("Content-Length", "112");
        header[5] = new OkHttpUtil.RequestData("Content-Type", "application/x-www-form-urlencoded");
        header[6] = new OkHttpUtil.RequestData("Host", "lanzous.com");
        header[7] = new OkHttpUtil.RequestData("Origin", "https://www.lanzous.com");
        header[8] = new OkHttpUtil.RequestData("Referer", fileSecondHref);
        header[9] = new OkHttpUtil.RequestData("Sec-Fetch-Mode", "cors");
        header[10] = new OkHttpUtil.RequestData("Sec-Fetch-Site", "same-origin");
        header[11] = new OkHttpUtil.RequestData("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
        header[12] = new OkHttpUtil.RequestData("X-Requested-With", "XMLHttpRequest");
        Log.d("66665", "1");
        String data = OkHttpUtil.postSyncString(UriUtil.GETDOWNURI, rs, header);
        JSONObject jsonObject = new JSONArray("[" + data + "]").getJSONObject(0);
        Log.d("66665", "2");
        return jsonObject.getString("url");
    }

    //解析二级域名
    private String GetDownSecondUri(String downUri) throws IOException {
        String address = UriUtil.DOWNFILEHEAD + downUri;
        String path = UriUtil.DOWNFILEPATH + downUri;
        OkHttpUtil.RequestData[] header = new OkHttpUtil.RequestData[11];
        header[0] = new OkHttpUtil.RequestData("authority", "vip.d0.baidupan.com");
        header[1] = new OkHttpUtil.RequestData("method", "GET");
        header[2] = new OkHttpUtil.RequestData("path", path);
        header[3] = new OkHttpUtil.RequestData("scheme", "https");
        header[4] = new OkHttpUtil.RequestData("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        header[5] = new OkHttpUtil.RequestData("accept-encoding", "gzip, deflate, br");
        header[6] = new OkHttpUtil.RequestData("accept-language", "zh-CN,zh;q=0.9");
        header[7] = new OkHttpUtil.RequestData("sec-fetch-mode", "navigate");
        header[8] = new OkHttpUtil.RequestData("sec-fetch-site", "none");
        header[9] = new OkHttpUtil.RequestData("upgrade-insecure-requests", "1");
        header[10] = new OkHttpUtil.RequestData("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36");
        return OkHttpUtil.getSync(address, header).request().url().toString();

    }

    //得到下载链接并下载
    private void downLoadDatabase(String downSecondUri,String fileName) {
        String filename[] = downSecondUri.split("=");
        //创建下载任务,downloadUrl就是下载链接
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downSecondUri));
        //指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir("Download/lanzou", fileName);
        //获取下载管理器
        DownloadManager downloadManager = (DownloadManager) Myapplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

    }

    //登录监听接口
    public interface loginCallbackListener {
        void onError(Exception e);

        void onFinish();
    }

    //获取短链接接口
    public interface FileHrefCallbackListener {
        void onError(Exception e);

        void onFinish(String file_href);
    }

    //页面刷新监听接口
    public interface PageUpdatePageCallbackListener {
        void onError(Exception e);

        void onFinish(List<FileItem> list);
    }

    //增删改查监听接口
    public interface CRUDCallbackListener {
        void onError(Exception e);

        void onFinish();
    }


    /*对外接口
     * ************************************************************************
     * ***********************************************************************
     * ***********************************************************************
     */
    //登录
    public static void Login(final String username, final String password, final loginCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = getInstance().LoginSync(username, password);
                    if (success)
                        listener.onFinish();
                    else
                        listener.onError(new Exception());
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onError(e);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //task22 获取文件短链接
    public static void getFileHref(final String file_id, final FileHrefCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String href = getInstance().getFileHrefSync(file_id);
                    listener.onFinish(href);
                } catch (IOException e) {
                    listener.onError(e);
                } catch (JSONException e) {
                    listener.onError(e);
                }
            }
        }).start();

    }

    //刷新页面
    public static void UpdatePage(final String folder_id, final PageUpdatePageCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<FileItem> list = getInstance().getFolderInfoSync(folder_id);
                    list.addAll(getInstance().getFileInfoSync(folder_id));
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFinish(list);
                            }

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onError(e);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError(e);
                }
            }
        }).start();

    }

    //创建文件夹
    public static void AddFolder(final String uri, final String folder_name, final String folder_description, final CRUDCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Boolean aBoolean = getInstance().setNewFolderSync(uri, folder_name, folder_description);
                    if (aBoolean) {
                        listener.onFinish();
                    } else
                        listener.onError(new Exception());
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onError(e);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError(e);
                }
            }
        }).start();
    }

    //下载文件
    public static void FileDown(final String fileName,final String fileId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getInstance().downFileSync(fileName,fileId);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //文件删除
    public static void DeleteFile(final String file_id, final CRUDCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Boolean aBoolean = getInstance().deleteFileSync(file_id);
                    if (aBoolean) {
                        listener.onFinish();
                    } else
                        listener.onError(new Exception());
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onError(e);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError(e);
                }
            }
        }).start();

    }

    //文件夹删除
    public static void DeleteFolder(final String folder_id, final CRUDCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Boolean aBoolean = getInstance().deleteFolderSync(folder_id);
                    if (aBoolean) {
                        listener.onFinish();
                    } else
                        listener.onError(new Exception());
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onError(e);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError(e);
                }
            }
        }).start();

    }

    //上传文件
    public static Boolean sendFiles(String file_uri, String folder_id) throws IOException {
        return getInstance().uploadFileSync(file_uri, folder_id);
    }

}
