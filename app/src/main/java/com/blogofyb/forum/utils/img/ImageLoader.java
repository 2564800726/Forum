package com.blogofyb.forum.utils.img;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class ImageLoader {
    private final int DOWNLOAD_FINISH = 0;

    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;

    private final int MAX_SIZE = (int) Runtime.getRuntime().maxMemory() / 8;
    private final int DISK_LRU_CACHE_SIZE = 1024 * 1024 * 100;
    private final int DISK_CACHE_INDEX = 0;

    private Context mContext;

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

    public ImageLoader(Context mContext) {
        this.mContext = mContext;
        mLruCache = new LruCache<String, Bitmap>(MAX_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        File diskCacheDir = getDiskCacheDir(this.mContext, "bitmap");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        try {
            mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_LRU_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        final String CACHE_PATH = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable() ?
                context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();
        return new File(CACHE_PATH + File.separator + uniqueName);
    }

    public void set(final ImageView imageView, final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = findBitmapFromLruCache(url);
                if (bitmap == null) {
                    bitmap = findBitmapFromDiskLruCache(imageView, url);
                }
                if (bitmap == null) {
                    if (savePictureToDiskLruCache(url)) {
                        bitmap = findBitmapFromDiskLruCache(imageView, url);
                    }
                }
                savePictureToLruCache(url, bitmap);
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

    private Bitmap findBitmapFromDiskLruCache(ImageView imageView, String url) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashKeyFromUrl(url));
            if (snapshot != null) {
                FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                FileDescriptor fileDescriptor = fileInputStream.getFD();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
                setInSampleSize(options, imageView);
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean downloadBitmapFromUrl(String url, OutputStream outputStream) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url1 = new URL(url);
            connection = (HttpURLConnection) url1.openConnection();
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.connect();
            inputStream = connection.getInputStream();

            byte[] buffer = new byte[1024];
            int length = -1;
            if (outputStream != null) {
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void savePictureToLruCache(String url, Bitmap bitmap) {
        if (url != null && bitmap != null) {
            mLruCache.put(url, bitmap);
        }
    }

    private boolean savePictureToDiskLruCache(String url) {
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(hashKeyFromUrl(url));
            if (editor != null) {
                if (downloadBitmapFromUrl(url, editor.newOutputStream(DISK_CACHE_INDEX))) {
                    editor.commit();
                    return true;
                } else {
                    editor.abort();
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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

    private void setInSampleSize(BitmapFactory.Options options, ImageView imageView) {
        int reqWidth = imageView.getWidth();
        int reqHeight = imageView.getHeight();

        if (reqWidth == 0 || reqHeight == 0) {
            options.inSampleSize = 0;
            return;
        }

        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            while ((halfWidth / inSampleSize) >= reqWidth &&
                    (halfHeight / inSampleSize) >= reqHeight) {
                inSampleSize *= 2;
            }
        }
        options.inSampleSize = inSampleSize;
    }

    private String hashKeyFromUrl(String url) {
        String cacheKey = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            cacheKey = bytesToHexString(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                stringBuilder.append('0');
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }
}
