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
import android.view.ViewGroup;
import android.widget.FrameLayout;

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

    private int[] position = new int[4];
    private boolean useLastChildPosition;
    private DragListener dragListener;


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
        screenWidth = getScreenWidth(getContext());
        dragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {

            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {
                Log.d(TAG, "tryCaptureView: child = " + child);
                return true;
            }

            @Override
            public void onViewDragStateChanged(int state) {
                Log.d(TAG, "onViewDragStateChanged: state = " + state);
            }

            @Override
            public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
                Log.d(TAG, "onViewCaptured: capturedChild = " + capturedChild);
                if (dragListener != null) {
                    dragListener.onDragging(capturedChild);
                }
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
            public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                dragListener.onReleased(releasedChild);
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
                invalidate();
                if (dragListener != null) {
                    dragListener.onSettling(releasedChild, settledEdge);
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (finalLeft == -1 && finalTop == -1) {
            super.onLayout(changed, left, top, right, bottom);
        } else if (useLastChildPosition) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                child.layout(position[0], position[1], position[2], position[3]);
            }
        }
        useLastChildPosition = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return isTouchChildView(event);
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
     * @param context
     * @return
     */
    private int getScreenWidth(Context context) {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics outMetrics = new DisplayMetrics();
//        wm.getDefaultDisplay().getMetrics(outMetrics);
//        return outMetrics.widthPixels;
    }

    @Override
    public void removeAllViews() {
        beforeRemove();
        super.removeAllViews();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        useLastChildPosition = true;
    }

    private void beforeRemove() {
        final int count = getChildCount();
        if (count > 0) {
            View child = getChildAt(0);
            position[0] = child.getLeft();
            position[1] = child.getTop();
            position[2] = child.getRight();
            position[3] = child.getBottom();
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

    public void setDragListener(DragListener dragListener) {
        this.dragListener = dragListener;
    }
}
