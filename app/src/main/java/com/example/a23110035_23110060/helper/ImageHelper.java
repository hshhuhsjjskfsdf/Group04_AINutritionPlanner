package com.example.a23110035_23110060.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class ImageHelper {
    private ImageHelper() {
    }

    public static String copyUriToCache(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            File file = new File(context.getCacheDir(), "meal_" + UUID.randomUUID() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    public static String saveBitmapToCache(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        try {
            File file = new File(context.getCacheDir(), "camera_" + UUID.randomUUID() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
}
