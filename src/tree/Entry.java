package tree;

import java.io.Serializable;

/**
 * Class used to represent the entries each node contains.
 */
public class Entry implements Serializable {
    protected BoundingBox boundingBox; // The minimum bounding box of the entry that is determined based on its child node.
    private final long childNodeId; // The child node's unique ID.

    public Entry(BoundingBox boundingBox, long childNodeId) {
        this.boundingBox = boundingBox;
        this.childNodeId = childNodeId;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Used to recalculate the bounding box of the entry when its child node is updated.
     * @param updatedChildNode
     */
    public void adjustBoundingBox(Node updatedChildNode) {
        boundingBox = BoundingBox.calculateMBR(updatedChildNode.getEntries());
    }

    public long getChildNodeId() {
        return childNodeId;
    }

    public String toString()
    {
        return "Entry(" + boundingBox.toString() + ", " + "childNodeId(" + childNodeId + "))";
    }
}
