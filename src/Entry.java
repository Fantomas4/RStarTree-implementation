import java.io.Serializable;

public class Entry implements Serializable {
    private static long idCount = 0;

    private final long entryId;
    private Rectangle rectangle;

    // use blockId or nodeId?
    private long childBlockId;

    public Entry(Rectangle rectangle, long blockId) {
        this.rectangle = rectangle;
        this.childBlockId = blockId;
        entryId = idCount ++;
    }

    public long getEntryId() {
        return entryId;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public long getChildBlockId() {
        return childBlockId;
    }
}
