package io.nya.powerlyrics.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;

import io.nya.powerlyrics.R;
import io.nya.powerlyrics.lyric.Lyric;
import io.nya.powerlyrics.lyric.LyricEntry;

/**
 * A lyric view to show lyric with auto scroll and highlight current playing lyric text
 */

public class LyricView extends View {

    private static final String TAG = LyricView.class.getName();

    private static final int INVALID_POSITION = -1;

    private static final int SMOOTH_SCROLL_DURATION = 400;

    private TextPaint mDefaultPaint;
    private TextPaint mHighlighPaint;

    private int mCurrentPosition = INVALID_POSITION;

    private int mMiddleY = 0;
    private boolean hasInit = false;

    private float mItemMargin;

    Lyric mLyric;

    ArrayList<ArrayList<StaticLayout>> mLayoutList;
    ArrayList<ArrayList<StaticLayout>> mHighlightLayoutList;

    /**
     * the duration of current music
     */
    private long mDuration = 0;

    private SmoothScrollRunnable mSmoothScrollbarEnabled;

    public LyricView(Context context) {
        super(context);
        init(context, null);
    }

    public LyricView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDefaultPaint = new TextPaint();
        mHighlighPaint = new TextPaint();

        int defaultTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, context.getResources().getDisplayMetrics());
        mItemMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LyricView, 0, 0);
            try {
                mDefaultPaint.setColor(a.getColor(R.styleable.LyricView_textColor, Color.LTGRAY));
                mHighlighPaint.setColor(a.getColor(R.styleable.LyricView_textHighlightColor, Color.WHITE));
                mDefaultPaint.setTextSize(a.getDimensionPixelSize(R.styleable.LyricView_textSize, defaultTextSize));
                mHighlighPaint.setTextSize(a.getDimensionPixelSize(R.styleable.LyricView_textSize, defaultTextSize));
            } finally {
                a.recycle();
            }
        } else {
            mDefaultPaint.setColor(Color.LTGRAY);
            mDefaultPaint.setColor(Color.WHITE);
            mDefaultPaint.setTextSize(defaultTextSize);
        }
    }

    public void setDuration(long duration) {
        if (mLyric == null) {
            throw new RuntimeException("Lyric must be set before set duration");
        }
        mDuration = duration;
        LyricEntry lastEntry = mLyric.get(mLyric.size() - 1);
        lastEntry.duration = duration - lastEntry.timestamp;
    }

    public void setLyric(Lyric lyric) {
        mLyric = lyric;
        requestLayout();
    }

    /**
     * Update the current play time in milliseconds, this is usually called by the player
     * @param currentTime the current play time in milliseconds
     */
    public void updateCurrentTime(long currentTime) {
        if (mDuration == 0 || !hasInit) {
            // do nothing
            return;
        }
        final int currentPosition = mCurrentPosition;
        final Lyric lyric = mLyric;
        if (currentPosition != INVALID_POSITION && currentPosition < lyric.size() - 1) {
            LyricEntry currentEntry = lyric.get(currentPosition);
            if (currentEntry.timestamp <= currentTime && currentEntry.duration > (currentTime - currentEntry.timestamp)) {
                // position not changed
                return;
            } else if(currentTime > currentEntry.timestamp + currentEntry.duration) {
                // current time is within the next entry range.
                LyricEntry nextEntry = lyric.get(currentPosition + 1);
                if (nextEntry.timestamp <= currentTime && currentTime < nextEntry.timestamp + nextEntry.duration) {
                    scrollToPosition(currentPosition + 1);
                    return;
                }
            }
        }
        // perform a search from beginning
        for (int i = 0; i < lyric.size(); i++) {
            LyricEntry entry = lyric.get(i);
            if (entry.timestamp <= currentTime && currentTime < entry.timestamp + entry.duration) {
                scrollToPosition(i);
                return;
            }
        }
    }

    public void scrollToPosition(int position) {
        if (mCurrentPosition == position) {
            return;
        }
        int currentPosition = mCurrentPosition == INVALID_POSITION ? 0: mCurrentPosition;
        int startPos, endPos;
        String direction;
        if (position > currentPosition) {
            startPos = currentPosition;
            endPos = position;
            direction = "down";
        } else {
            startPos = position;
            endPos = currentPosition;
            direction = "up";
        }
        int delta = 0;
        for (int i = startPos; i < endPos; i++) {
            for (StaticLayout layout : mLayoutList.get(i)) {
                delta += layout.getHeight();
            }
            delta += mItemMargin;
        }

        mCurrentPosition = position;
        if (mSmoothScrollbarEnabled == null) {
            mSmoothScrollbarEnabled = new SmoothScrollRunnable();
        }
        if (direction.equals("up")) {
            mSmoothScrollbarEnabled.startScroll(-delta);
        } else {
            mSmoothScrollbarEnabled.startScroll(delta);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int top = mMiddleY;
        int childLeft = getPaddingLeft();
        if (mLyric != null) {
            for(int i = 0; i < mLyric.size(); i++) {
                if (i == mCurrentPosition) {
                    for (StaticLayout layout: mHighlightLayoutList.get(i)) {
                        canvas.save();
                        canvas.translate(childLeft, top);
                        layout.draw(canvas);
                        canvas.restore();
                        top += layout.getHeight();
                    }
                } else {
                    for (StaticLayout layout: mLayoutList.get(i)) {
                        canvas.save();
                        canvas.translate(childLeft, top);
                        layout.draw(canvas);
                        canvas.restore();
                        top += layout.getHeight();
                    }
                }
                top += mItemMargin;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int containerWidth = MeasureSpec.getSize(widthMeasureSpec);
        mMiddleY = MeasureSpec.getSize(heightMeasureSpec) / 2;
        mCurrentPosition = INVALID_POSITION;
        setScrollY(0);
        if (mLyric != null) {
            mLayoutList = new ArrayList<>();
            mHighlightLayoutList = new ArrayList<>();
            for (LyricEntry entry: mLyric) {
                ArrayList<StaticLayout> innerList = new ArrayList<>();
                ArrayList<StaticLayout> innerHighlightList = new ArrayList<>();
                if (entry.lyric != null) {
                    innerList.add(new StaticLayout(entry.lyric, mDefaultPaint, containerWidth, StaticLayout.Alignment.ALIGN_CENTER, 1f, 1.5f, true));
                    innerHighlightList.add(new StaticLayout(entry.lyric, mHighlighPaint, containerWidth, StaticLayout.Alignment.ALIGN_CENTER, 1f, 1.5f, true));
                }
                if (entry.tLyric != null) {
                    innerList.add(new StaticLayout(entry.tLyric, mDefaultPaint, containerWidth, StaticLayout.Alignment.ALIGN_CENTER, 1f, 1.5f, true));
                    innerHighlightList.add(new StaticLayout(entry.tLyric, mHighlighPaint, containerWidth, StaticLayout.Alignment.ALIGN_CENTER, 1f, 1.5f, true));
                }
                mLayoutList.add(innerList);
                mHighlightLayoutList.add(innerHighlightList);
            }
            hasInit = true;
        }
    }

    private class SmoothScrollRunnable implements Runnable {

        private Scroller mScroller;

        SmoothScrollRunnable() {
            mScroller = new Scroller(getContext());
        }

        void startScroll(int distance) {
            mScroller.abortAnimation();
            mScroller.startScroll(0, getScrollY(), 0, distance, SMOOTH_SCROLL_DURATION);
            postOnAnimation(this);
        }

        @Override
        public void run() {
            if(mScroller.computeScrollOffset()) {
                setScrollY(mScroller.getCurrY());
                postOnAnimation(this);
            }
        }
    }
}
