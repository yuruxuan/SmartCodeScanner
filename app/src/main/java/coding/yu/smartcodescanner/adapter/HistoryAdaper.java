package coding.yu.smartcodescanner.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import coding.yu.smartcodescanner.R;
import coding.yu.smartcodescanner.bean.ScanItem;

public class HistoryAdaper extends BaseQuickAdapter<ScanItem, BaseViewHolder> {


    public HistoryAdaper(@Nullable List<ScanItem> data) {
        super(R.layout.item_history, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ScanItem item) {
        helper.setText(R.id.text_result, item.content);

        String typeStr = "";
        if (item.type == ScanItem.TYPE_QR) {
            typeStr = mContext.getString(R.string.qr_code);
        }

        if (item.type == ScanItem.TYPE_BAR) {
            typeStr = mContext.getString(R.string.bar_code);
        }

        helper.setText(R.id.text_type, typeStr);
    }
}
