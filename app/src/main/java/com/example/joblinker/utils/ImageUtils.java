package com.example.joblinker.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.joblinker.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    /**
     * Load image with Glide
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Load circular image
     */
    public static void loadCircularImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        RequestOptions options = new RequestOptions()
                .circleCrop()
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder);

        Glide.with(context)
                .load(imageUrl)
                .apply(options)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Load company logo
     */
    public static void loadCompanyLogo(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_company_placeholder)
                .error(R.drawable.ic_company_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Compress image
     */
    public static File compressImage(Context context, Uri imageUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        if (inputStream != null) {
            inputStream.close();
        }

        // Resize if too large
        int maxWidth = 1024;
        int maxHeight = 1024;

        if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
            float scale = Math.min(
                    (float) maxWidth / bitmap.getWidth(),
                    (float) maxHeight / bitmap.getHeight()
            );

            int newWidth = Math.round(bitmap.getWidth() * scale);
            int newHeight = Math.round(bitmap.getHeight() * scale);

            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }

        // Compress to JPEG
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

        // Save to file
        File tempFile = new File(context.getCacheDir(), "compressed_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(outputStream.toByteArray());
        fos.close();
        outputStream.close();

        return tempFile;
    }

    /**
     * Get file from URI
     */
    public static File getFileFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File tempFile = new File(context.getCacheDir(), "temp_" + System.currentTimeMillis());

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int length;

        if (inputStream != null) {
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
        }

        outputStream.close();
        return tempFile;
    }
}