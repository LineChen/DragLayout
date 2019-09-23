package com.line.flowlayout;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linechen on 2019/3/15.
 */
public class FlowLayout extends FrameLayout {

    private static final String TAG = "FlowLayout";

    public static final int STICKY_EDGE_NONE = 0x00000000;
    public static final int STICKY_EDGE_LEFT = 0x00000001;
    public static final int STICKY_EDGE_RIGHT = 0x00000010;
    public static final int STICKY_EDGE_LEFT_RIGHT = 0x00000011;

    private ViewDragHelper dragHelper;
    private int screenWidth;
    private int stickyEdge = STICKY_EDGE_LEFT_RIGHT;//悬停靠边
    private float showPercent = 1;//悬停时显示部分
    private int marginTop = 0;
    private int marginBottom = 0;
    private int finalLeft = -1;
    private int finalTop = -1;

    private List<DragListener> dragListenerList = new ArrayList<>();


    public FlowLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public FlowLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        if (ta != null) {
            stickyEdge = ta.getInt(R.styleable.FlowLayout_fl_stickyEdge, STICKY_EDGE_NONE);
            marginTop = ta.getInt(R.styleable.FlowLayout_fl_marginTop, 0);
            marginBottom = ta.getInt(R.styleable.FlowLayout_fl_marginBottom, 0);
            showPercent = ta.getFloat(R.styleable.FlowLayout_fl_showPercent, 1);
            ta.recycle();
        }
        init();
    }

    private void init() {
        screenWidth = getScreenWidth();
        dragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {

            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {
//                Log.d(TAG, "tryCaptureView: child = " + child);
                return true;
            }

            @Override
            public void onViewDragStateChanged(int state) {
//                Log.d(TAG, "onViewDragStateChanged: state = " + state);
            }

            @Override
            public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
//                Log.d(TAG, "onViewCaptured: capturedChild = " + capturedChild);
                onDragging(capturedChild);
            }

            @Override
            public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
                return top;
            }

            @Override
            public int getViewHorizontalDragRange(@NonNull View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }

            @Override
            public void onViewReleased(@NonNull final View releasedChild, float xvel, float yvel) {
                Log.d(TAG, "onViewReleased: " + releasedChild);
                onReleased(releasedChild);

                int viewWidth = releasedChild.getWidth();
                int viewHeight = releasedChild.getHeight();
                int curLeft = releasedChild.getLeft();
                int curTop = releasedChild.getTop();

                finalTop = curTop < marginTop ? marginTop : curTop;
                finalLeft = curLeft < 0 ? 0 : curLeft;
                if ((finalTop + viewHeight) > getHeight() - marginBottom) {
                    finalTop = getHeight() - viewHeight - marginBottom;
                }

                if ((finalLeft + viewWidth) > getWidth()) {
                    finalLeft = getWidth() - viewWidth;
                }
                int settledEdge = STICKY_EDGE_NONE;//停止的位置
                switch (stickyEdge) {
                    case STICKY_EDGE_NONE://无
                        settledEdge = STICKY_EDGE_NONE;
                        break;
                    case STICKY_EDGE_LEFT://左
                        finalLeft = -(int) (viewWidth * (1 - showPercent));
                        settledEdge = STICKY_EDGE_LEFT;
                        break;
                    case STICKY_EDGE_RIGHT://右
                        finalLeft = screenWidth - (int) (viewWidth * showPercent);
                        settledEdge = STICKY_EDGE_RIGHT;
                        break;
                    case STICKY_EDGE_LEFT_RIGHT://左右
                        finalLeft = -(int) (viewWidth * (1 - showPercent));
                        if ((curLeft + viewWidth / 2) > screenWidth / 2) {
                            finalLeft = screenWidth - (int) (viewWidth * showPercent);
                            settledEdge = STICKY_EDGE_RIGHT;
                        } else {
                            settledEdge = STICKY_EDGE_LEFT;
                        }
                        break;
                }

                dragHelper.settleCapturedViewAt(finalLeft, finalTop);
                Log.d(TAG, "onViewReleased: finalLeft = " + finalLeft + ",finalTop = " + finalTop);

                releasedChild.post(new Runnable() {
                    @Override
                    public void run() {
                        //需要保存滑动过的控件最后的位置，否则下次onLayout会将改View的位置重置为第一次添加进来的位置
                        FrameLayout.LayoutParams st = (FrameLayout.LayoutParams) releasedChild.getLayoutParams();
                        st.gravity = 0;
                        st.leftMargin = finalLeft;
                        st.topMargin = finalTop;
                        releasedChild.setLayoutParams(st);
                    }
                });

                invalidate();
                onSettling(releasedChild, settledEdge);
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG, "super onLayout: " + changed + " , " + left + ", " + top + ", " + right + ", " + bottom);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean interceptTouchEvent = dragHelper.shouldInterceptTouchEvent(ev);
//        Log.e(TAG, "onInterceptTouchEvent: interceptTouchEvent = " + interceptTouchEvent);
        boolean touchChildView = isTouchChildView(ev);
//        Log.e(TAG, "onInterceptTouchEvent: touchChildView = " + touchChildView);
        return interceptTouchEvent && touchChildView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        boolean touchChildView = isTouchChildView(event);
//        Log.e(TAG, "onTouchEvent: " + touchChildView);
        return touchChildView;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        Log.e(TAG, "dispatchTouchEvent");
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (dragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    private boolean isTouchChildView(MotionEvent ev) {
        Rect rect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            rect.set((int) view.getX(), (int) view.getY(), (int) view.getX() + view.getWidth(), (int) view.getY() + view.getHeight());
            if (rect.contains((int) ev.getX(), (int) ev.getY())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获得屏幕宽度
     *
     * @return
     */
    private int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private void onDragging(View view) {
        for (DragListener l : dragListenerList) {
            l.onDragging(view);
        }
    }

    private void onReleased(View view) {
        for (DragListener l : dragListenerList) {
            l.onReleased(view);
        }
    }

    private void onSettling(View view, int edge) {
        for (DragListener l : dragListenerList) {
            l.onSettling(view, edge);
        }
    }

    public void setStickyEdge(int stickyEdge) {
        this.stickyEdge = stickyEdge;
    }

    public void setShowPercent(float showPercent) {
        this.showPercent = showPercent;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }

    public void addDragListener(DragListener dragListener) {
        if (!dragListenerList.contains(dragListener)) {
            dragListenerList.add(dragListener);
        }
    }

    public void removeDragListener(DragListener dragListener) {
        dragListenerList.remove(dragListener);
    }

}
