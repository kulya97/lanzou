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
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kulya.lanzou.down.DownloadService;
import com.kulya.lanzou.http.HttpUtil;
import com.kulya.lanzou.http.HttpWorker;
import com.kulya.lanzou.http.MyCookieJar;
import com.kulya.lanzou.listview.FileItem;
import com.kulya.lanzou.listview.FileListAdapter;
import com.kulya.lanzou.http.OkHttpUtil;
import com.kulya.lanzou.http.UriUtil;
import com.kulya.lanzou.util.activitycollector;
import com.kulya.lanzou.util.baseactivity;
import com.kulya.lanzou.view.addFolderPop;
import com.kulya.lanzou.view.fileInfoPop;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
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

    //重置
    private void reset() {
        list.clear();
        history.clear();
        jump(UriUtil.HOME, OPENPACKAGE);
    }

    //悬浮按钮点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newFolder:
                new addFolderPop(this, new addFolderPop.onClick() {
                    @Override
                    public void onClick(String folderName, String folderDescription) {
                        String uri = history.get(history.size() - 1);
                        setNewFolder(uri, folderName, folderDescription);
                    }
                });
                break;
        }
    }

    //list下拉刷新事件
    @Override
    public void onRefresh() {
        String uri = history.get(history.size() - 1);
        jump(uri, OPENPACKAGE);
        swipeRefreshLayout.setRefreshing(false);

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

    //listitem点击事件,唤起详情页
    class itemOnClick implements FileListAdapter.OnItemClickListener {
        @Override
        public void ItemClick(View v, int position) {
            FileItem fileItem = list.get(position);
            if (fileItem.getFileORHolder() == FileItem.ISHOLDER) {
                String uri = UriUtil.HHTPHEAD + fileItem.getHref();
                jump(uri, OPENPACKAGE);
            } else if (fileItem.getFileORHolder() == FileItem.ISFILE) {
                final String file_id = fileItem.getHref();
                new fileInfoPop(MainActivity.this, file_id,  new fileInfoPop.onClick() {
                    @Override
                    public void onClick(int num, String file_id) {
                        Log.d("9527", num + " :" + file_id);
                        switch (num) {
                            case fileInfoPop.DELETE:
                                deletefile(file_id);
                                break;
                            case fileInfoPop.DOWNLAOD:
                                HttpWorker.downFile(file_id);
                                break;
                            default:
                                break;
                        }
                    }
                });


            }
        }
    }

    //listitem长按事件，删除文件夹
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

    //创建页面，文件夹数据
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
                //  reset();
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
                    // reset();
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
                //reset();
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



}
