package coding.yu.smartcodescanner.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.blankj.utilcode.util.ThreadUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import coding.yu.smartcodescanner.R;
import coding.yu.smartcodescanner.adapter.HistoryAdaper;
import coding.yu.smartcodescanner.bean.ScanItem;
import coding.yu.smartcodescanner.helper.HistoryHelper;

public class HistoryActivity extends BaseActivity {

    private AppCompatImageView imageBack;
    private RecyclerView recyclerView;


    private HistoryAdaper mAdapter;
    private List<ScanItem> mList = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new HistoryAdaper(mList);
        mAdapter.bindToRecyclerView(recyclerView);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ScanItem item = mList.get(position);
                if (item.type == ScanItem.TYPE_QR) {
                    ResultActivity.launchFromQRCode(HistoryActivity.this, item.content);
                    return;
                }

                if (item.type == ScanItem.TYPE_BAR) {
                    ResultActivity.launchFromBarCode(HistoryActivity.this, item.content);
                    return;
                }
            }
        });

        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<List<ScanItem>>() {
            @Nullable
            @Override
            public List<ScanItem> doInBackground() throws Throwable {
                return HistoryHelper.getInstance().getAll();
            }

            @Override
            public void onSuccess(@Nullable List<ScanItem> result) {
                if (result == null) {
                    return;
                }
                mList.clear();
                mList.addAll(result);
                mAdapter.notifyDataSetChanged();
            }
        });
    }
}
