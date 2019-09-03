package com.android.launcher3;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class FolderEditText extends EditText {

    private Folder mFolder;

    /**
     * Implemented by listeners of the back key.
     */
    public interface OnBackKeyListener {
        public boolean onBackKey();
    }

    private OnBackKeyListener mBackKeyListener;

    public FolderEditText(Context context) {
        super(context);
    }

    public FolderEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FolderEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnBackKeyListener(OnBackKeyListener listener) {
        mBackKeyListener = listener;
    }

    public void setFolder(Folder folder) {
        mFolder = folder;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        // Catch the back button on the soft keyboard so that we can just close the activity
        /*if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK) {
            mFolder.doneEditingFolderName(true);
        }
        return super.onKeyPreIme(keyCode, event);*/

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mBackKeyListener != null) {
                return mBackKeyListener.onBackKey();
            }
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
