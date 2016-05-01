package mw.ankara.uikit.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

/**
 * @author masawong
 * @since 4/14/16
 */
public class SquareImageView extends WebImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getSizeFromMeasureSpec(widthMeasureSpec);
        int height = getSizeFromMeasureSpec(heightMeasureSpec);
        int sizeMeasureSpec = MeasureSpec.makeMeasureSpec(width > height ? height : width,
            MeasureSpec.EXACTLY);
        super.onMeasure(sizeMeasureSpec, sizeMeasureSpec);
    }

    private int getSizeFromMeasureSpec(int measureSpec) {
        return MeasureSpec.getMode(measureSpec) == MeasureSpec.UNSPECIFIED ?
            Integer.MAX_VALUE : MeasureSpec.getSize(measureSpec);
    }
}
