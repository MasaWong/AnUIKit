package mw.ankara.uikit.refresh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;

/**
 * @author masawong
 * @since 16/3/15
 */
public class SwipeRefreshLayout extends ViewGroup {

    private static final int INVALID_POINTER = -1;
    private static final float SCROLL_RATE = .5f;

    private final int mTouchSlop;
    private final float mTotalDragDistance;

    private boolean mIsBeingDragging;
    private boolean mReturningToStart;
    private boolean mRefreshing;

    private int mActivePointerId = INVALID_POINTER;

    private float mInitialY;
    private float mCurrentY;

    private OnRefreshCallback mHeaderRefreshCallback;

    public SwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTotalDragDistance = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            64f, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() != 2) {
            throw new IllegalStateException("SwipeRefreshLayout can only host two child");
        }

        int maxWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int maxHeight = getMeasuredHeight();

        View header = getChildAt(0);
        if (header instanceof OnRefreshCallback) {
            mHeaderRefreshCallback = (OnRefreshCallback) header;
        }
        header.measure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));

        getChildAt(1).measure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = l + getPaddingLeft();
        int right = r - getPaddingRight();

        View header = getChildAt(0);
        header.layout(left, t - header.getMeasuredHeight(), right, t);

        View content = getChildAt(1);
        content.layout(left, t, right, b);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragging = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialY = mCurrentY = initialDownY;
                break;

            case MotionEvent.ACTION_MOVE:
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialY;
                if (yDiff > mTouchSlop && !mIsBeingDragging) {
                    mIsBeingDragging = true;
                }
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragging = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragging;
    }

    @SuppressLint("WrongCall")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp()) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragging = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float distanceY = (y - mInitialY) * SCROLL_RATE;
                if (mIsBeingDragging) {
                    if (mHeaderRefreshCallback != null) {
                        mHeaderRefreshCallback.onSwipeDiff(y - mCurrentY);
                    }
                    onLayout(false, getLeft(), (int) (getTop() + distanceY),
                        getRight(), (int) (getBottom() + distanceY));
                }
                mCurrentY = y;
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float distanceY = (y - mInitialY) * SCROLL_RATE;
                mIsBeingDragging = false;
                if (distanceY > mTotalDragDistance) {
                    mRefreshing = true;
                    if (mHeaderRefreshCallback != null) {
                        mHeaderRefreshCallback.onRefresh();
                    }
                    scrollToCoordinate(mTotalDragDistance);
                } else {
                    mReturningToStart = true;
                    scrollToCoordinate(0);
                }
                mActivePointerId = INVALID_POINTER;
            }
        }
        return true;
    }

    // TODO: 16/3/15 use Scroller
    @SuppressLint("WrongCall")
    private void scrollToCoordinate(final float y) {
        TranslateAnimation animation = new TranslateAnimation(
            Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f,
            Animation.ABSOLUTE, getChildAt(1).getTop(), Animation.ABSOLUTE, y);
        animation.setDuration(300l);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (y == 0) {
                    mReturningToStart = false;
                    mRefreshing = false;
                } else {
                    onRefresh();
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        getChildAt(0).startAnimation(animation);
        getChildAt(1).startAnimation(animation);

        onLayout(false, getLeft(), (int) (getTop() + y), getRight(), (int) (getBottom() + y));
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    public boolean canChildScrollUp() {
        View content = getChildAt(1);

        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (content instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) content;
                return absListView.getChildCount() > 0
                    && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                    .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(content, -1) || content.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(content, -1);
        }
    }

    public void onRefresh() {

    }

    public void refreshOver() {
        if (mHeaderRefreshCallback != null) {
            mHeaderRefreshCallback.onRefreshOver();
        }
        scrollToCoordinate(0);
    }

    public interface OnRefreshCallback {
        void onSwipeDiff(float diffY);

        void onRefresh();

        void onRefreshOver();
    }
}
