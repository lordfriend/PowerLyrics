package io.nya.powerlyrics.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;

import io.nya.powerlyrics.lyric.Lyric;
import io.nya.powerlyrics.lyric.LyricEntry;

/**
 * Created by nene on 3/4/17.
 */

public class LyricView extends View {

    private static final int INVALID_POSITION = -1;

    private TextPaint mDefaultPaint;
    private TextPaint mHighlighPaint;

    private int mCurrentPosition = INVALID_POSITION;

    private int mMiddleY = 0;

    Lyric mLyric;

    ArrayList<StaticLayout> mLayoutList = new ArrayList<>();

    /**
     * the duration of current music
     */
    private long mDuration = 0;

    private SmoothScrollRunnable mSmoothScrollbarEnabled;

    public LyricView(Context context) {
        super(context);
    }

    public LyricView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        mDefaultPaint = new TextPaint();
        mDefaultPaint.setColor(Color.LTGRAY);

        mHighlighPaint = new TextPaint();
        mDefaultPaint.setColor(Color.WHITE);
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
        if (mDuration == 0) {
            throw new RuntimeException("Must set a duration before update current time");
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
            endPos = mCurrentPosition;
            direction = "up";
        }
        int delta = 0;
        for (int i = startPos; i <= endPos; i++) {
            StaticLayout layout = mLayoutList.get(i);
            delta += layout.getHeight();
        }
        String lyricText = mLyric.get(position).lyric;
        int width = getMeasuredWidth();
        mLayoutList.set(position, new StaticLayout(lyricText, mHighlighPaint, width, StaticLayout.Alignment.ALIGN_CENTER, 1f, 1.5f, true));
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
        int top = mMiddleY - getScrollY();
        for(StaticLayout layout: mLayoutList) {
            int childLeft = getPaddingLeft();
            canvas.save();
            canvas.translate(childLeft, top);
            layout.draw(canvas);
            canvas.restore();
            top = top + layout.getHeight();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int containerWidth = MeasureSpec.getSize(widthMeasureSpec);
        mMiddleY = MeasureSpec.getSize(heightMeasureSpec) / 2;
        if (mLyric != null) {
            for (LyricEntry entry: mLyric) {
                if (mCurrentPosition != INVALID_POSITION) {
                    mLayoutList.add(new StaticLayout(entry.lyric, mDefaultPaint, containerWidth, StaticLayout.Alignment.ALIGN_CENTER, 1f, 1.5f, true));
                } else {
                    mLayoutList.add(new StaticLayout(entry.lyric, mHighlighPaint, containerWidth, StaticLayout.Alignment.ALIGN_CENTER, 1f, 1.5f, true));
                }
            }
        }
    }

    private class SmoothScrollRunnable implements Runnable {

        private Scroller mScroller;

        public SmoothScrollRunnable() {
            mScroller = new Scroller(getContext());
        }

        public void startScroll(int distance) {
            mScroller.abortAnimation();
            mScroller.startScroll(0, 0, 0, distance);
            postOnAnimation(this);
        }

        @Override
        public void run() {
            if(mScroller.computeScrollOffset()) {
                setScrollY(mScroller.getCurrY());
            }
        }
    }
}
