package io.nya.powerlyrics.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import io.nya.powerlyrics.lyric.Lyric;
import io.nya.powerlyrics.lyric.LyricEntry;

/**
 * Created by nene on 2/25/17.
 */

public class LyricView extends ListView implements AbsListView.OnScrollListener {

    private final static String LOG_TAG = LyricView.class.getName();

    public int mMiddleX = 0;

    private float friction = 0.15f;

    private int mPendingPlayPosition;
    private boolean isSeeking = false;
    private long mSeekingPlayTime = 0;
    private Paint mIndicatorPaint = null;
    private long mDuration = 0;

    private Drawable mDividerDrawable;

    public LyricView(Context context) {
        super(context);
        init(context);
    }

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
//        mMiddleX = getHeight() / 2;
//        ViewConfiguration config = ViewConfiguration.get(context);
//        float friction = config.getScrollFriction();
//        Log.d(LOG_TAG, "friction: " + friction);
        setFriction(friction);
        setClipToPadding(false);
        setOnScrollListener(this);
        mDividerDrawable = new ColorDrawable(Color.TRANSPARENT);
//        setDivider(mDividerDrawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMiddleX == 0) {
            mMiddleX = getMeasuredHeight() / 2;
            float fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            Log.d(LOG_TAG, "mMiddleX: " + mMiddleX);
            Log.d(LOG_TAG, "font size: " + fontSize);
            setPadding(0, mMiddleX, 0, mMiddleX + (int)fontSize);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(LOG_TAG,"measuredHeight: "  + getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(isSeeking) {
            if(mIndicatorPaint == null) {
                mIndicatorPaint = new Paint();
                mIndicatorPaint.setColor(Color.WHITE);
                mIndicatorPaint.setTextSize(24);
            }
            canvas.drawText("Current Position: " + mPendingPlayPosition, 0, mMiddleX, mIndicatorPaint);
        }
    }

    /**
     * Smoothly scroll to the specified adapter position, The view will scroll such that the
     * indicated position is displayed in the center of this view.
     * @param position Position to Scroll
     */
    public void smoothScrollToPositionMiddle(int position) {
        smoothScrollToPositionFromTop(position, mMiddleX);
    }

    /**
     * get the play time of current adapter position + offset
     * @param position
     */
    private void getPlayTime(int position, float offsetOfChildHeightPercent) {
        ListAdapter adapter = getAdapter();
        LyricEntry item = (LyricEntry) adapter.getItem(position);
        int total = adapter.getCount();
        LyricEntry nextItem;
        long playTimeDiff = 0;
        if (position < total - 1) {
            nextItem = (LyricEntry) adapter.getItem(position + 1);
            playTimeDiff = nextItem.timestamp -  item.timestamp;

        } else {
            playTimeDiff = mDuration - item.timestamp;
        }
        mSeekingPlayTime = (long)((float) playTimeDiff * offsetOfChildHeightPercent);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d(LOG_TAG, "ScrollState: " + new String[]{"idle", "touch_scroll", "fling"}[scrollState]);
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            isSeeking = false;
        } else if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            isSeeking = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d(LOG_TAG, "onScroll");
        int position = 0;
        int offset = 0;
        View currentChild = null;
        for (int i = 0; i < visibleItemCount; i++) {
            View child = getChildAt(i);
            offset = Math.abs(child.getTop() - mMiddleX);
            Log.d(LOG_TAG, "child offset: " + offset);

            if (offset <= child.getMeasuredHeight()) {
                position = i + firstVisibleItem;
                currentChild = child;
                break;
            }
        }
        if (currentChild != null) {
            mPendingPlayPosition = position;
//            getPlayTime(position, (float) offset / (float) currentChild.getMeasuredHeight());
        }
    }

    public void setPlayDuration(long durationInMillis) {
        mDuration = durationInMillis;
    }
}
