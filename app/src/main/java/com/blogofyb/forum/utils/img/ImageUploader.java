package com.blogofyb.forum.utils.img;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.blogofyb.forum.interfaces.HttpCallbackListener;
import com.blogofyb.forum.utils.constant.SQLite;
import com.blogofyb.forum.utils.database.MySQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;

public class ImageUploader {
    private static final String ADDRESS = "129.204.3.245";
    private static final int REMOTE_PORT = 23333;

    public static void uploadByPath(final Context context, final String path, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String account = null;
                String password = null;
                SQLiteDatabase database = MySQLiteOpenHelper.getDatabase(context);
                Cursor cursor = database.query(SQLite.TABLE_NAME, new String[]{SQLite.ACCOUNT, SQLite.PASSWORD},
                        null, null, null, null, null);
                while (cursor.moveToNext()) {
                    account = cursor.getString(cursor.getColumnIndex(SQLite.ACCOUNT));
                    password = cursor.getString(cursor.getColumnIndex(SQLite.PASSWORD));
                }
                cursor.close();
                Socket socket = null;
                OutputStream outputStream = null;
                FileInputStream fileInputStream = null;
                try {
                    socket = new Socket(ADDRESS, REMOTE_PORT);
                    outputStream = socket.getOutputStream();

                    /*
                     * 添加一个头：
                     * 账号：11字节
                     * 密码：18字节
                     * key：HEA（头像）|POS（帖子）  正好凑齐64字节
                     * MD5：32字节
                     */
                    String md5 = getMd5(path.split(":")[0]);
                    if (account != null && password != null) {
                        outputStream.write(account.getBytes());
                        byte[] passwordByte = new byte[18];
                        System.arraycopy(password.getBytes(), 0, passwordByte, 0, password.length());
                        outputStream.write(passwordByte);
                        outputStream.write(path.split(":")[1].getBytes());
                        outputStream.write(md5.getBytes());
                    }

                    fileInputStream = new FileInputStream(new File(path.split(":")[0]));
                    byte[] buffer = new byte[1024];
                    int length = -1;
                    while ((length = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.flush();
                    listener.onFinish(md5);  // 返回md5值
                } catch (Exception e) {
                    listener.onFailure(e);
                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            private String  getMd5(String path) {
                // 根据图片的路径和当前的系统时间生成一个md5值
                String key = path + System.currentTimeMillis();
                String md5 = null;
                try {
                    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                    messageDigest.update(key.getBytes());
                    md5 = bytesToHexString(messageDigest.digest());
                } catch (Exception e) {
                    e.printStackTrace();
                    return String.valueOf(key.hashCode());
                }
                return md5;
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
        }).start();
    }
}
