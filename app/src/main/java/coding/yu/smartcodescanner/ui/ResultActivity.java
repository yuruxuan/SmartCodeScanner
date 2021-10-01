package coding.yu.smartcodescanner.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.io.File;

import coding.yu.smartcodescanner.R;
import coding.yu.smartcodescanner.utils.EncodingHandler;

public class ResultActivity extends BaseActivity {

    private static final int FLAG_FROM_QR_CODE = 1;
    private static final int FLAG_FROM_BAR_CODE = 2;

    private static final String EXTRA_FROM_PAGE = "coding.yu.smartcodescanner.EXTRA_FROM_PAGE";
    private static final String EXTRA_SCAN_RESULT = "coding.yu.smartcodescanner.EXTRA_SCAN_RESULT";

    private String mResultStr;
    private int mFromPage = 0;
    private Bitmap mCodeBitmap;

    private ImageView imageCode;
    private TextView textResult;
    private Button textCopy;
    private Button textSavePic;
    private Button textVisitUrl;
    private Button textShareContent;

    public static void launchFromQRCode(Context context, String result) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_FROM_PAGE, FLAG_FROM_QR_CODE);
        intent.putExtra(EXTRA_SCAN_RESULT, result);
        context.startActivity(intent);
    }

    public static void launchFromBarCode(Context context, String result) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_FROM_PAGE, FLAG_FROM_BAR_CODE);
        intent.putExtra(EXTRA_SCAN_RESULT, result);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        mFromPage = intent.getIntExtra(EXTRA_FROM_PAGE, 0);
        if (mFromPage <= 0) {
            finish();
            return;
        }

        mResultStr = intent.getStringExtra(EXTRA_SCAN_RESULT);

        init();
    }

    private void init() {
        setContentView(R.layout.activity_result);

        findViewById(R.id.image_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageCode = findViewById(R.id.image_code);
        textResult = findViewById(R.id.text_result);
        textResult.setText(mResultStr);

        textCopy = findViewById(R.id.text_copy);
        textCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("ScanResult", mResultStr);
                cm.setPrimaryClip(clipData);
                Toast.makeText(ResultActivity.this, R.string.has_copy_to_board, Toast.LENGTH_SHORT).show();
            }
        });

        textSavePic = findViewById(R.id.text_save_pic);
        textSavePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    saveCodePic();
                } else {
                    PermissionUtils.permission(PermissionConstants.STORAGE).callback(new PermissionUtils.SimpleCallback() {
                        @Override
                        public void onGranted() {
                            saveCodePic();
                        }

                        @Override
                        public void onDenied() {
                            Toast.makeText(ResultActivity.this, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
                        }
                    }).request();
                }
            }
        });

        textVisitUrl = findViewById(R.id.text_visit_url);
        if (RegexUtils.isURL(mResultStr)) {
            textVisitUrl.setVisibility(View.VISIBLE);
        } else {
            textVisitUrl.setVisibility(View.GONE);
        }
        textVisitUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(mResultStr);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        textShareContent = findViewById(R.id.text_share_content);
        textShareContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndShare();
            }
        });

        generateCodePic();
    }

    private void saveAndShare() {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<File>() {
            @Nullable
            @Override
            public File doInBackground() throws Throwable {
                File file = new File(PathUtils.getExternalAppCachePath(), System.currentTimeMillis() + ".jpg");
                boolean result = ImageUtils.save(mCodeBitmap, file, Bitmap.CompressFormat.JPEG);
                if (result) {
                    return file;
                }
                return null;
            }

            @Override
            public void onSuccess(@Nullable File file) {
                try {
                    if (file != null && file.exists() && file.isFile()) {
                        shareCodePic(getString(R.string.share_to), mResultStr, Uri.fromFile(file));
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    private void shareCodePic(String dlgTitle, String content, Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }

        if (dlgTitle != null && !"".equals(dlgTitle)) {
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else {
            startActivity(intent);
        }
    }

    private void generateCodePic() {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Bitmap>() {
            @Nullable
            @Override
            public Bitmap doInBackground() throws Throwable {
                int size = SizeUtils.dp2px(120);

                if (mFromPage == FLAG_FROM_QR_CODE) {
                    return CodeCreator.createQRCode(mResultStr, size, size, null);
                }

                if (mFromPage == FLAG_FROM_BAR_CODE) {
                    return EncodingHandler.createBarCode(mResultStr, size, size / 2);
                }

                Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
                bitmap.eraseColor(Color.LTGRAY);
                return bitmap;
            }

            @Override
            public void onSuccess(@Nullable Bitmap result) {
                mCodeBitmap = result;
                imageCode.setImageBitmap(mCodeBitmap);
            }
        });
    }

    private void saveCodePic() {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Boolean>() {
            @Nullable
            @Override
            public Boolean doInBackground() throws Throwable {
                File file = new File(PathUtils.getExternalPicturesPath(), System.currentTimeMillis() + ".jpg");
                return ImageUtils.save(mCodeBitmap, file, Bitmap.CompressFormat.JPEG);
            }

            @Override
            public void onSuccess(@Nullable Boolean result) {
                if (result != null && result) {
                    Toast.makeText(ResultActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ResultActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
