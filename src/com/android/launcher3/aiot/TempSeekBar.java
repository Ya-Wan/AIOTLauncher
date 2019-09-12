package com.android.launcher3.aiot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.SeekBar;


public class TempSeekBar extends SeekBar {

    private Paint mTextPaint;

    private Rect mProgressTextRect = new Rect();

    private int mThumbWidth = dp2px(50);
    // 进度指示器宽度
    private int mIndicatorWidth = dp2px(50);

    private OnIndicatorSeekBarChangeListener mIndicatorSeekBarChangeListener;

    public TempSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(sp2px(16));

        // 如果不设置padding，当滑动到最左边或最右边时，滑块会显示不全
        setPadding(mThumbWidth / 2, 0, mThumbWidth / 2, 0);

        // 设置滑动监听
        this.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // NO OP
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mIndicatorSeekBarChangeListener != null) {
                    mIndicatorSeekBarChangeListener.onStartTrackingTouch(seekBar);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mIndicatorSeekBarChangeListener != null) {
                    mIndicatorSeekBarChangeListener.onStopTrackingTouch(seekBar);
                }
            }
        });
    }


    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String progressText = getProgress() + "°C";
        mTextPaint.getTextBounds(progressText, 0, progressText.length(), mProgressTextRect);

        float progressRatio = (float) (getProgress() - getMin()) / (getMax() - getMin());
        float thumbOffset = ((mThumbWidth - mProgressTextRect.width()) >> 1) - mThumbWidth * progressRatio;
        float thumbX = getWidth() * progressRatio + thumbOffset;
        float thumbY = getHeight() / 2f + mProgressTextRect.height() / 2f;
        canvas.drawText(progressText, thumbX, thumbY, mTextPaint);

        if (mIndicatorSeekBarChangeListener != null) {
            float indicatorOffset = getWidth() * progressRatio - ((mIndicatorWidth - mThumbWidth) >> 1) - mThumbWidth * progressRatio;
            mIndicatorSeekBarChangeListener.onProgressChanged(this, getProgress(), indicatorOffset);
        }
    }

    /**
     * 设置进度监听
     *
     * @param listener OnIndicatorSeekBarChangeListener
     */
    public void setOnSeekBarChangeListener(OnIndicatorSeekBarChangeListener listener) {
        this.mIndicatorSeekBarChangeListener = listener;
    }

    /**
     * 进度监听
     */
    public interface OnIndicatorSeekBarChangeListener {
        /**
         * 进度监听回调
         *
         * @param seekBar         SeekBar
         * @param progress        进度
         * @param indicatorOffset 指示器偏移量
         */
        public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset);

        /**
         * 开始拖动
         *
         * @param seekBar SeekBar
         */
        public void onStartTrackingTouch(SeekBar seekBar);

        /**
         * 停止拖动
         *
         * @param seekBar SeekBar
         */
        public void onStopTrackingTouch(SeekBar seekBar);
    }

    public int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @param sp sp值
     * @return px值
     */
    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }
}
