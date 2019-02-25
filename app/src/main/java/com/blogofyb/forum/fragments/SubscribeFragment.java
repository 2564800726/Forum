package com.blogofyb.forum.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.blogofyb.forum.R;
import com.blogofyb.forum.utils.constant.ServerInformation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubscribeFragment extends Fragment {
    private ImageView mImageView;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_subscribe, container, false);
        Button button = view.findViewById(R.id.btn_test_upload);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage = new File(getContext().getExternalCacheDir(), "test.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(getContext(), "com.blogofyb.forum.test", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 1);
            }
        });
        mImageView = view.findViewById(R.id.iv_test_photo);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(imageUri));
                        mImageView.setImageBitmap(bitmap);
                        InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                        final DataInputStream dataInputStream = new DataInputStream(inputStream);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    URL url = new URL(ServerInformation.UPLOAD_IMAGE);
                                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                    httpURLConnection.setDoInput(true);
                                    httpURLConnection.setDoOutput(true);
                                    httpURLConnection.setRequestMethod("PUT");
                                    httpURLConnection.connect();
                                    OutputStream outputStream = httpURLConnection.getOutputStream();
                                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                                    byte[] bytes = new byte[100];
                                    while (dataInputStream.read(bytes) != -1) {
                                        dataOutputStream.write(bytes);
                                    }
                                    dataOutputStream.write("\r\n".getBytes());
                                    dataOutputStream.flush();
                                    dataOutputStream.close();
                                    dataInputStream.close();
                                    Log.e("UPLOAD", httpURLConnection.getResponseCode() + "");
                                    httpURLConnection.disconnect();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
