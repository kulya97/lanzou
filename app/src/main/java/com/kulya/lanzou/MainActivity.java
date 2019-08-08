package com.kulya.lanzou;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Printer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.kulya.lanzou.util.FileItem;
import com.kulya.lanzou.util.FileListAdapter;
import com.kulya.lanzou.util.OkHttpUtil;
import com.kulya.lanzou.util.UriUtil;
import com.kulya.lanzou.util.activitycollector;
import com.kulya.lanzou.util.baseactivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;

public class MainActivity extends baseactivity {
    private RecyclerView fileList;
    private List<FileItem> list = new ArrayList<>();
    List<String> history = new ArrayList<>();
    private FileListAdapter adapter;
    private final static int OPENPACKAGE = 0;
    private final static int CLOSEPACKAGE = 1;

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
    }
}