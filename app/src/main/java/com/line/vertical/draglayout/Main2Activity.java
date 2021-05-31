package com.line.vertical.draglayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * created by chenliu on  2021/5/31 11:25 上午.
 * 验证：
 * 1.覆盖在另一个view上不影响该view的事件（OK）
 * 2.滑动后可以停到任意地方（OK）
 * 3.拖动到边缘外，显示指定最边缘位置（OK）
 */
public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
}
