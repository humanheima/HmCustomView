package com.humanheima.hmcustomview.ui.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.humanheima.hmcustomview.R;
import com.humanheima.hmcustomview.ui.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SwitchButtonExampleActivity extends AppCompatActivity {

    @BindView(R.id.text_test_without_animation)
    TextView textTestWithoutAnimation;
    @BindView(R.id.sb_without_animation)
    SwitchButton sbWithoutAnimation;
    @BindView(R.id.text_set_text)
    TextView textSetText;
    @BindView(R.id.sb_set_text)
    SwitchButton sbSetText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_button_example);
        ButterKnife.bind(this);
        sbWithoutAnimation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(SwitchButtonExampleActivity.this, "选中状态", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SwitchButtonExampleActivity.this, "取消状态", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sbSetText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(SwitchButtonExampleActivity.this, "选中状态", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SwitchButtonExampleActivity.this, "取消状态", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @OnClick({R.id.text_test_without_animation, R.id.text_set_text})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_test_without_animation:
                sbWithoutAnimation.setCheckedImmediately(!sbWithoutAnimation.isChecked());
                break;
            case R.id.text_set_text:
                SpannableString ss = new SpannableString("abc");
                Drawable d = getResources().getDrawable(R.drawable.icon_blog);
                if (d != null) {
                    d.setBounds(0, d.getIntrinsicWidth() / 4, d.getIntrinsicWidth() / 2, d.getIntrinsicHeight() * 3 / 4);
                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                    ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    sbSetText.setText(ss, "OFF");
                }
                break;
        }
    }
}
