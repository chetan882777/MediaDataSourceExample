package com.jacks205.mediadatasourceexample;

import android.media.MediaDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class VideoDataSource extends MediaDataSource {

    public static String VIDEO_URL = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/fileSequence0.ts";
    private volatile byte[] videoBuffer;

    private volatile VideoDownloadListener listener;
    private volatile  boolean isDownloading;


    Runnable downloadVideoRunnable = new Runnable() {
        @Override
        public void run() {
            try{
                URL url = new URL(VIDEO_URL);
                //Open the stream for the file.
                InputStream inputStream = url.openStream();
                //For appending incoming bytes
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int read = 0;
                while (read != -1){ //While there is more data
                    //Read in bytes to data buffer
                    read = inputStream.read();
                    //Write to output stream
                    byteArrayOutputStream.write(read);
                }
                inputStream.close();

                //Flush and set buffer.
                byteArrayOutputStream.flush();
                videoBuffer = byteArrayOutputStream.toByteArray();

                byteArrayOutputStream.close();
                listener.onVideoDownloaded();
            }catch (Exception e){
                listener.onVideoDownloadError(e);
            }finally {
                isDownloading = false;
            }
        }
    };

    public VideoDataSource(){
        isDownloading = false;
    }

    public void downloadVideo(VideoDownloadListener videoDownloadListener){
        if(isDownloading)
            return;
        listener = videoDownloadListener;
        Thread downloadThread = new Thread(downloadVideoRunnable);
        downloadThread.start();
        isDownloading = true;
    }

    @Override
    public synchronized int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        synchronized (videoBuffer){
            int length = videoBuffer.length;
            if (position >= length) {
                return -1; // -1 indicates EOF
            }
            if (position + size > length) {
                size -= (position + size) - length;
            }
            System.arraycopy(videoBuffer, (int)position, buffer, offset, size);
            return size;
        }
    }

    @Override
    public synchronized long getSize() throws IOException {
        synchronized (videoBuffer) {
            return videoBuffer.length;
        }
    }

    @Override
    public synchronized void close() throws IOException {

    }

}
