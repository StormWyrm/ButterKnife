package com.github.stormwyrm.butterknifedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.stormwyrm.butterknife.ButterKnife;
import com.github.stormwyrm.butterknife.R;
import com.github.stormwyrm.butterknife.Unbinder;
import com.github.stormwyrm.butterknife.annotation.BindView;

public class OtherActivity extends AppCompatActivity {
    private Unbinder unbinder;

    @BindView(R.id.tv)
    TextView textView;

    @BindView(R.id.tv1)
    TextView textView1;

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
