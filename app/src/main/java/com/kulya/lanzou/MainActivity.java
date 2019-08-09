package com.kulya.lanzou;


import android.os.Bundle;
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
import com.kulya.lanzou.util.FileItem;
import com.kulya.lanzou.util.FileListAdapter;
import com.kulya.lanzou.util.OkHttpUtil;
import com.kulya.lanzou.util.UriUtil;
import com.kulya.lanzou.util.activitycollector;
import com.kulya.lanzou.util.baseactivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;

public class MainActivity extends baseactivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView fileList;
    private List<FileItem> list = new ArrayList<>();
    List<String> history = new ArrayList<>();
    private FileListAdapter adapter;
    private final static int OPENPACKAGE = 0;
    private final static int CLOSEPACKAGE = 1;
    private final static int UPDATEPACKAGE = 1;
    private FloatingActionButton newFolder;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PopupWindow mPopWindow;
    //private LinearLayoutManager layoutManager;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newFolder:
                showpopwindow();
                break;
        }
    }

    private void showpopwindow() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.newfloderpopwindow, null);
        mPopWindow = new PopupWindow(contentView,
                RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);
        //显示PopupWindow
        View rootview = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        mPopWindow.setAnimationStyle(R.style.contextMenuAnim);//设置动画
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
                mPopWindow.dismiss();
            }
        });
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        mPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
    }

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
                Log.d("onError", "onError1: ");
            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("9537", data);
                String name = "";
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray("[" + data + "]");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    name = jsonObject.getString("info");
                    Log.d("95372", name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (name.equals("创建成功"))
                    jump(uri, OPENPACKAGE);
                else
                    Toast.makeText(MainActivity.this, "创建失败", Toast.LENGTH_SHORT).show();
            }
        }, rs);
    }

    @Override
    public void onRefresh() {
        String uri = history.get(history.size() - 1);
        jump(uri, OPENPACKAGE);
        swipeRefreshLayout.setRefreshing(false);

    }

    class itemOnClick implements FileListAdapter.OnItemClickListener {
        @Override
        public void ItemClick(View v, int position) {
            FileItem fileItem = list.get(position);
            if (fileItem.getSrc() == R.drawable.p1) {
                String uri = UriUtil.HHTPHEAD + fileItem.getHref();
                jump(uri, OPENPACKAGE);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        jump(UriUtil.HOME, OPENPACKAGE);
    }

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

    private void jump(final String uri, final int action) {

        OkHttpUtil.getAsync(uri, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Toast.makeText(MainActivity.this, "获取目录失败，请检查网络", Toast.LENGTH_SHORT).show();
                Log.d("jump9527", "bug");
            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("jump9527", data);
                Document document = Jsoup.parse(data);
                Elements element = document.getElementsByClass("f_name2");
                list.clear();
                for (Element link : element) {
                    String filename = link.text().trim().replace("修", "");
                    String linkHref = link.select("a").attr("href");
                    list.add(new FileItem(filename, R.drawable.p1, linkHref));
                }
                String folder_ids[] = uri.split("=");
                String folder_id = folder_ids[folder_ids.length - 1];
                if (uri.equals(UriUtil.HOME))
                    folder_id = "-1";
                if (action == OPENPACKAGE)
                    history.add(uri);
                else if (action == CLOSEPACKAGE)
                    history.remove(history.size() - 1);
                getItem(folder_id);
            }
        });


    }

    private void getItem(String folder_id) {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[3];
        rs[0] = new OkHttpUtil.RequestData("task", "5");
        rs[1] = new OkHttpUtil.RequestData("folder_id", folder_id);
        rs[2] = new OkHttpUtil.RequestData("pg", "1");//页数
        OkHttpUtil.postAsync(UriUtil.GETFILEID, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d("getItem9527", "bug");
            }

            @Override
            public void onResponse(byte[] response) {
                try {
                    String data = new String(response);
                    Log.d("getItem9527", data);
                    String ss = data.substring(data.indexOf("["), data.indexOf("]") + 1);
                    JSONArray jsonArray = new JSONArray(ss);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String name = jsonObject.getString("name_all");
                        list.add(new FileItem(name, R.drawable.p2, null));
                    }
                    adapter.setMOnItemClickListener(new itemOnClick());
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.d("getItem9527", "bug");
                }
            }
        }, rs);

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
}