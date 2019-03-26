package coding.yu.smartcodescanner.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import coding.yu.smartcodescanner.R;
import coding.yu.smartcodescanner.bean.ScanItem;
import coding.yu.smartcodescanner.helper.HistoryHelper;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_QR_CODE_SCAN = 1;
    private static final int REQUEST_BAR_CODE_SCAN = 2;

    private LinearLayout layoutScanQr;
    private LinearLayout layoutScanBar;
    private LinearLayout layoutHistory;
    private LinearLayout layoutAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        setContentView(R.layout.activity_main);

        layoutScanQr = findViewById(R.id.layout_scan_qr);
        layoutScanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtils.isGranted(Manifest.permission.CAMERA)) {
                    scanQRCode();
                } else {
                    PermissionUtils.permission(PermissionConstants.CAMERA).callback(new PermissionUtils.SimpleCallback() {
                        @Override
                        public void onGranted() {
                            scanQRCode();
                        }

                        @Override
                        public void onDenied() {
                            Toast.makeText(MainActivity.this, R.string.no_camera_permission, Toast.LENGTH_SHORT).show();
                        }
                    }).request();
                }
            }
        });
        layoutScanBar = findViewById(R.id.layout_scan_bar);
        layoutScanBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtils.isGranted(Manifest.permission.CAMERA)) {
                    scanBarCode();
                } else {
                    PermissionUtils.permission(PermissionConstants.CAMERA).callback(new PermissionUtils.SimpleCallback() {
                        @Override
                        public void onGranted() {
                            scanBarCode();
                        }

                        @Override
                        public void onDenied() {
                            Toast.makeText(MainActivity.this, R.string.no_camera_permission, Toast.LENGTH_SHORT).show();
                        }
                    }).request();
                }
            }
        });
        layoutHistory = findViewById(R.id.layout_history);
        layoutHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        });
        layoutAbout = findViewById(R.id.layout_about);
        layoutAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void scanQRCode() {
        ZxingConfig config = new ZxingConfig();
        config.setPlayBeep(false);
        config.setShake(true);
        config.setFullScreenScan(true);
        config.setDecodeBarCode(false);

        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
        startActivityForResult(intent, REQUEST_QR_CODE_SCAN);
    }

    private void scanBarCode() {
        ZxingConfig config = new ZxingConfig();
        config.setPlayBeep(false);
        config.setShake(true);
        config.setFullScreenScan(true);
        config.setDecodeBarCode(true);

        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
        startActivityForResult(intent, REQUEST_BAR_CODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_QR_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.i(TAG, "QR Scan Result:" + content);
                ResultActivity.launchFromQRCode(this, content);

                ScanItem item = new ScanItem(ScanItem.TYPE_QR, content, System.currentTimeMillis());
                HistoryHelper.getInstance().add(item);
            }
        }

        if (requestCode == REQUEST_BAR_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.i(TAG, "Bar Scan Result:" + content);
                ResultActivity.launchFromBarCode(this, content);

                ScanItem item = new ScanItem(ScanItem.TYPE_BAR, content, System.currentTimeMillis());
                HistoryHelper.getInstance().add(item);
            }
        }
    }
}
