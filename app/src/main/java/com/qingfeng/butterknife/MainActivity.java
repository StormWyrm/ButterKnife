package com.qingfeng.butterknife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.qingfeng.butterknife2.ButterKnife;
import com.qingfeng.butterknife2.Unbinder;
import com.qingfeng.lib.annotation.BindView;

public class MainActivity extends AppCompatActivity {
    private Unbinder unbinder;

    @BindView(R.id.tv)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null)
            unbinder.unbind();
    }
}
