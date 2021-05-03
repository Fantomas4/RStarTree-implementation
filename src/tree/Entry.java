package tree;

import java.io.Serializable;

public class Entry implements Serializable {
    protected BoundingBox boundingBox;
    private long childNodeId;

    public Entry(BoundingBox boundingBox, long childNodeId) {
        this.boundingBox = boundingBox;
        this.childNodeId = childNodeId;
    }

    public Entry(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        this.childNodeId = - 1; // tree.Entry has no child node
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public long getChildNodeId() {
        return childNodeId;
    }
}
