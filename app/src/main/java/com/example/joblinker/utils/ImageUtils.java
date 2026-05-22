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
     * Load circular image — always fetches fresh copy, no disk cache
     * so profile photo updates are visible immediately after save.
     */
    public static void loadCircularImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        Glide.with(context)
                .load(imageUrl)
                .apply(new RequestOptions()
                        .circleCrop()
                        .placeholder(R.drawable.ic_person_placeholder)
                        .error(R.drawable.ic_person_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)   // never cache to disk
                        .skipMemoryCache(true))                        // never cache in memory
                .into(imageView);
    }

    /**
     * Load circular image with disk cache (use for conversation/job lists
     * where the image rarely changes and speed matters more).
     */
    public static void loadCircularImageCached(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        RequestOptions options = new RequestOptions()
                .circleCrop()
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(imageUrl)
                .apply(options)
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
        // Decode inside try-with-resources so the stream is always closed,
        // even if BitmapFactory.decodeStream() throws
        Bitmap original;
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            original = BitmapFactory.decodeStream(inputStream);
        }
        if (original == null) throw new IOException("Could not decode image");

        // Resize if too large
        int maxWidth = 1024, maxHeight = 1024;
        Bitmap bitmap;
        if (original.getWidth() > maxWidth || original.getHeight() > maxHeight) {
            float scale = Math.min(
                    (float) maxWidth  / original.getWidth(),
                    (float) maxHeight / original.getHeight());
            bitmap = Bitmap.createScaledBitmap(original,
                    Math.round(original.getWidth()  * scale),
                    Math.round(original.getHeight() * scale), true);
            original.recycle(); // ✅ free original after scaling
        } else {
            bitmap = original;
        }

        // Compress to JPEG
        File tempFile = new File(context.getCacheDir(),
                "compressed_" + System.currentTimeMillis() + ".jpg");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            fos.write(bos.toByteArray());
        } finally {
            if (bitmap != original) bitmap.recycle();
        }
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