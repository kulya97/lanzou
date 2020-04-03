package com.kulya.lanzou;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kulya.lanzou.asyncTask.uploadTask;
import com.kulya.lanzou.http.HttpWorker;
import com.kulya.lanzou.http.UriUtil;
import com.kulya.lanzou.listview.FileItem;
import com.kulya.lanzou.listview.FileListAdapter;
import com.kulya.lanzou.util.Myapplication;
import com.kulya.lanzou.util.activitycollector;
import com.kulya.lanzou.util.baseactivity;
import com.kulya.lanzou.view.addFolderPop;
import com.kulya.lanzou.view.fileInfoPop;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends baseactivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView fileListView;
    String whitelist[] = new String[]{".doc", ".docx", ".zip", ".rar", ".apk", ".ipa", ".txt",
            ".exe", ".7z", ".e", ".z", ".ct", ".ke", ".cetrainer", ".db", ".tar", ".pdf", ".w3x",
            ".epub", ".mobi", ".azw", ".azw3", ".osk", ".osz", ".xpa", ".cpk", ".lua", ".jar",
            ".dmg", ".ppt", ".pptx", ".xls", ".xlsx", ".mp3", ".ipa", ".iso", ".img", ".gho",
            ".ttf", ".ttc", ".txf", ".dwg", ".bat", ".dll"};
    int REQUESTCODE_FROM_ACTIVITY = 1000;
    private List<FileItem> fileList = new ArrayList<>();
    private List<String> history = new ArrayList<>();
    private FileListAdapter adapter;
    private boolean ischeck;
    private FloatingActionButton newFolder;
    private SwipeRefreshLayout swipeRefreshLayout;
    uploadTask.SendCallbackListener listener = new uploadTask.SendCallbackListener() {
        @Override
        public void onError(int[] num) {
            Toast.makeText(Myapplication.getContext(), "部分文件不可传，已成功" + num[0] + "个", Toast.LENGTH_SHORT).show();
            RefreshPage();
        }

        @Override
        public void onFinish(int[] num) {
            Toast.makeText(Myapplication.getContext(), num[0] + "个成功，"+num[1] + "个失败", Toast.LENGTH_SHORT).show();
            RefreshPage();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        openPage("-1");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    //设置标题栏菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.titlemenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //标题栏菜单点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_menu:
                new addFolderPop(this, new addFolderPop.onClick() {
                    @Override
                    public void onClick(String folderName, String folderDescription) {
                        String uri = history.get(history.size() - 1);
                        addFolder(uri, folderName, folderDescription);
                    }
                });
                break;
            case R.id.select_menu:
                for (FileItem lis : fileList) {
                    lis.setIsCheck(!ischeck);
                }
                ischeck = !ischeck;
                adapter.notifyDataSetChanged();
                break;
            case R.id.delete_menu:
                for (FileItem lis : fileList) {
                    if (lis.getIsCheck()) {
                        if (lis.getFileORHolder() == FileItem.ISFILE) {
                            deleteFile_(lis.getHref());
                        } else if (lis.getFileORHolder() == FileItem.ISHOLDER) {
                            String folder_href = lis.getHref();
                            String folder_ids[] = folder_href.split("=");
                            String folder_id = folder_ids[folder_ids.length - 1];
                            deleteFolder(folder_id);
                        }
                    }
                }
                break;
            case R.id.down_menu:
                for (FileItem lis : fileList) {

                    if (lis.getFileORHolder() == FileItem.ISFILE) {
                        if (lis.getIsCheck()) {
                            Log.d("hjy", lis.getFilename());
                            HttpWorker.FileDown(lis.getFilename(),lis.getHref());
                        }
                    }
                }
                break;
            case R.id.share:
                Uri uri = Uri.parse("https://github.com/kulya91/lanzou");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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

    //悬浮按钮点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newFolder:

                new LFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(REQUESTCODE_FROM_ACTIVITY)
                        .withStartPath("/sdcard")
                        .withIsGreater(false)
                        .withIconStyle(Constant.ICON_STYLE_YELLOW)
                        .withFileSize(100000 * 1024)
                        .withFileFilter(whitelist)
                        .start();
                break;
        }
    }

    //选择文件回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FROM_ACTIVITY) {
                List<String> list = data.getStringArrayListExtra("paths");
                String ss[] = history.get(history.size() - 1).split("=");
                String folder_id = ss[ss.length - 1];
                new uploadTask(folder_id, listener).execute(list);
           }
        }
    }

    //下拉刷新事件
    @Override
    public void onRefresh() {
        String uri = history.get(history.size() - 1);
        RefreshPage();
        swipeRefreshLayout.setRefreshing(false);

    }

    //返回键事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (history.size() <= 1) {
                    activitycollector.finishall();
                    System.exit(0);
                    return super.onKeyUp(keyCode, event);
                }
                backPage();
                return false;
        }
        return super.onKeyUp(keyCode, event);
    }

    //listitem点击事件,打开文件夹，唤起详情页
    class itemOnClick implements FileListAdapter.OnItemClickListener {
        @Override
        public void ItemClick(View v, int position) {
            FileItem fileItem = fileList.get(position);
            if (fileItem.getFileORHolder() == FileItem.ISHOLDER) {
                openPage(fileItem.getHref());
            } else if (fileItem.getFileORHolder() == FileItem.ISFILE) {
                new fileInfoPop(MainActivity.this, fileItem, new fileInfoPop.onClick() {
                    @Override
                    public void onClick(int num, FileItem Item) {
                        Log.d("9527", num + " :" + Item.getHref());
                        switch (num) {
                            case fileInfoPop.DELETE:
                                deleteFile_(Item.getHref());
                                break;
                            case fileInfoPop.DOWNLAOD:
                                HttpWorker.FileDown(Item.getFilename(),Item.getHref());
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        }
    }

    //打开页面
    private void openPage(String uri) {
        history.add(uri);
        UpdatePage(uri);
    }

    //返回页面
    private void backPage() {
        history.remove(history.size() - 1);
        String uri = history.get(history.size() - 1);
        UpdatePage(uri);
    }

    //刷新当前页面
    private void RefreshPage() {
        String uri = history.get(history.size() - 1);
        UpdatePage(uri);
    }

    //刷新页面   ,被调用
    private void UpdatePage(final String uri) {

        HttpWorker.UpdatePage(uri, new HttpWorker.PageUpdatePageCallbackListener() {
            @Override
            public void onError(Exception e) {
                Log.d("7777", "onError: ");
            }

            @Override
            public void onFinish(final List<FileItem> list) {
                fileList = list;
                ischeck=false;
                adapter = new FileListAdapter(fileList);
                fileListView.setAdapter(adapter);
                adapter.setMOnItemClickListener(new itemOnClick());
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
                RefreshPage();
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
                RefreshPage();
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
                RefreshPage();
            }
        });
    }

}