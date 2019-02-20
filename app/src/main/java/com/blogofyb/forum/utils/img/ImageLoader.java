package com.blogofyb.forum.utils.img;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.blogofyb.forum.R;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoader {
    private final int DOWNLOAD_FINISH = 0;

    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;
    private ImageResizer mImageResizer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case DOWNLOAD_FINISH:
                    Result result = (Result) message.obj;
                    result.mImageView.setImageBitmap(result.mBitmap);
                    break;
            }
        }
    };

    public ImageLoader() {
        final int MAX_SIZE = (int) Runtime.getRuntime().maxMemory() / 8;
        mLruCache = new LruCache<String, Bitmap>(MAX_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                Log.e("MEM", value.getByteCount() + "");
                return value.getByteCount();
            }
        };
        Log.e("MEM", MAX_SIZE + "");
    }

    public void set(final ImageView imageView, final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = findBitmapFromLruCache(url);
                if (bitmap == null) {
                    bitmap = downloadBitmapFromUrl(url);
                }
                Message message = new Message();
                Result result = new Result(url, imageView, bitmap);
                message.what = DOWNLOAD_FINISH;
                message.obj = result;
                handler.sendMessage(message);
            }
        }).start();
    }

    private Bitmap findBitmapFromLruCache(String url) {
        return mLruCache.get(url);
    }

    private Bitmap findBitmapFromDiskLruCache(String url) {
        return null;
    }

    private Bitmap downloadBitmapFromUrl(String url) {
        HttpURLConnection connection = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            URL url1 = new URL(url);
            connection = (HttpURLConnection) url1.openConnection();
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.connect();
            bufferedInputStream = new BufferedInputStream(connection.getInputStream());
            Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);
            if (bitmap != null) {
                savePictureToLruCache(url, bitmap);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    private void savePictureToLruCache(String url, Bitmap bitmap) {
        mLruCache.put(url, bitmap);
    }

    private boolean savePictureToDiskLruCache(String url, Bitmap bitmap) {
        return false;
    }

    public class Result {
        private ImageView mImageView;
        private String mUrl;
        private Bitmap mBitmap;

        public Result (String mUrl, ImageView mImageView, Bitmap mBitmap) {
            this.mBitmap = mBitmap;
            this.mImageView = mImageView;
            this.mUrl = mUrl;
        }
    }
}
