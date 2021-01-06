package com.kulya.lanzou;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kulya.lanzou.http.HttpWorker;
import com.kulya.lanzou.listview.FileItem;
import com.kulya.lanzou.listview.FileListAdapter;
import com.kulya.lanzou.util.activitycollector;
import com.kulya.lanzou.util.baseactivity;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIProgressBar;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.topbar)
    Toolbar topbar;
    @BindView(R.id.fileList)
    RecyclerView fileListView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.newFolder)
    FloatingActionButton newFolder;

    String whitelist[] = new String[]{".doc", ".docx", ".zip", ".rar", ".apk", ".ipa", ".txt",
            ".exe", ".7z", ".e", ".z", ".ct", ".ke", ".cetrainer", ".db", ".tar", ".pdf", ".w3x",
            ".epub", ".mobi", ".azw", ".azw3", ".osk", ".osz", ".xpa", ".cpk", ".lua", ".jar",
            ".dmg", ".ppt", ".pptx", ".xls", ".xlsx", ".mp3", ".ipa", ".iso", ".img", ".gho",
            ".ttf", ".ttc", ".txf", ".dwg", ".bat", ".dll"};
    int REQUESTCODE_FROM_ACTIVITY = 1000;
    @BindView(R.id.fileProgress)
    QMUIProgressBar fileProgress;
    @BindView(R.id.allProgress)
    QMUIProgressBar allProgress;
    private List<FileItem> fileList = new ArrayList<>();
    private List<String> history = new ArrayList<>();
    private FileListAdapter adapter;
    private boolean ischeck;
    private boolean isMultipleChoice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
        List<FileItem> mList = new ArrayList<>();
        switch (item.getItemId()) {
            case R.id.new_menu:
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(MainActivity.this);
                builder.setTitle("创建文件夹")
                        .setPlaceholder("在此输入文件夹名称")
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                String text = builder.getEditText().getText().toString();
                                if (text != null && text.length() > 0) {
                                    String uri = history.get(history.size() - 1);
                                    addFolder(uri, text);
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(MainActivity.this, "请填入文件夹名", LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
                break;
            case R.id.select_menu:
                if (!adapter.getSelectedMode()) {
                    break;
                }
                for (FileItem lis : fileList) {
                    lis.setIsCheck(!ischeck);
                }
                ischeck = !ischeck;
                adapter.notifyDataSetChanged();
                break;
            case R.id.delete_menu:
                mList = adapter.getSelectedItem();
                for (FileItem lis : mList) {
                    if (lis.getIsCheck()) {
                        if (lis.getFileORHolder() == FileItem.ISFILE) {
                            deleteFile_(lis.getId());
                        } else if (lis.getFileORHolder() == FileItem.ISHOLDER) {
                            String folder_href = lis.getId();
                            String folder_ids[] = folder_href.split("=");
                            String folder_id = folder_ids[folder_ids.length - 1];
                            deleteFolder(folder_id);
                        }
                    }
                }
                break;
            case R.id.down_menu:
                mList = adapter.getSelectedItem();
                for (FileItem lis : mList) {

                    if (lis.getFileORHolder() == FileItem.ISFILE) {
                        if (lis.getIsCheck()) {
                            Log.d("hjy", lis.getFilename());
                            HttpWorker.FileDown(lis.getFilename(), lis.getId());
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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        fileListView.setLayoutManager(linearLayoutManager);
        adapter = new FileListAdapter(fileList);
        adapter.setOnItemClickListener2(new itemOnClick());
        fileListView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        setSupportActionBar(topbar);
    }

    //悬浮按钮点击事件
    @OnClick(R.id.newFolder)
    public void onClick() {
        new LFilePicker()
                .withActivity(MainActivity.this)
                .withRequestCode(REQUESTCODE_FROM_ACTIVITY)
                .withStartPath("/sdcard")
                .withIsGreater(false)
                .withIconStyle(Constant.ICON_STYLE_YELLOW)
                .withFileSize(100000 * 1024)
                .withFileFilter(whitelist)
                .start();
    }

    //选择文件回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FROM_ACTIVITY) {
                fileProgress.setVisibility(View.VISIBLE);
                allProgress.setVisibility(View.VISIBLE);
                fileProgress.setQMUIProgressBarTextGenerator(new QMUIProgressBar.QMUIProgressBarTextGenerator() {
                    @Override
                    public String generateText(QMUIProgressBar progressBar, int value, int maxValue) {
                        return value + "/" + maxValue;
                    }
                });
                allProgress.setQMUIProgressBarTextGenerator(new QMUIProgressBar.QMUIProgressBarTextGenerator() {
                    @Override
                    public String generateText(QMUIProgressBar progressBar, int value, int maxValue) {
                        return value + "/" + maxValue;
                    }
                });
                fileProgress.setProgress(0, false);
                allProgress.setProgress(0, false);
                List<String> list = data.getStringArrayListExtra("paths");
                String ss[] = history.get(history.size() - 1).split("=");
                String folder_id = ss[ss.length - 1];
                int fileCount = list.size();
                allProgress.setMaxValue(fileCount);
                HttpWorker.sendFiles(list, folder_id, new HttpWorker.UpLoadCallbackListener() {
                    @Override
                    public void onError(int count) {
                        fileProgress.setProgress(0, false);

                    }

                    @Override
                    public void Progress(double progress) {
                        fileProgress.setProgress((int) progress);
                    }

                    @Override
                    public void onFinish(int count) {
                        allProgress.setProgress(count + 1);
                        fileProgress.setProgress(0, false);
                        RefreshPage();
                        if (count >= fileCount - 1) {
                            fileProgress.setVisibility(View.GONE);
                            allProgress.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    }

    //下拉刷新事件
    @Override
    public void onRefresh() {
        RefreshPage();
        swipeRefreshLayout.setRefreshing(false);
    }

    //返回键事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (adapter.getSelectedMode()) {
                    adapter.setSelectedMode(false);
                    return false;
                }
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
    class itemOnClick implements FileListAdapter.OnClickListener {
        @Override
        public void onItemClick(View view, int position) {
            final FileItem fileItem = fileList.get(position);
            if (fileItem.getFileORHolder() == FileItem.ISHOLDER) {
                openPage(fileItem.getId());
            } else if (fileItem.getFileORHolder() == FileItem.ISFILE) {
                new QMUIBottomSheet.BottomListSheetBuilder(MainActivity.this)
                        .setGravityCenter(true)
                        .setAddCancelBtn(true)
                        .addItem(fileItem.getFileUrl(), "url")
                        .addItem("下载", "down")
                        .addItem("删除文件", "delete")
                        .addItem("移动文件", "move")
                        .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                            @Override
                            public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                                dialog.dismiss();
                                switch (tag) {
                                    case "down":
                                        HttpWorker.FileDown(fileItem.getFilename(), fileItem.getId());
                                        break;
                                    case "delete":
                                        deleteFile_(fileItem.getId());
                                        break;
                                    case "url":
                                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        cm.setText(fileItem.getFileUrl());
                                        Toast.makeText(MainActivity.this, "复制到剪切板", LENGTH_SHORT).show();
                                        break;
                                    default:
                                        break;

                                }
                            }
                        }).build().show();
            }
        }

        @Override
        public boolean onItemLongClick(View view, int position) {
            return false;
        }
    }

    /***********页面进退刷新***********/
    //刷新页面,被调用
    private void UpdatePage(final String uri) {
        final QMUITipDialog mdialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mdialog.show();
        HttpWorker.UpdatePage(uri, new HttpWorker.PageUpdatePageCallbackListener() {
            @Override
            public void onError(Exception e) {
                mdialog.dismiss();
            }

            @Override
            public void onFinish(final List<FileItem> list) {
                mdialog.dismiss();
                fileList.clear();
                fileList = list;
                ischeck = false;
                adapter = new FileListAdapter(fileList);
                fileListView.setAdapter(adapter);
                adapter.setOnItemClickListener2(new itemOnClick());
                adapter.notifyDataSetChanged();

            }
        });
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


    //增加文件夹之后刷新
    private void addFolder(final String uri, String folder_name) {
        final QMUITipDialog mdialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mdialog.show();
        HttpWorker.AddFolder(uri, folder_name, new HttpWorker.CRUDCallbackListener() {
            @Override
            public void onError(Exception e) {
                mdialog.dismiss();
            }

            @Override
            public void onFinish() {
                mdialog.dismiss();
                RefreshPage();
            }
        });
    }

    // 删除文件夹
    private void deleteFolder(String holder_id) {
        final QMUITipDialog mdialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mdialog.show();
        HttpWorker.DeleteFolder(holder_id, new HttpWorker.CRUDCallbackListener() {
            @Override
            public void onError(Exception e) {
                mdialog.dismiss();
            }

            @Override
            public void onFinish() {
                mdialog.dismiss();
                RefreshPage();
            }
        });

    }

    // 删除文件
    private void deleteFile_(String file_id) {
        final QMUITipDialog mdialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mdialog.show();
        HttpWorker.DeleteFile(file_id, new HttpWorker.CRUDCallbackListener() {
            @Override
            public void onError(Exception e) {
                mdialog.dismiss();
            }

            @Override
            public void onFinish() {
                mdialog.dismiss();
                RefreshPage();
            }
        });
    }
}