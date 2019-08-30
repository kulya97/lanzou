package com.kulya.lanzou;


import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kulya.lanzou.down.DownloadService;
import com.kulya.lanzou.http.HttpWorker;
import com.kulya.lanzou.http.OkHttpUtil;
import com.kulya.lanzou.http.UriUtil;
import com.kulya.lanzou.listview.FileItem;
import com.kulya.lanzou.listview.FileListAdapter;
import com.kulya.lanzou.util.activitycollector;
import com.kulya.lanzou.util.baseactivity;
import com.kulya.lanzou.view.addFolderPop;
import com.kulya.lanzou.view.fileInfoPop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;

public class MainActivity extends baseactivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView fileListView;
    private List<FileItem> fileList = new ArrayList<>();
    private List<String> history = new ArrayList<>();
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
        UpdatePage(UriUtil.HOME, OPENPACKAGE);
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
        fileListView = (RecyclerView) findViewById(R.id.fileList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        fileListView.setLayoutManager(linearLayoutManager);
        adapter = new FileListAdapter(fileList);
        fileListView.setAdapter(adapter);
        newFolder = (FloatingActionButton) findViewById(R.id.newFolder);
        newFolder.setOnClickListener(this);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    //重置
    private void reset() {
        fileList.clear();
        history.clear();
        UpdatePage(UriUtil.HOME, OPENPACKAGE);
    }

    //悬浮按
    // 钮点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newFolder:
                new addFolderPop(this, new addFolderPop.onClick() {
                    @Override
                    public void onClick(String folderName, String folderDescription) {
                        String uri = history.get(history.size() - 1);
                        addFolder(uri, folderName, folderDescription);
                    }
                });
                break;
        }
    }

    //list下拉刷新事件
    @Override
    public void onRefresh() {
        String uri = history.get(history.size() - 1);
        UpdatePage(uri, OPENPACKAGE);
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
                UpdatePage(uri, CLOSEPACKAGE);
                return false;
        }
        return super.onKeyUp(keyCode, event);
    }

    //listitem点击事件,唤起详情页
    class itemOnClick implements FileListAdapter.OnItemClickListener {
        @Override
        public void ItemClick(View v, int position) {
            FileItem fileItem = fileList.get(position);
            if (fileItem.getFileORHolder() == FileItem.ISHOLDER) {
                String uri = UriUtil.HHTPHEAD + fileItem.getHref();
                UpdatePage(uri, OPENPACKAGE);
            } else if (fileItem.getFileORHolder() == FileItem.ISFILE) {
                final String file_id = fileItem.getHref();
                new fileInfoPop(MainActivity.this, file_id, new fileInfoPop.onClick() {
                    @Override
                    public void onClick(int num, String file_id) {
                        Log.d("9527", num + " :" + file_id);
                        switch (num) {
                            case fileInfoPop.DELETE:
                                deleteFile_(file_id);
                                break;
                            case fileInfoPop.DOWNLAOD:
                                HttpWorker.FileDown(file_id);
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
            FileItem fileItem = fileList.get(position);
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

    //刷新页面
    private void UpdatePage(final String uri, final int action) {
        if (action == OPENPACKAGE)
            history.add(uri);
        else if (action == CLOSEPACKAGE)
            history.remove(history.size() - 1);
        else if (action == UPDATEPACKAGE) {
        }
        HttpWorker.UpdatePage(uri, new HttpWorker.PageUpdatePageCallbackListener() {
            @Override
            public void onError(Exception e) {
                Log.d("7777", "onError: ");
            }

            @Override
            public void onFinish(final List<FileItem> list) {
                fileList = list;
                adapter = new FileListAdapter(fileList);
                fileListView.setAdapter(adapter);
                adapter.setMOnItemClickListener(new itemOnClick());
                adapter.setMOnItemLongClickListener(new itemLongOnClick());
                adapter.notifyDataSetChanged();
            }
        });
    }

    //增加文件夹之后刷新
    private void addFolder(final String uri, String folder_name, String folder_description) {
        HttpWorker.AddFolder(uri, folder_name, folder_description, new HttpWorker.CRUDCallbackListener() {
            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onFinish() {
                UpdatePage(uri, UPDATEPACKAGE);
            }
        });
    }

    //task3 删除文件夹
    private void deleteFolder(String holder_id) {
        HttpWorker.DeleteFolder(holder_id, new HttpWorker.CRUDCallbackListener() {
            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onFinish() {
                String uri = history.get(history.size() - 1);
                UpdatePage(uri, UPDATEPACKAGE);
            }
        });

    }

    //task6 删除文件
    private void deleteFile_(String file_id) {
        HttpWorker.DeleteFile(file_id, new HttpWorker.CRUDCallbackListener() {
            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onFinish() {
                String uri = history.get(history.size() - 1);
                UpdatePage(uri, UPDATEPACKAGE);
            }
        });
    }

}