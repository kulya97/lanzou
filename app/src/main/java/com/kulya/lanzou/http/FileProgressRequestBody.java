package com.kulya.lanzou.http;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * @author: kulya91
 * @date: 2021/1/3
 * @description:
 */
public class FileProgressRequestBody extends RequestBody {

    public interface ProgressListener {
        void transferred(double size);
    }

private int segmentSize=128;
    protected File file;
    protected ProgressListener listener;
    protected String contentType;
     private long fileLength;

    public FileProgressRequestBody(String contentType, File file, ProgressListener listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = listener;
        fileLength=contentLength();
        segmentSize= 2*1024;
        Log.d("789", fileLength+":"+segmentSize);
    }

    protected FileProgressRequestBody() {
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(file);
            long total = 0;
            long read;
            while ((read = source.read(sink.buffer(), segmentSize)) != -1) {
                total += read;
                sink.flush();
                this.listener.transferred((double)(total)/fileLength*100);
            }
        } finally {
            Util.closeQuietly(source);
        }
    }

}