package com.github.stormwyrm.butterknifedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.stormwyrm.butterknife.ButterKnife;
import com.github.stormwyrm.butterknife.R;
import com.github.stormwyrm.butterknife.Unbinder;
import com.github.stormwyrm.butterknife.annotation.BindView;
import com.github.stormwyrm.butterknife.annotation.OnClick;

public class MainActivity extends AppCompatActivity {
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

    @OnClick(R.id.tv)
    public void onClick(View view) {

    }
}
