package com.blogofyb.forum.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.blogofyb.forum.R;
import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.img.ImageUploader;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SelectImageActivity extends BaseActivity implements View.OnClickListener {
    private final int CHOOSE_PHOTO = 0;
    private final int TAKE_PHOTO = 1;
    private final int UPLOAD_SUCCESS = 2;
    private final int UPLOAD_FAILED = 3;
    private final int CROP_IMAGE = 4;

    private String key = null;
    private Uri mUri;
    private Uri mOutPutUri;
    private int mWidth;
    private int mHeight;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPLOAD_SUCCESS:
                    Intent intent = new Intent();
                    intent.putExtra("md5", (String) message.obj);
                    setResult(RESULT_OK, intent);
                    Toast.makeText(SelectImageActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                    break;
                case UPLOAD_FAILED:
                    Toast.makeText(SelectImageActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        ActivitiesManager.addActivity(this);
        setContentView(R.layout.layout_choose_resource);
        Intent intent = getIntent();
        if (intent != null) {
            key = intent.getStringExtra("key");
            mWidth = intent.getIntExtra("width", 0);
            mHeight = intent.getIntExtra("height", 0);
        }
        mOutPutUri = Uri.parse("file://" + getExternalCacheDir() + File.separator + "temp_crop.png");
        setTitle("选择图片");
        findViewById(R.id.tv_take_photo).setOnClickListener(this);
        findViewById(R.id.tv_select_from_album).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_take_photo:
                takePhoto();
                break;
            case R.id.tv_select_from_album:
                if (ContextCompat.checkSelfPermission(SelectImageActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SelectImageActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "你取消了授权", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                Crop.of(data.getData(), mOutPutUri).withAspect(mWidth, mHeight).start(this);
                break;
            case TAKE_PHOTO:
                Crop.of(mUri, mOutPutUri).withAspect(mWidth, mHeight).start(this);
                break;
            case Crop.REQUEST_CROP:
                uploadImage(mOutPutUri.getPath());
                break;
        }
    }

    private void uploadImage(String path) {
        ImageUploader.uploadByPath(this, path + ":" + key, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Message message = new Message();
                message.what = UPLOAD_SUCCESS;
                message.obj = response;
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = UPLOAD_FAILED;
                handler.sendMessage(message);
            }
        });
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void takePhoto() {
        File tempImage = new File(getExternalCacheDir(), "temp.jpg");
        try {
            if (tempImage.exists()) {
                tempImage.delete();
            }
            tempImage.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            mUri = FileProvider.getUriForFile(this, "com.blogofyb.forum.test", tempImage);
        } else {
            mUri = Uri.fromFile(tempImage);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void openAlbum() {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
            startActivityForResult(intent, CHOOSE_PHOTO);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, CHOOSE_PHOTO);
        }
    }
}
