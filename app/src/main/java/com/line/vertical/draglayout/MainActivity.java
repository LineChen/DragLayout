package com.line.vertical.draglayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.line.flowlayout.DragListener;
import com.line.flowlayout.FlowLayout;

public class MainActivity extends AppCompatActivity {

    TextView imageView;
    View blackBtn;
    private FlowLayout flowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);
        blackBtn = findViewById(R.id.btn_black);
        blackBtn.setTag("black");
        flowLayout = findViewById(R.id.flow_layout);
        flowLayout.addDragListener(new DragListener() {
            @Override
            public void onDragging(View view) {
                if (view == imageView) {
                    imageView.setBackgroundColor(Color.YELLOW);
                    imageView.setText("我在拖动");
                }
            }

            @Override
            public void onReleased(View view) {

            }

            @Override
            public void onSettling(View view, int edge) {
                if (view == imageView) {
                    imageView.setBackgroundColor(Color.LTGRAY);
                    String text = "我贴在" + (edge == FlowLayout.STICKY_EDGE_LEFT ? "左边" : "右边");
                    imageView.setText(text);
                }
            }
        });

        TextView addedTextView = new TextView(this);
        addedTextView.setText("addView 添加");
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        flowLayout.addView(addedTextView, params);
    }

    public void black(View view) {
        Toast.makeText(this, "black", Toast.LENGTH_SHORT).show();
        blackBtn.setVisibility(View.GONE);
        flowLayout.removeView(blackBtn);
    }

    public void red(View view) {
        blackBtn.setVisibility(View.VISIBLE);
        flowLayout.removeView(blackBtn);
        flowLayout.addView(blackBtn);
        Toast.makeText(this, "red", Toast.LENGTH_SHORT).show();
    }

    public void blue(View view) {
        blackBtn.setVisibility(View.GONE);
        Toast.makeText(this, "blue", Toast.LENGTH_SHORT).show();
    }
}
