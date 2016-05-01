package mw.ankara.uikit.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;

import com.squareup.picasso.Transformation;

/**
 * @author masawong
 * @since 16/5/1
 */
public class CircleImageView extends WebImageView {

    private int mRadius;

    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize(context);
    }

    private void initialize(Context context) {
        setTransformation(new CircleTransform());
    }

    public void setRadius(int radius) {
        mRadius = radius;
    }

    private class CircleTransform implements Transformation {

        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            // 取正中间
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }
            // end

            // 裁圆角
            Bitmap resultBitmap = Bitmap.createBitmap(size, size, squaredBitmap.getConfig());

            Canvas canvas = new Canvas(resultBitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            BitmapShader shader = new BitmapShader(squaredBitmap,
                BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);

            if (mRadius == 0) {
                float r = size / 2f;
                canvas.drawCircle(r, r, r, paint);
            } else {
                RectF rect = new RectF(0, 0, size, size);
                canvas.drawRoundRect(rect, mRadius, mRadius, paint);
            }

            squaredBitmap.recycle();
            //end

            return resultBitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }
}
