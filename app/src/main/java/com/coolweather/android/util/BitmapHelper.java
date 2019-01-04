package com.coolweather.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BitmapHelper {
    /**  压缩图片大小 */
    private Bitmap comp(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {
            //判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            //这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*500分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 500f;//这里设置宽度为500f

        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可，scaling=1 表示不缩放
        int scaling = 1;
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            scaling = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            scaling = (int) (newOpts.outHeight / hh);
        }
        if (scaling <= 0) {
            scaling = 1;
        }
        newOpts.inSampleSize = scaling;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;//压缩好比例大小后再进行质量压缩
    }

    /**
     * 调整图片分辨率
     */
    public static Bitmap resizePicture(Bitmap bitmap ,int newWidth, int newHeight) {
        if (bitmap == null) {
            return null;
        }
        if (newWidth <= 0 || newHeight <= 0) {
            return bitmap;
        }

        int picWidth = bitmap.getWidth();
        int picHeight = bitmap.getHeight();

        float scaleWidth = (float) newWidth / picWidth;
        float scaleHeight = (float) newHeight / picHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);

        return Bitmap.createBitmap(bitmap, 0, 0, picWidth, picHeight, matrix, true);
    }


}
