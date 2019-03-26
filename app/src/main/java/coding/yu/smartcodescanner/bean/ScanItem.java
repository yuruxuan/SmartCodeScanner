package coding.yu.smartcodescanner.bean;

public class ScanItem {

    public static final int TYPE_QR = 1;
    public static final int TYPE_BAR = 2;

    public int type;
    public String content;
    public long time;

    public ScanItem() {
    }

    public ScanItem(int type, String content, long time) {
        this.type = type;
        this.content = content;
        this.time = time;
    }
}
