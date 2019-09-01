package com.kulya.lanzou.asyncTask;


import android.os.AsyncTask;

import com.kulya.lanzou.http.HttpWorker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/9/1 8:53
*/
public class uploadTask extends AsyncTask<List<String>, Integer, int[]> {
    private String folder_id;
    private int tasknum;
    private SendCallbackListener listener;

    private List<List<String>> splitList(List<String> list, int groupSize) {
        int length = list.size();
        int num = (length + groupSize - 1) / groupSize; // TODO
        List<List<String>> newList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            int fromIndex = i * groupSize;
            int toIndex = (i + 1) * groupSize < length ? (i + 1) * groupSize : length;
            newList.add(list.subList(fromIndex, toIndex));
        }
        return newList;
    }

    //上传文件监听接口
    public interface SendCallbackListener {
        void onError(int[] num);

        void onFinish(int[] num);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public uploadTask(String folder_id, SendCallbackListener listener) {
        this.folder_id = folder_id;
        this.listener = listener;
    }

    @Override
    protected int[] doInBackground(List<String>... lists) {
        tasknum = lists[0].size();
        int a[] = {0, 0};

            for (String list :lists[0]) {
                try {
                    Boolean finish = HttpWorker.sendFiles(list, folder_id);
                    if (finish)
                        a[0]++;
                    else
                        a[1]++;
                } catch (IOException e) {
                    a[1]++;
                }
            }
        return a;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(int[] status) {
        if (status[0] + status[1] == tasknum) {
            listener.onFinish(status);
        } else
            listener.onError(status);
    }
}
