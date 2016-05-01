package mw.ankara.uikit.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.File;

/**
 * @author masawong
 * @since 16/4/14
 */
public class WebImageView extends ImageView {

    private Transformation mTransformation;

    public WebImageView(Context context) {
        super(context);
    }

    public WebImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setTransformation(Transformation transformation) {
        mTransformation = transformation;
    }

    // web image
    public void setImageUrl(String url) {
        setImageUrl(url, 0, 0, 0, 0);
    }

    public void setImageUrlWithHolder(String url, int placeHolder, int error) {
        setImageUrl(url, placeHolder, error, 0, 0);
    }

    public void setImageUrlWithSize(String url, int width, int height) {
        setImageUrl(url, 0, 0, width, height);
    }

    public void setImageUrl(String url, int placeHolder, int error, int width, int height) {
        setImageUri(Uri.parse(url), placeHolder, error, width, height);
    }

    // local image
    public void setImagePath(String path) {
        setImagePath(path, 0, 0, 0, 0);
    }

    public void setImagePathWithHolder(String path, int placeHolder, int error) {
        setImagePath(path, placeHolder, error, 0, 0);
    }

    public void setImagePathWithSize(String path, int width, int height) {
        setImagePath(path, 0, 0, width, height);
    }

    public void setImagePath(String path, int placeHolder, int error, int width, int height) {
        setImageUri(Uri.fromFile(new File(path)), placeHolder, error, width, height);
    }

    // uri
    public void setImageUri(Uri uri) {
        setImageUri(uri, 0, 0, 0, 0);
    }

    public void setImageUriWithHolder(Uri uri, int placeHolder, int error) {
        setImageUri(uri, placeHolder, error, 0, 0);
    }

    public void setImageUriWithSize(Uri uri, int width, int height) {
        setImageUri(uri, 0, 0, width, height);
    }

    public void setImageUri(Uri uri, int placeHolder, int error, int width, int height) {
        RequestCreator creator = Picasso.with(getContext()).load(uri);

        // size
        if (width <= 0) {
            width = getMeasuredWidth();
        }
        if (height <= 0) {
            height = getMeasuredHeight();
        }
        if (width > 0 && height > 0) {
            creator.resize(width, height).centerCrop();
        }

        // place holder
        if (placeHolder > 0) {
            creator.placeholder(placeHolder);
        }

        // error holder
        if (error > 0) {
            creator.error(error);
        }

        // transformation
        if (mTransformation != null) {
            creator.transform(mTransformation);
        }

        // execute
        creator.into(this);
    }
}
