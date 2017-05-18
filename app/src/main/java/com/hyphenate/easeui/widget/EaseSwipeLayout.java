package com.hyphenate.easeui.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chatuidemo.R;


/**
 * Created by Bruce on 11/24/14.
 * Modified by linan on 4/5/17.
 */


public class EaseSwipeLayout extends LinearLayout {

    public static abstract class SwipeListener {
        void onOpen(EaseSwipeLayout layout) {}
        void onClose(EaseSwipeLayout layout) {}
    }

    public static class SwipeAction {
        public SwipeAction(String name, String color, View.OnClickListener onClickListener) {
            this.buttonName = name;
            this.color = color;
            this.onClickListener = onClickListener;
        }
        String buttonName;
        String color;
        View.OnClickListener onClickListener;
    }

    private ViewDragHelper viewDragHelper;
    private View contentView;
    private View actionView;
    private int dragDistance;
    private final double AUTO_OPEN_SPEED_LIMIT = 800.0;
    private int draggedX;

    private SwipeListener mSwipeListener;
    private View listItem;

    private boolean disable = false;

    Handler handler = new Handler();

    public EaseSwipeLayout(Context context) {
        this(context, null);
    }

    public EaseSwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public EaseSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewDragHelper = ViewDragHelper.create(this, new DragHelperCallback());
    }

    public void setSwipeListener(SwipeListener listener) {
        mSwipeListener = listener;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onFinishInflate() {
        contentView = getChildAt(0);
        actionView = getChildAt(1);
        actionView.setVisibility(GONE);
        super.onFinishInflate();
    }

    public void attachListItem(View listItem) {
        this.listItem = listItem;
    }

    public void setButtons(SwipeAction[] actions) {
        disable = actions.length == 0;

        ((ViewGroup) actionView).removeAllViews();
        LayoutInflater layoutInflater = LayoutInflater.from(this.getContext());
        int validCount = 0;
        for (int i = 0; i < actions.length; i++) {
            final SwipeAction action = actions[i];
            if (action == null) {
                continue;
            }
            View convertView0 = layoutInflater.inflate(R.layout.ease_list_item_action_item, (ViewGroup) actionView, false);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            ((ViewGroup) actionView).addView(convertView0, params);
            ((TextView)convertView0.findViewById(R.id.text_item)).setText(action.buttonName);
            if (action.color != null && !action.color.isEmpty()) {
                convertView0.findViewById(R.id.text_item).setBackgroundColor(Color.parseColor(action.color));
            }
            convertView0.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (action.onClickListener != null) {
                        action.onClickListener.onClick(listItem);
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            EaseSwipeLayout.this.close();
                        }
                    }, 200);
                }
            });
            validCount++;
        }
        dragDistance = dip2px(getContext(), 50 * validCount);
        actionView.invalidate();
    }

    public void updateListPosition(int position) {
        listItem.setTag(new Integer(position));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View view, int i) {
            return view == contentView || view == actionView;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            draggedX = left;
            if (changedView == contentView) {
                actionView.offsetLeftAndRight(dx);
            } else {
                contentView.offsetLeftAndRight(dx);
            }
            if (actionView.getVisibility() == View.GONE) {
                actionView.setVisibility(View.VISIBLE);
            }
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == contentView) {
                final int leftBound = getPaddingLeft();
                final int minLeftBound = -leftBound - dragDistance;
                final int newLeft = Math.min(Math.max(minLeftBound, left), 0);
                return newLeft;
            } else {
                final int minLeftBound = getPaddingLeft() + contentView.getMeasuredWidth() - dragDistance;
                final int maxLeftBound = getPaddingLeft() + contentView.getMeasuredWidth() + getPaddingRight();
                final int newLeft = Math.min(Math.max(left, minLeftBound), maxLeftBound);
                return newLeft;
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return dragDistance;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            boolean settleToOpen = false;
            if (xvel > AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = false;
            } else if (xvel < -AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = true;
            } else if (draggedX <= -dragDistance / 2) {
                settleToOpen = true;
            } else if (draggedX > -dragDistance / 2) {
                settleToOpen = false;
            }

            final int settleDestX = settleToOpen ? -dragDistance : 0;
            viewDragHelper.smoothSlideViewTo(contentView, settleDestX, 0);
            ViewCompat.postInvalidateOnAnimation(EaseSwipeLayout.this);

            if (mSwipeListener != null && settleToOpen == false) {
                mSwipeListener.onClose(EaseSwipeLayout.this);
            }
            if (mSwipeListener != null && settleToOpen == true) {
                mSwipeListener.onOpen(EaseSwipeLayout.this);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(disable == false && viewDragHelper.shouldInterceptTouchEvent(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (disable == true) {
            return false;
        }
        viewDragHelper.processTouchEvent(event);
//        return super.onTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void close() {
        viewDragHelper.smoothSlideViewTo(contentView, 0, 0);
        ViewCompat.postInvalidateOnAnimation(EaseSwipeLayout.this);
        if (mSwipeListener != null) {
            mSwipeListener.onClose(EaseSwipeLayout.this);
        }
    }

    public View getListItem() {
        return listItem;
    }

}
