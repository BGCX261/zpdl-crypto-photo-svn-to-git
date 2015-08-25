package com.zpdl.api.drawable;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.zpdl.api.util.Alog;

public class AbitmapFactory {
    public static int Orientation(String filename) {
        int degree = 0;
        try {
            ExifInterface exif = new ExifInterface(filename);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                    break;
                }
            }
        } catch (IOException e) {
            Alog.w("ApiBitmapFactory.Orientation : "+e.getMessage());
        }
        return degree;
    }

    public enum ScaleType {
        CENTER_CROP;
    };

    public static Bitmap ScaledBitmap(Bitmap source, ScaleType scaleType) {
        if (ScaleType.CENTER_CROP == scaleType) {
            int size = source.getWidth() > source.getHeight() ?  source.getHeight() : source.getWidth();
            return scaledBitmapCenterCrop(source, size, size);
        }
        return null;
    }

    public static Bitmap ScaledBitmap(Bitmap source, ScaleType scaleType, int dwidth, int dheight) {
    	if (source == null) {
    		return null;
    	}
        if (ScaleType.CENTER_CROP == scaleType) {
            return scaledBitmapCenterCrop(source, dwidth, dheight);
        }
        return null;
    }

    private static Bitmap scaledBitmapCenterCrop(Bitmap source, int twidth, int theight) {
        float scale = 1f;
        int dx = 0, dy = 0;
        int swidth = source.getWidth();
        int sheight = source.getHeight();

        if (swidth * theight > twidth * sheight) {
            scale = (float) theight / (float) sheight;
            dx = (int) ((swidth - sheight) * 0.5f);
        } else {
            scale = (float) twidth / (float) swidth;
            dy = (int) ((sheight - swidth) * 0.5f);
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap scaledBitmap = Bitmap.createBitmap(source, dx, dy, swidth - 2*dx, sheight - 2*dy, matrix, false);

        return scaledBitmap;
    }
}
