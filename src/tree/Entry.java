package tree;

import utils.ByteConvertible;


/**
 * Class used to represent the entries each node contains.
 */
public class Entry extends ByteConvertible {
    protected BoundingBox boundingBox; // The minimum bounding box of the entry that is determined based on its child node.
    private long childNodeId; // The child node's unique ID.
    // (isLeafNode, BoundingBox, childNodeId, recordId, blockId)
    public static final int BYTES = BoundingBox.BYTES + 3 * Long.BYTES;

    public Entry(BoundingBox boundingBox, long childNodeId) {
        this.boundingBox = boundingBox;
        this.childNodeId = childNodeId;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Used to recalculate the bounding box of the entry when its child node is updated.
     * @param updatedChildNode the updated child node for which we want the entry's bounding box to be recalculated
     *                         in order to become a minimum bounding box (MBR)
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

    @Override
    public byte[] toBytes()
    {
        byte[] entryAsBytes = new byte[BYTES];
        int destPos = 0;

        System.arraycopy(boundingBox.toBytes(), 0, entryAsBytes, destPos, BoundingBox.BYTES);
        destPos += BoundingBox.BYTES;
        System.arraycopy(longToBytes(childNodeId), 0, entryAsBytes, destPos, Long.BYTES);

        return entryAsBytes;
    }


    public static Entry fromBytes(byte[] bytes)
    {
        byte[] boundingBoxAsBytes = new byte[BoundingBox.BYTES],
                childNodeIdAsBytes = new byte[Long.BYTES];
        int srcPos = 0;

        System.arraycopy(bytes, srcPos, boundingBoxAsBytes, 0, boundingBoxAsBytes.length);
        srcPos += boundingBoxAsBytes.length;
        System.arraycopy(bytes, srcPos, childNodeIdAsBytes, 0, childNodeIdAsBytes.length);

        return new Entry(BoundingBox.fromBytes(boundingBoxAsBytes), bytesToLong(childNodeIdAsBytes));
    }
}
