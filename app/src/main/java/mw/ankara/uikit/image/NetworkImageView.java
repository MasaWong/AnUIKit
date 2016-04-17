package mw.ankara.uikit.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;

/**
 * @author masawong
 * @since 16/4/14
 */
public class NetworkImageView extends ImageView {

    public NetworkImageView(Context context) {
        super(context);
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NetworkImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setImageUrl(String url, int placeHolder, int error) {
        setImageUri(Uri.parse(url), placeHolder, error);
    }

    public void setImagePath(String path, int placeHolder, int error) {
        setImageUri(Uri.fromFile(new File(path)), placeHolder, error);
    }

    public void setImageUri(Uri uri, int placeHolder, int error) {
        RequestCreator creator = Picasso.with(getContext()).load(uri);
        if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
            creator.resize(getMeasuredWidth(), getMeasuredHeight()).centerCrop();
        }
        if (placeHolder > 0) {
            creator.placeholder(placeHolder);
        }
        if (error > 0) {
            creator.error(error);
        }
        creator.into(this);
    }
}
