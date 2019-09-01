package com.kulya.lanzou.http;


import android.app.DownloadManager;
import android.content.Context;

import android.net.Uri;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.kulya.lanzou.listview.FileItem;
import com.kulya.lanzou.util.Myapplication;
import com.kulya.lanzou.asyncTask.uploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
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


    private boolean login(final String username, final String password) throws IOException {
        String data = OkHttpUtil.getSyncString(UriUtil.GETFORMHASH);
        Document document = Jsoup.parse(data);
        Elements element = document.select("input[name=formhash]");
        String formhash = element.attr("value");

        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[6];
        rs[0] = new OkHttpUtil.RequestData("formhash", formhash);
        rs[1] = new OkHttpUtil.RequestData("username", username);
        rs[2] = new OkHttpUtil.RequestData("password", password);
        rs[3] = new OkHttpUtil.RequestData("action", "login");
        rs[4] = new OkHttpUtil.RequestData("task", "login");
        rs[5] = new OkHttpUtil.RequestData("ref", "https://up.woozooo.com/");
        String info = OkHttpUtil.postSyncString(UriUtil.LOGIN, rs);

        Document document2 = Jsoup.parse(info);
        Elements element2 = document2.getElementsByClass("info_b2");
        String linkText = element2.text();
        Log.d("2222224", linkText);
        if (linkText.equals("登录成功，欢迎您回来。")) {
            return true;
        } else if (document.getElementsByClass("e_u").text().equals("账号不正确 密码不正确")) {
            MyCookieJar.resetCookies();
            return false;
        }
        return false;
    }

    //获取短链接https://www.lanzous.com/ifdfhdsad,得到https://www.lanzous.com/?fnadjic_c
    private void downFile(String file_id) throws IOException, JSONException {
        //获取短链接
        String file_href = getFileHrefSync(file_id);
        Log.d("down1", file_href);
        //得到key
        String data = OkHttpUtil.getSyncString(file_href);
        Log.d("down12", data);
        Document document = Jsoup.parse(data);
        Elements element = document.getElementsByClass("ifr2");
        String linkHref = element.attr("src");
        Log.d("down12", linkHref);
        GetDownKey(linkHref);

    }

    //解析https://www.lanzous.com/？fnadjic_c得到第二个
    private void GetDownKey(String url) throws IOException, JSONException {
        final String uri = "https://www.lanzous.com" + url;
        Response data1 = OkHttpUtil.getSync(uri);
        String data = data1.body().string();
        Log.d("getdownkey:data", data);
        Document document = Jsoup.parse(data);
        String str = document.getElementsByTag("script").toString().trim();
        int a = str.indexOf("sign");
        int b = str.indexOf("c_c");
        String dat = str.substring(a + 7, b + 3);
        Log.d("down234", dat + "chsduic");
        GetDownUri(dat, uri);
    }

    //得到一级域名
    private void GetDownUri(String id, String uri) throws IOException, JSONException {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[3];
        rs[0] = new OkHttpUtil.RequestData("action", "downprocess");
        rs[1] = new OkHttpUtil.RequestData("sign", id);
        rs[2] = new OkHttpUtil.RequestData("ves", "1");
        OkHttpUtil.RequestData[] header = new OkHttpUtil.RequestData[15];

        header[0] = new OkHttpUtil.RequestData("authority", "www.lanzous.com");
        header[1] = new OkHttpUtil.RequestData("method", "POST");
        header[2] = new OkHttpUtil.RequestData("path", "/ajaxm.php");
        header[3] = new OkHttpUtil.RequestData("scheme", "https");
        header[4] = new OkHttpUtil.RequestData("accept", "application/json, text/javascript, */*");
        header[5] = new OkHttpUtil.RequestData("accept-encoding", "gzip, deflate, br");
        header[6] = new OkHttpUtil.RequestData("accept-language", "zh-CN,zh;q=0.9");
        header[7] = new OkHttpUtil.RequestData("content-length", "114");
        header[8] = new OkHttpUtil.RequestData("content-type", "application/x-www-form-urlencoded");
        header[9] = new OkHttpUtil.RequestData("origin", "https://www.lanzous.com");
        header[10] = new OkHttpUtil.RequestData("referer", uri);
        header[11] = new OkHttpUtil.RequestData("sec-fetch-mode", "cors");
        header[12] = new OkHttpUtil.RequestData("sec-fetch-site", "same-origin");
        header[13] = new OkHttpUtil.RequestData("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
        header[14] = new OkHttpUtil.RequestData("x-requested-with", "XMLHttpRequest");
        String data = OkHttpUtil.postSyncString(UriUtil.GETDOWNURI, rs, header);
        Log.d("down3:data", data);
        String url = "";
        JSONObject jsonObject = new JSONArray("[" + data + "]").getJSONObject(0);
        url = jsonObject.getString("url");
        url.replace("\\\"", "");
        Log.d("down3:name0", url);
        downLoadDatabase(UriUtil.DOWNFILEHEAD + url, UriUtil.DOWNFILEPATH + url);


    }

    //解析二级域名
    private Response loginGet2(String address, String path) throws IOException {
        OkHttpUtil.RequestData[] header = new OkHttpUtil.RequestData[9];
        header[0] = new OkHttpUtil.RequestData("path", path);
        header[1] = new OkHttpUtil.RequestData("scheme", "https");
        header[2] = new OkHttpUtil.RequestData("upgrade-insecure-requests", "1");
        header[3] = new OkHttpUtil.RequestData("authority", "vip.d0.baidupan.com");
        header[4] = new OkHttpUtil.RequestData("method", "GET");
        header[5] = new OkHttpUtil.RequestData("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        header[6] = new OkHttpUtil.RequestData("accept-encoding", "gzip, deflate, br");
        header[7] = new OkHttpUtil.RequestData("accept-language", "zh-CN,zh;q=0.9");
        header[8] = new OkHttpUtil.RequestData("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
        return OkHttpUtil.getSync(address, header);

    }

    //得到下载链接并下载
    private void downLoadDatabase(String url, String path) throws IOException {

        Response response = loginGet2(url, path);
        String downurl = response.request().url().toString();
        Headers headers = response.headers();
        String Disposition = headers.get("Content-Disposition");
        String filename[] = Disposition.split("=");

        //创建下载任务,downloadUrl就是下载链接
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downurl));
        //指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir("Download/lanzou", filename[1]);
        //获取下载管理器
        DownloadManager downloadManager = (DownloadManager) Myapplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

    }

    //创建页面，文件夹数据
    private List<FileItem> UpdatePageSync(final String uri) throws IOException, JSONException {
        List<FileItem> list = new ArrayList<>();
        list.clear();

        String data = OkHttpUtil.getSyncString(uri);
        Document document = Jsoup.parse(data);
        Log.d("888", data);
        Elements element = document.getElementsByClass("f_name2");
        for (Element link : element) {
            String filename = link.text().trim().replace("修", "");
            String linkHref = link.select("a").attr("href");
            list.add(new FileItem(filename, FileItem.ISHOLDER, linkHref));
            Log.d("jump:linkHref", linkHref);
        }
for(int page=1;page<8;page++){
    String folder_ids[] = uri.split("=");
    String folder_id = folder_ids[folder_ids.length - 1];
    OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[3];
    rs[0] = new OkHttpUtil.RequestData("task", "5");
    rs[1] = new OkHttpUtil.RequestData("folder_id", folder_id);
    rs[2] = new OkHttpUtil.RequestData("pg", String.valueOf(page));//页数
    String data2 = OkHttpUtil.postSyncString(UriUtil.GETFILEID, rs);
    Log.d("8881", data2);
    String ss = data2.substring(data2.indexOf("["), data2.indexOf("]") + 1);
    if(ss.length()<=5)
        break;
    Log.d("88812", ss);
    JSONArray jsonArray = new JSONArray(ss);
    for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        String name = jsonObject.getString("name_all");
        String file_id = jsonObject.getString("id");
        Log.d("getItem:file_id", file_id);
        Log.d("getItem:file_id", name);
        list.add(new FileItem(name, FileItem.ISFILE, file_id));
    }
}

        return list;
    }

    //新建文件夹
    private boolean setNewFolder(String uri, String folder_name, String folder_description) throws IOException, JSONException {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[4];
        String folder_ids[] = uri.split("=");
        String parent_id = folder_ids[folder_ids.length - 1];
        rs[0] = new OkHttpUtil.RequestData("task", "2");
        rs[1] = new OkHttpUtil.RequestData("parent_id", parent_id);
        rs[2] = new OkHttpUtil.RequestData("folder_name", folder_name);
        rs[3] = new OkHttpUtil.RequestData("folder_description", folder_description);
        String data = OkHttpUtil.postSyncString(UriUtil.GETFILEID, rs);
        Log.d("setNewFolder:data", data);
        String info = "";
        JSONArray jsonArray = new JSONArray("[" + data + "]");
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        info = jsonObject.getString("info");
        Log.d("setNewFolder:name", info);
        if (info.equals("创建成功")) {
            return true;
        }
        return false;
    }

    //获取文件短链接
    private String getFileHrefSync(String file_id) throws IOException, JSONException {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "22");
        rs[1] = new OkHttpUtil.RequestData("file_id", file_id);
        String uri = "";
        String response = OkHttpUtil.postSyncString(UriUtil.GETFILEID, rs);
        Log.d("downfile:f_id1", response);
        JSONObject jsonObject = new JSONArray("[" + response + "]").getJSONObject(0);
        String info = jsonObject.getString("info");
        JSONObject jsonObject2 = new JSONArray("[" + info + "]").getJSONObject(0);
        String f_id = jsonObject2.getString("f_id");
        uri = UriUtil.SHAREHEAD + f_id;
        Log.d("downfile:f_id", uri);
        return uri;
    }

    //上传文件
    private boolean sendFromDataPostRequest(String file_uri, String folder_id) throws IOException {
        OkHttpClient client = OkHttpUtil.getmOkHttpClient2();
        String url = "https://pc.woozooo.com/fileup.php";
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
                .url(url)
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

    //task3 删除文件夹
    private boolean folderDelete(String holder_id) throws JSONException, IOException {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "3");
        rs[1] = new OkHttpUtil.RequestData("folder_id", holder_id);
        String data = OkHttpUtil.postSyncString(UriUtil.GETFILEID, rs);
        Log.d("deleteHolder:data", data);
        String info = "";
        JSONArray jsonArray = new JSONArray("[" + data + "]");
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        info = jsonObject.getString("info");
        Log.d("deleteHolder:name", info);
        if (info.equals("删除成功")) {
            return true;
        }
        return false;

    }

    //task6 删除文件
    private boolean fileDelete(String file_id) throws IOException, JSONException {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "6");
        rs[1] = new OkHttpUtil.RequestData("file_id", file_id);
        String data = OkHttpUtil.postSyncString(UriUtil.GETFILEID, rs);
        Log.d("deletefile:data", data);
        String info = "";
        JSONArray jsonArray = new JSONArray("[" + data + "]");
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        info = jsonObject.getString("info");
        Log.d("deletefile:name", info);
        if (info.equals("已删除")) {
            return true;
        }
        return false;

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
    public static void Login(final String username, final String password, final loginCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = getInstance().login(username, password);
                    if (success)
                        listener.onFinish();
                    else
                        listener.onError(new Exception());
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onError(e);
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
    public static void UpdatePage(final String uri, final PageUpdatePageCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<FileItem> list = getInstance().UpdatePageSync(uri);
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
                    Boolean aBoolean = getInstance().setNewFolder(uri, folder_name, folder_description);
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
    public static void FileDown(final String file_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getInstance().downFile(file_id);
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
                    Boolean aBoolean = getInstance().fileDelete(file_id);
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
                    Boolean aBoolean = getInstance().folderDelete(folder_id);
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
    public static Boolean sendFiles( String file_uri, String folder_id) throws IOException {
        return getInstance().sendFromDataPostRequest(file_uri,folder_id);
    }

}
