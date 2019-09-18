package com.line.flowlayout;

import android.view.View;

/**
 * Created by linechen on 2019-09-17.
 */
public interface DragListener {
    void onDragging(View view);

    void onReleased(View view);

    void onSettling(View view, int edge);
}
