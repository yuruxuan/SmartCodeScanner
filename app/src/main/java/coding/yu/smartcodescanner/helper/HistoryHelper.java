package coding.yu.smartcodescanner.helper;

import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.GsonUtils;

import java.util.ArrayList;
import java.util.List;

import coding.yu.smartcodescanner.bean.ScanItem;

public class HistoryHelper {

    private static final String TAG = "HistoryHelper";
    private static final String KEY_SCAN_DATA = "scan_data";
    private static final int MAX_SIZE = 10;

    private volatile static HistoryHelper sInstance;

    private HistoryHelper() {
    }

    public static HistoryHelper getInstance() {
        if (sInstance == null) {
            synchronized (HistoryHelper.class) {
                if (sInstance == null) {
                    sInstance = new HistoryHelper();
                }
            }
        }
        return sInstance;
    }

    public void add(ScanItem item) {
        String data = CacheDiskUtils.getInstance().getString(KEY_SCAN_DATA);
        List<ScanItem> list;
        if (data == null) {
            list = new ArrayList<>();
        } else {
            list = GsonUtils.fromJson(data, GsonUtils.getListType(ScanItem.class));
        }

        list.add(0, item);

        if (list.size() > MAX_SIZE) {
            list = list.subList(0, 10);
        }

        String result = GsonUtils.toJson(list);
        CacheDiskUtils.getInstance().put(KEY_SCAN_DATA, result);
    }

    public List<ScanItem> getAll() {
        String data = CacheDiskUtils.getInstance().getString(KEY_SCAN_DATA);
        if (data == null) {
            return new ArrayList<>();
        } else {
            return GsonUtils.fromJson(data, GsonUtils.getListType(ScanItem.class));
        }
    }
}
