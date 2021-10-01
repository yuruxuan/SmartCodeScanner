package coding.yu.smartcodescanner.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import coding.yu.smartcodescanner.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        findViewById(R.id.image_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
