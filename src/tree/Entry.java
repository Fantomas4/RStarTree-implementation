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

    public void adjustBoundingBox(Node updatedChildNode) {
        boundingBox = BoundingBox.calculateMBR(updatedChildNode.getEntries());
    }

    public long getChildNodeId() {
        return childNodeId;
    }

    public void setChildNodeId(long childNodeId) {
        this.childNodeId = childNodeId;
    }
}
