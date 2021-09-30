package com.example.challyfilio.pmss.Util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class STGalleryUtil {
    public static String getFileRoot(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//判断sd卡是否存在是否有读写权限
            File external = context.getExternalFilesDir(null);
            if (external != null) {
                return external.getAbsolutePath();//获取绝对路径
            }
        }
        return context.getFilesDir().getAbsolutePath();
    }

    public static void saveImageToGallery(Context context, Bitmap bmp, String picName) {
        String fileName = null;
        //系统相册目录
        String galleryPath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;
        File file = null;//文件对象
        FileOutputStream outStream = null;//输出流
        try {
            // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
            file = new File(galleryPath, picName);
            // 获得文件相对路径
            fileName = file.toString();
            // 获得输出流，如果文件中有内容，追加内容
            outStream = new FileOutputStream(fileName);
            if (null != outStream) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            }
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MediaStore.Images.Media.insertImage(context.getContentResolver(), bmp, fileName, null);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);//扫描指定文件
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);//文件uri
        context.sendBroadcast(intent);
    }
}