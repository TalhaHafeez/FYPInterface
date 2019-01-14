package com.example.rabiaqayyum.fypinterface.emotionDetection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;

public class CropFace
{
    public static Bitmap cropBitmap(Bitmap bitmap, Rect rect) {
        int w = rect.right - rect.left;
        int h = rect.bottom - rect.top;
        Bitmap ret = Bitmap.createBitmap(w, h, bitmap.getConfig());
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(bitmap, -rect.left, -rect.top, null);
        bitmap.recycle();
        return ret;
    }


    public final static Bitmap rotate(Bitmap b, float degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2,
                    (float) b.getHeight() / 2);

            Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                    b.getHeight(), m, true);
            if (b != b2) {
                b.recycle();
                b = b2;
            }

        }
        return b;
    }
    public static Bitmap cropFace(FaceResult face, Bitmap bitmap, int rotate) {
        Bitmap bmp;

        float eyesDis = face.eyesDistance();
        PointF mid = new PointF();
        face.getMidPoint(mid);

        Rect rect = new Rect(
                (int) (mid.x - eyesDis * 1.20f),
                (int) (mid.y - eyesDis * 0.55f),
                (int) (mid.x + eyesDis * 1.20f),
                (int) (mid.y + eyesDis * 1.85f));

        Bitmap.Config config = Bitmap.Config.RGB_565;
        if (bitmap.getConfig() != null) config = bitmap.getConfig();
        bmp = bitmap.copy(config, true);

        switch (rotate) {
            case 90:
                bmp = rotate(bmp, 90);
                break;
            case 180:
                bmp = rotate(bmp, 180);
                break;
            case 270:
                bmp = rotate(bmp, 270);
                break;
        }

        bmp = cropBitmap(bmp, rect);
        return bmp;
    }
}
