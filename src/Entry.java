import java.io.Serializable;

public class Entry implements Serializable {
    private BoundingBox rectangle;

    // use blockId or nodeId?
    private long childNodeId;

    public Entry(BoundingBox rectangle, long childNodeId) {
        this.rectangle = rectangle;
        this.childNodeId = childNodeId;
    }

    public BoundingBox getRectangle() {
        return rectangle;
    }

    public long getChildNodeId() {
        return childNodeId;
    }
}
