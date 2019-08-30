package com.kulya.lanzou.http;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.kulya.lanzou.MainActivity;
import com.kulya.lanzou.listview.FileItem;
import com.kulya.lanzou.util.Myapplication;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;

/*
项目名称： lanzou
创建人：黄大神
类描述：网络工作类
创建时间：2019/8/30 11:42
*/
public class HttpWorker {

    public interface FileHrefCallbackListener {
        void onError(Exception e);

        void onFinish(String filehref);
    }

    //获取短链接https://www.lanzous.com/ifdfhdsad,得到https://www.lanzous.com/?fnadjic_c
    public static void downFile(String file_id) {
        HttpWorker.getFileHref(file_id, new HttpWorker.FileHrefCallbackListener() {
            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onFinish(String filehref) {
                OkHttpUtil.getAsync(filehref, new OkHttpUtil.ResultCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Log.d("down1", "fdf");

                    }

                    @Override
                    public void onResponse(byte[] response) {
                        String data = new String(response);
                        Log.d("down1", data);
                        Document document = Jsoup.parse(data);
                        Elements element = document.getElementsByClass("ifr2");
                        String linkHref = element.attr("src");
                        Log.d("down1", linkHref);
                        down2(linkHref);
                    }
                });
            }
        });
    }

    //解析https://www.lanzous.com/？fnadjic_c得到第二个
    private static void down2(String url) {
        final String uri = "https://www.lanzous.com" + url;
        OkHttpUtil.getAsync(uri, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d("down2:", "onError: ");
            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("down2:data", data);
                Document document = Jsoup.parse(data);
                String str = document.getElementsByTag("script").toString().trim();
                int a = str.indexOf("sign");
                int b = str.indexOf("c_c");
                String dat = str.substring(a + 7, b + 3);
                Log.d("down234", dat + "chsduic");
                down3(dat, uri);
            }
        });
    }

    private static void down3(String id, String uri) {
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
        OkHttpUtil.postAsync(UriUtil.GETDOWNURI, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d("down3:", "onError: ");

            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("down3:data", data);
                String url = "";
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray("[" + data + "]");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    url = jsonObject.getString("url");
                    url.replace("\\\"", "");
                    Log.d("down3:name0", url);
                    downLoadDatabase(UriUtil.DOWNFILEHEAD + url, UriUtil.DOWNFILEPATH + url);
                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        }, rs, header);
    }

    //解析长连接并下载
    private static void downLoadDatabase(String url, String path) {

        HttpUtil.loginGet2(url, path, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //  Log.d("downLoadDatabase", "sdfd");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                String str3 = response.request().url().toString();
                Log.d("downLoadDatabase", str3);

                Headers headers = response.headers();
                String Disposition = headers.get("Content-Disposition");
                String filename[] = Disposition.split("=");

                //创建下载任务,downloadUrl就是下载链接
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(str3));
                //指定下载路径和下载文件名
                request.setDestinationInExternalPublicDir("1111", filename[1]);
                //获取下载管理器
                DownloadManager downloadManager = (DownloadManager) Myapplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                downloadManager.enqueue(request);


            }
        });
    }

    //task22 获取文件id
    public static void getFileHref(String file_id, final FileHrefCallbackListener listener) {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "22");
        rs[1] = new OkHttpUtil.RequestData("file_id", file_id);
        OkHttpUtil.postAsync(UriUtil.GETFILEID, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d("downfile:", "onError: ");
                MyCookieJar.print();
                listener.onError(e);
            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("downfile:data", data);
                String f_id = "";
                String info = "";
                JSONArray jsonArray = null;
                JSONArray jsonArray2 = null;
                try {
                    jsonArray = new JSONArray("[" + data + "]");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    info = jsonObject.getString("info");
                    jsonArray2 = new JSONArray("[" + info + "]");
                    JSONObject jsonObject2 = jsonArray2.getJSONObject(0);
                    f_id = jsonObject2.getString("f_id");
                    String uri = UriUtil.SHAREHEAD + f_id;
                    Log.d("downfile:f_id", uri);
                    listener.onFinish(uri);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError(e);
                }
            }
        }, rs);
    }


}
