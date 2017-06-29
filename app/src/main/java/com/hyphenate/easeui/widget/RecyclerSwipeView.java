package com.hyphenate.easeui.widget;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.hyphenate.chatuidemo.R;

import java.util.List;


/**
 * Created by linan on 17/4/5.
 */

public class RecyclerSwipeView extends RecyclerView {

    private static final String TAG = "swipe";

    private static EaseSwipeLayout mOpenItem;

    public RecyclerSwipeView(Context context) {
        super(context);
    }

    public RecyclerSwipeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public RecyclerSwipeView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (mOpenItem != null) {
            if (!isViewUnder(mOpenItem.getListItem(), (int)e.getX(), (int)e.getY())) {
                mOpenItem.close();
                return true;
            }
        }
        return super.onInterceptTouchEvent(e);
    }

    private boolean isViewUnder(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        return x >= view.getLeft() &&
                x < view.getRight() &&
                y >= view.getTop() &&
                y < view.getBottom();
    }

    /**
     * RecyclerSwipeAdapter
     * @param <VH>
     */
    public abstract class RecyclerSwipeAdapter<VH extends SwipeViewHolder, T> extends RecyclerView.Adapter<VH> {

        List<T> data;

        public RecyclerSwipeAdapter(List<T> _data) {
            data = _data;
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }


    public static class SwipeViewHolder extends RecyclerView.ViewHolder {
        private EaseSwipeLayout layout;

        public SwipeViewHolder(View itemView, EaseSwipeLayout.SwipeListener listener) {
            super(itemView);
            layout = (EaseSwipeLayout) itemView.findViewById(R.id.swipe_layout);
            layout.setSwipeListener(listener);
        }
    }

    private EaseSwipeLayout.SwipeListener listener = new EaseSwipeLayout.SwipeListener() {

        @Override
        public void onOpen(EaseSwipeLayout layout) {
            mOpenItem = layout;
            Log.d(TAG, "onOpen");
        }


        @Override
        public void onClose(EaseSwipeLayout layout) {
            mOpenItem = null;
            Log.d(TAG, "onClose");
        }
    };

    public EaseSwipeLayout.SwipeListener getSwipeListener() {
        return listener;
    }

}

