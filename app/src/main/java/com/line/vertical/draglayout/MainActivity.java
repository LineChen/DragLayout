package com.line.vertical.draglayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.line.flowlayout.DragListener;
import com.line.flowlayout.FlowLayout;

public class MainActivity extends AppCompatActivity {

    TextView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);
        FlowLayout flowLayout = findViewById(R.id.flow_layout);
        flowLayout.setDragListener(new DragListener() {
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
    }

    public void black(View view) {
        Toast.makeText(this, "black", Toast.LENGTH_SHORT).show();
    }

    public void red(View view) {
        Toast.makeText(this, "red", Toast.LENGTH_SHORT).show();
    }

    public void blue(View view) {
        Toast.makeText(this, "blue", Toast.LENGTH_SHORT).show();
    }
}
