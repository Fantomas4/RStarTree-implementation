package tree;

import utils.ByteConvertible;

public class Entry extends ByteConvertible {
    protected BoundingBox boundingBox;
    private long childNodeId;
    // (isLeafNode, BoundingBox, childNodeId, recordId, blockId)
    public static final int BYTES = BoundingBox.BYTES + 3 * Long.BYTES;

    public Entry(BoundingBox boundingBox, long childNodeId) {
        this.boundingBox = boundingBox;
        this.childNodeId = childNodeId;
    }

    public Entry(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        this.childNodeId = -1; // tree.Entry has no child node
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
