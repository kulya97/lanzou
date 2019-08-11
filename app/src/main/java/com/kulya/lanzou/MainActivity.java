package com.kulya.lanzou;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kulya.lanzou.down.DownloadService;
import com.kulya.lanzou.util.FileItem;
import com.kulya.lanzou.adapter.FileListAdapter;
import com.kulya.lanzou.http.OkHttpUtil;
import com.kulya.lanzou.http.UriUtil;
import com.kulya.lanzou.util.activitycollector;
import com.kulya.lanzou.util.baseactivity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends baseactivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView fileList;
    private List<FileItem> list = new ArrayList<>();
    List<String> history = new ArrayList<>();
    private FileListAdapter adapter;
    private final static int OPENPACKAGE = 0;
    private final static int CLOSEPACKAGE = 1;
    private final static int UPDATEPACKAGE = 2;
    private FloatingActionButton newFolder;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PopupWindow addPopWindow;
    private PopupWindow filePopWindow;
    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //获取分享链接接口
    public interface FileHrefCallbackListener {
        void onError(Exception e);

        void onFinish(String filehref);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        jump(UriUtil.HOME, OPENPACKAGE);
        Intent startIntent = new Intent(this, DownloadService.class);
        startService(startIntent);
        bindService(startIntent, connection, BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }

    private void initView() {
        fileList = (RecyclerView) findViewById(R.id.fileList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        fileList.setLayoutManager(linearLayoutManager);
        adapter = new FileListAdapter(list);
        fileList.setAdapter(adapter);
        newFolder = (FloatingActionButton) findViewById(R.id.newFolder);
        newFolder.setOnClickListener(this);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void reset() {
        list.clear();
        history.clear();
        jump(UriUtil.HOME, OPENPACKAGE);
    }

    //pop中点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newFolder:
                showFolderTips();
                break;
        }
    }

    //list刷新事件
    @Override
    public void onRefresh() {
        String uri = history.get(history.size() - 1);
        jump(uri, OPENPACKAGE);
        swipeRefreshLayout.setRefreshing(false);

    }

    //list点击事件
    class itemOnClick implements FileListAdapter.OnItemClickListener {
        @Override
        public void ItemClick(View v, int position) {
            FileItem fileItem = list.get(position);
            if (fileItem.getFileORHolder() == FileItem.ISHOLDER) {
                String uri = UriUtil.HHTPHEAD + fileItem.getHref();
                jump(uri, OPENPACKAGE);
            } else if (fileItem.getFileORHolder() == FileItem.ISFILE) {
                final String file_id = fileItem.getHref();
                getFileHref(file_id, new FileHrefCallbackListener() {
                    @Override
                    public void onError(Exception e) {
                        reset();
                    }

                    @Override
                    public void onFinish(String filehref) {
                        showFileTips(file_id, filehref);
                    }
                });

            }
        }
    }

    //list长按事件
    class itemLongOnClick implements FileListAdapter.OnItemLongClickListener {

        @Override
        public boolean onLongClick(View view, int position) {
            FileItem fileItem = list.get(position);
            Log.d("onLongClick", "onLongClick: ");
            if (fileItem.getFileORHolder() == FileItem.ISHOLDER) {
                String folder_href = fileItem.getHref();
                String folder_ids[] = folder_href.split("=");
                String folder_id = folder_ids[folder_ids.length - 1];
                Log.d("onLongClick2", folder_id);
                deleteFolder(folder_id);
            }
            return false;
        }
    }

    //返回键事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (history.size() == 1) {
                    activitycollector.finishall();
                    System.exit(0);
                    return super.onKeyUp(keyCode, event);
                }
                String uri = history.get(history.size() - 2);
                jump(uri, CLOSEPACKAGE);
                return false;
        }
        return super.onKeyUp(keyCode, event);
    }

    //显示新建文件创建窗口
    private void showFolderTips() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.newfloderpopwindow, null);
        addPopWindow = new PopupWindow(contentView,
                RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        addPopWindow.setContentView(contentView);
        //显示PopupWindow
        View rootview = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        addPopWindow.setAnimationStyle(R.style.contextMenuAnim);//设置动画
        final EditText name = contentView.findViewById(R.id.enter_name);
        final EditText description = contentView.findViewById(R.id.description);
        Button newfolder = contentView.findViewById(R.id.newFolder);
        newfolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newfoldername = name.getText().toString();
                String newfolderdescription = description.getText().toString();
                if (newfoldername.equals("")) {
                    Toast.makeText(MainActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
                    newfoldername = "";
                    newfolderdescription = "";
                } else {
                    String uri = history.get(history.size() - 1);
                    setNewFolder(uri, newfoldername, newfolderdescription);
                }
                addPopWindow.dismiss();
            }
        });
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        addPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        addPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 600);
    }

    //显示新建文件夹创建窗口
    private void showFileTips(final String file_id, String filehref) {
        View contentView = LayoutInflater.from(this).inflate(R.layout.filetips, null);
        filePopWindow = new PopupWindow(contentView,
                RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        filePopWindow.setContentView(contentView);
        //显示PopupWindow
        View rootview = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        filePopWindow.setAnimationStyle(R.style.contextMenuAnim);//设置动画
        final Button href = contentView.findViewById(R.id.filehref);
        href.setText(filehref);
        Button deltefile = contentView.findViewById(R.id.deletefile);
        Button downfile = contentView.findViewById(R.id.downloadfile);
        Button movefile = contentView.findViewById(R.id.movefile);
        Button pass = contentView.findViewById(R.id.pass);
        deltefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletefile(file_id);
                filePopWindow.dismiss();
            }
        });
        downfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                down1(file_id);
                filePopWindow.dismiss();
            }
        });
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);

        filePopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        filePopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
    }

    //创建页面文件夹数据
    private void jump(final String uri, final int action) {

        OkHttpUtil.getAsync(uri, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                reset();
                Toast.makeText(MainActivity.this, "获取目录失败，请检查网络", Toast.LENGTH_SHORT).show();
                Log.d("jump:", "bug");
            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("jump:data", data);
                Document document = Jsoup.parse(data);
                Elements element = document.getElementsByClass("f_name2");
                list.clear();
                for (Element link : element) {
                    String filename = link.text().trim().replace("修", "");
                    String linkHref = link.select("a").attr("href");
                    list.add(new FileItem(filename, FileItem.ISHOLDER, linkHref));
                    Log.d("jump:linkHref", linkHref);
                }
                String folder_ids[] = uri.split("=");
                String folder_id = folder_ids[folder_ids.length - 1];

                if (action == OPENPACKAGE)
                    history.add(uri);
                else if (action == CLOSEPACKAGE)
                    history.remove(history.size() - 1);
                else if (action == UPDATEPACKAGE) {
                }
                getItem(folder_id);
            }
        });

    }

    //task5 创建页面文件数据
    private void getItem(String folder_id) {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[3];
        rs[0] = new OkHttpUtil.RequestData("task", "5");
        rs[1] = new OkHttpUtil.RequestData("folder_id", folder_id);
        rs[2] = new OkHttpUtil.RequestData("pg", "1");//页数
        OkHttpUtil.postAsync(UriUtil.GETFILEID, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d("getItem:", "bug");
                reset();
            }

            @Override
            public void onResponse(byte[] response) {
                try {
                    String data = new String(response);
                    Log.d("getItem:data", data);
                    String ss = data.substring(data.indexOf("["), data.indexOf("]") + 1);
                    JSONArray jsonArray = new JSONArray(ss);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String name = jsonObject.getString("name_all");
                        String file_id = jsonObject.getString("id");
                        Log.d("getItem:file_id", file_id);
                        list.add(new FileItem(name, FileItem.ISFILE, file_id));
                    }
                    adapter.setMOnItemClickListener(new itemOnClick());
                    adapter.setMOnItemLongClickListener(new itemLongOnClick());
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.d("getItem:", "bug");
                    reset();
                }
            }
        }, rs);

    }

    //task2 创建文件夹
    private void setNewFolder(final String uri, String folder_name, String folder_description) {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[4];
        String folder_ids[] = uri.split("=");
        String parent_id = folder_ids[folder_ids.length - 1];
        rs[0] = new OkHttpUtil.RequestData("task", "2");
        rs[1] = new OkHttpUtil.RequestData("parent_id", parent_id);
        rs[2] = new OkHttpUtil.RequestData("folder_name", folder_name);
        rs[3] = new OkHttpUtil.RequestData("folder_description", folder_description);
        OkHttpUtil.postAsync(UriUtil.GETFILEID, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d("setNewFolder", "onError: ");
                reset();
            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("setNewFolder:data", data);
                String info = "";
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray("[" + data + "]");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    info = jsonObject.getString("info");
                    Log.d("setNewFolder:name", info);
                } catch (JSONException e) {
                    e.printStackTrace();
                    reset();
                }
                if (info.equals("创建成功")) {
                    jump(uri, UPDATEPACKAGE);
                }
                Toast.makeText(MainActivity.this, info, Toast.LENGTH_SHORT).show();
            }
        }, rs);
    }

    //task3 删除文件夹
    private void deleteFolder(String holder_id) {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "3");
        rs[1] = new OkHttpUtil.RequestData("folder_id", holder_id);
        OkHttpUtil.postAsync(UriUtil.GETFILEID, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d("deleteHolder:", "onError: ");
                reset();
            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("deleteHolder:data", data);
                String info = "";
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray("[" + data + "]");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    info = jsonObject.getString("info");
                    Log.d("deleteHolder:name", info);
                } catch (JSONException e) {
                    e.printStackTrace();
                    reset();
                }
                Toast.makeText(MainActivity.this, info, Toast.LENGTH_SHORT).show();
                String uri = history.get(history.size() - 1);
                jump(uri, UPDATEPACKAGE);

            }
        }, rs);

    }

    //task6 删除文件
    private void deletefile(String file_id) {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "6");
        rs[1] = new OkHttpUtil.RequestData("file_id", file_id);
        OkHttpUtil.postAsync(UriUtil.GETFILEID, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d("deletefile:", "onError: ");
                reset();
            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("deletefile:data", data);
                String info = "";
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray("[" + data + "]");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    info = jsonObject.getString("info");
                    Log.d("deletefile:name", info);
                } catch (JSONException e) {
                    e.printStackTrace();
                    reset();
                }
                Toast.makeText(MainActivity.this, info, Toast.LENGTH_SHORT).show();
                String uri = history.get(history.size() - 1);
                jump(uri, UPDATEPACKAGE);
            }
        }, rs);
    }

    //task22 获取文件链接
    private void getFileHref(String file_id, final FileHrefCallbackListener listener) {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("task", "22");
        rs[1] = new OkHttpUtil.RequestData("file_id", file_id);
        OkHttpUtil.postAsync(UriUtil.GETFILEID, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d("downfile:", "onError: ");
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
                Toast.makeText(MainActivity.this, f_id, Toast.LENGTH_SHORT).show();
            }
        }, rs);
    }

    private void down2(String uri) {

        OkHttpUtil.getAsync(uri, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d("down2:", "onError: ");
                reset();
            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("down2:data", data);
                Document document = Jsoup.parse(data);
                String str = document.getElementsByTag("script").toString().trim();
                int a = str.indexOf("sign");
                int b = str.indexOf("dataType");
                String dat = str.substring(a + 7, b - 8);
                Log.d("down234", dat);
                down3(dat);
            }
        });
    }

    private void down3(String id) {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[2];
        rs[0] = new OkHttpUtil.RequestData("action", "downprocess");
        rs[1] = new OkHttpUtil.RequestData("sign", id);
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
                    Log.d("down3:name", url);
                    Log.d("down3:name2", UriUtil.DOWNFILEHEAD + url);
                    // downloadBinder.startDownload("https://minecraft.azureedge.net/bin-linux/bedrock-server-1.12.0.28.zip");
                    String uri23 = "https://vip.d0.baidupan.com/file/?B2EGOA" +
                            "4/VWRUXQszCj9TP1plUmpe5FbkAYkE5VeDUeFXsgLFW4pX5gT/U6MAegBgU3lXOQR9BWEL" +
                            "ZQZsAjsBXwduBjUOblUwVDILbgpvU2FaMlJhXjNWcgFgBHFXNlFlV2MCYVsxVzsEalMzAGc" +
                            "AJ1N5VyIEZgU1CzwGMQJnAS8HNwZmDnxVM1QwC3IKP1M3WjZSZV4zVm0BNQQ7VzhRYFdkAmZb" +
                            "Ylc1BDBTMABiAGBTaFdmBDoFZQs9BjMCZwEyBzAGNA4yVWdUNQs4CnBTM1pwUjxeJ1YhAXUEZ1" +
                            "d5UTlXMwJoWz5XOwRjUzcAZgA1Uy9XJgQyBWoLaQZmAmMBMQcyBmcOZlU1VDELbApvU2ZaNlJ5X" +
                            "idWIQF2BD9XOlF+V3ECM1tqV3QEb1M0AGEAN1M9V2IEbgU1CzgGMgJsASYHcgYhDiRVPFQzC2gKa" +
                            "FNhWjhSZ14xVmQBNQQ0";
                    // downloadBinder.startDownload(uri23);
//                    OkHttpUtil.getAsync(uri23, new OkHttpUtil.ResultCallback() {
//                        @Override
//                        public void onError(Call call, Exception e) {
//                            Log.d("123123", "fdfasd");
//                        }
//
//                        @Override
//                        public void onResponse(byte[] response) {
//                            Log.d("123123", "sdfds");
//                            String data = new String(response);
//                            Log.d("123123", data);
//                        }
//                    });
                    downLoadDatabase("https://vip.d0.baidupan.com/file/?VTNQbgw9BzYGD1NrUWQGagc4ADgFs1b4B7xWt1WTAYcE4QX0XYNXLQYkUH5UdFdwAz8PY15tA2QFXQFuADhbZ1VkUDcMaAdlBmhTM1E1BjEHewBjBSlWaQc4VmdVPQEyBDYFY10mV3MGIFA7VDJXZgNoDzNeLgMxBToBKABtW2pVelA1DGQHNQYzUzVRPAZmBzsAZwVrVjIHZlZmVWwBNARmBWtdMVdnBjFQM1RlV2UDbg9rXmADYAVnATAAaVtoVWFQKQwlBysGJFMjUXAGdAc4AHcFM1YwBz1WaFUxATEENgVgXTRXJQYkUG9UbVczAz8PN14wAzQFNAEyAGhbY1VnUD4MYgdgBn5TI1FwBncHYAA0BXRWcgdmVjxVfgE9BDUFZ102VzYGZFA+VDlXYgNsDzxeJwN0BXIBcABhW2tVYFAxDGQHZwZgUzBRNQY1B2s=");
                } catch (JSONException e) {
                    e.printStackTrace();
                    reset();
                }


            }
        }, rs);
    }

    private void down1(String file_id) {
        getFileHref(file_id, new FileHrefCallbackListener() {
            @Override
            public void onError(Exception e) {
                reset();
            }

            @Override
            public void onFinish(String filehref) {
                OkHttpUtil.getAsync(filehref, new OkHttpUtil.ResultCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Log.d("down1", "fdf");
                        reset();
                    }

                    @Override
                    public void onResponse(byte[] response) {
                        String data = new String(response);
                        Log.d("down1", data);
                        Document document = Jsoup.parse(data);
                        Elements element = document.getElementsByClass("ifr2");
                        String linkHref = element.attr("src");
                        Log.d("down1", linkHref);
                        down2("https://www.lanzous.com" + linkHref);
                    }
                });
            }
        });
    }

    private void downLoadDatabase(String url) {
        //创建下载任务,downloadUrl就是下载链接
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir("", "fsdfds");
        //获取下载管理器
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
        //将下载任务加入下载队列，否则不会进行下载
    }


}
