package com.android.launcher3.aiot;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.launcher3.R;

public class RefrigeratorControlPopupWindow {

    private Context mContext;
    private int mWidth;
    private int mHeight;
    private boolean mIsFocusable = true;
    private boolean mIsOutside = true;
    private int mResLayoutId = -1;
    private View mContentView;
    private PopupWindow mPopupWindow;
    private int mAnimationStyle = -1;

    private boolean mClippEnable = true;//default is true
    private boolean mIgnoreCheekPress = false;
    private int mInputMode = -1;
    private PopupWindow.OnDismissListener mOnDismissListener;
    private int mSoftInputMode = -1;
    private boolean mTouchable = true;//default is ture
    private View.OnTouchListener mOnTouchListener;

    private TextView modeTeaTv, modeWineTv, modeBeerTv, minProgressTv, maxProgressTv;
    private LinearLayout modeTeaContainer, modeWineContainer, modeBeerContainer;
    private TempSeekBar seekBar;

    private TextView[] modes;
    private LinearLayout[] containers;
    private Drawable[] drawables;

    private View.OnClickListener listener = v -> {
        if (v.getId() == R.id.mode_tea_container) {
            setMode(0);
        } else if (v.getId() == R.id.mode_wine_container) {
            setMode(1);
        } else if (v.getId() == R.id.mode_beer_container) {
            setMode(2);
        }
    };

    private RefrigeratorControlPopupWindow(Context context) {
        mContext = context;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }


    public RefrigeratorControlPopupWindow showAsDropDown(View anchor, int xOff, int yOff) {
        if (mPopupWindow != null) {
            mPopupWindow.showAsDropDown(anchor, xOff, yOff);
        }
        return this;
    }

    public RefrigeratorControlPopupWindow showAsDropDown(View anchor) {
        if (mPopupWindow != null) {
            mPopupWindow.showAsDropDown(anchor);
        }
        return this;
    }

    public RefrigeratorControlPopupWindow showAtLocation(View parent, int gravity, int x, int y) {
        if (mPopupWindow != null) {
            mPopupWindow.showAtLocation(parent, gravity, x, y);
        }
        return this;
    }

    private PopupWindow build() {

        if (mContentView == null) {
            mContentView = LayoutInflater.from(mContext).inflate(mResLayoutId, null);
        }

        initPopupWindowView(mContentView);

        if (mWidth != 0 && mHeight != 0) {
            mPopupWindow = new PopupWindow(mContentView, mWidth, mHeight);
        } else {
            mPopupWindow = new PopupWindow(mContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        if (mAnimationStyle != -1) {
            mPopupWindow.setAnimationStyle(mAnimationStyle);
        }

        apply(mPopupWindow);//设置一些属性

        mPopupWindow.setFocusable(mIsFocusable);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setOutsideTouchable(mIsOutside);

        if (mWidth == 0 || mHeight == 0) {
            mPopupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            //如果外面没有设置宽高的情况下，计算宽高并赋值
            mWidth = mPopupWindow.getContentView().getMeasuredWidth();
            mHeight = mPopupWindow.getContentView().getMeasuredHeight();
        }

        mPopupWindow.update();

        return mPopupWindow;
    }

    public PopupWindow getPopupWindow() {
        return mPopupWindow;
    }

    private void initPopupWindowView(View view) {
        modeTeaContainer = view.findViewById(R.id.mode_tea_container);
        modeWineContainer = view.findViewById(R.id.mode_wine_container);
        modeBeerContainer = view.findViewById(R.id.mode_beer_container);

        minProgressTv = view.findViewById(R.id.min_progress_tv);
        maxProgressTv = view.findViewById(R.id.max_progress_tv);

        seekBar = view.findViewById(R.id.seek_bar);

        modeTeaTv = view.findViewById(R.id.mode_tea_tv);
        modeWineTv = view.findViewById(R.id.mode_wine_tv);
        modeBeerTv = view.findViewById(R.id.mode_beer_tv);

        modeTeaContainer.setOnClickListener(listener);
        modeWineContainer.setOnClickListener(listener);
        modeBeerContainer.setOnClickListener(listener);

        modes = new TextView[]{
                modeTeaTv,
                modeWineTv,
                modeBeerTv
        };

        containers = new LinearLayout[]{
                modeTeaContainer,
                modeWineContainer,
                modeBeerContainer
        };

        drawables = new Drawable[]{
                mContext.getDrawable(R.drawable.ic_mode_tea),
                mContext.getDrawable(R.drawable.ic_mode_wine),
                mContext.getDrawable(R.drawable.ic_mode_beer),
        };

        setMode(0);
    }

    public void setMode(int i) {
        for (int j = 0; j < modes.length; j++) {
            TextView mode = modes[j];
            LinearLayout container = containers[j];
            Drawable drawable = drawables[j];
            if (j == i) {
                mode.setTextColor(Color.WHITE);
                container.setBackgroundResource(R.drawable.refrigerator_mode_selected);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mode.setCompoundDrawablePadding(20);
                mode.setCompoundDrawables(drawable, null, null, null);

            } else {
                mode.setTextColor(mContext.getColor(R.color.refrigerator_mode_primary_color));
                container.setBackgroundResource(R.drawable.refrigerator_mode_normal);
                mode.setCompoundDrawables(null, null, null, null);
            }
        }

        switch (i) {
            case 0:
                seekBar.setMax(10);
                seekBar.setMin(2);
                seekBar.setProgress(3);
                minProgressTv.setText(2 + "°C");
                maxProgressTv.setText(10 + "°C");
                break;

            case 1:
                seekBar.setMax(5);
                seekBar.setMin(0);
                seekBar.setProgress(3);
                minProgressTv.setText(0 + "°C");
                maxProgressTv.setText(5 + "°C");
                break;

            case 2:
                seekBar.setMax(7);
                seekBar.setMin(-2);
                seekBar.setProgress(0);
                minProgressTv.setText(-2 + "°C");
                maxProgressTv.setText(7 + "°C");
                break;

            default:
                break;
        }
    }


    private void apply(PopupWindow popupWindow) {
        popupWindow.setClippingEnabled(mClippEnable);
        if (mIgnoreCheekPress) {
            popupWindow.setIgnoreCheekPress();
        }
        if (mInputMode != -1) {
            popupWindow.setInputMethodMode(mInputMode);
        }
        if (mSoftInputMode != -1) {
            popupWindow.setSoftInputMode(mSoftInputMode);
        }
        if (mOnDismissListener != null) {
            popupWindow.setOnDismissListener(mOnDismissListener);
        }
        if (mOnTouchListener != null) {
            popupWindow.setTouchInterceptor(mOnTouchListener);
        }
        popupWindow.setTouchable(mTouchable);

    }

    public void dismiss() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    public static class PopupWindowBuilder {
        private RefrigeratorControlPopupWindow ePopPopupWindow;

        public PopupWindowBuilder(Context context) {
            ePopPopupWindow = new RefrigeratorControlPopupWindow(context);
        }

        public PopupWindowBuilder size(int width, int height) {
            ePopPopupWindow.mWidth = width;
            ePopPopupWindow.mHeight = height;
            return this;
        }


        public PopupWindowBuilder setFocusable(boolean focusable) {
            ePopPopupWindow.mIsFocusable = focusable;
            return this;
        }

        public PopupWindowBuilder setView(int resLayoutId) {
            ePopPopupWindow.mResLayoutId = resLayoutId;
            ePopPopupWindow.mContentView = null;
            return this;
        }

        public PopupWindowBuilder setView(View view) {
            ePopPopupWindow.mContentView = view;
            ePopPopupWindow.mResLayoutId = -1;
            return this;
        }

        public PopupWindowBuilder setOutsideTouchable(boolean outsideTouchable) {
            ePopPopupWindow.mIsOutside = outsideTouchable;
            return this;
        }

        public PopupWindowBuilder setAnimationStyle(int animationStyle) {
            ePopPopupWindow.mAnimationStyle = animationStyle;
            return this;
        }


        public PopupWindowBuilder setClippingEnable(boolean enable) {
            ePopPopupWindow.mClippEnable = enable;
            return this;
        }


        public PopupWindowBuilder setIgnoreCheekPress(boolean ignoreCheekPress) {
            ePopPopupWindow.mIgnoreCheekPress = ignoreCheekPress;
            return this;
        }

        public PopupWindowBuilder setInputMethodMode(int mode) {
            ePopPopupWindow.mInputMode = mode;
            return this;
        }

        public PopupWindowBuilder setOnDissmissListener(PopupWindow.OnDismissListener onDissmissListener) {
            ePopPopupWindow.mOnDismissListener = onDissmissListener;
            return this;
        }


        public PopupWindowBuilder setSoftInputMode(int softInputMode) {
            ePopPopupWindow.mSoftInputMode = softInputMode;
            return this;
        }


        public PopupWindowBuilder setTouchable(boolean touchable) {
            ePopPopupWindow.mTouchable = touchable;
            return this;
        }

        public PopupWindowBuilder setTouchIntercepter(View.OnTouchListener touchIntercepter) {
            ePopPopupWindow.mOnTouchListener = touchIntercepter;
            return this;
        }


        public RefrigeratorControlPopupWindow create() {
            //构建PopWindow
            ePopPopupWindow.build();
            return ePopPopupWindow;
        }
    }


}
