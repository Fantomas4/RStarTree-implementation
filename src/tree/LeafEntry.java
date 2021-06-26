package tree;

/**
 * Class used to represent the Leaf Entries containing the data records at the lowest level of the tree.
 */
public class LeafEntry extends Entry {
    private final long recordId; // The unique ID of the record the leaf entry points to.
    private final long blockId; // The unique ID of the block that contains the record the leaf entry points to.

    public LeafEntry(BoundingBox boundingBox, long recordId, long blockId) {
        super(boundingBox, -1); //  A value of -1 is used since leaf entries have no child node.
        this.recordId = recordId;
        this.blockId = blockId;
    }

    public long getRecordId() {
        return recordId;
    }

    public long getBlockId() {
        return blockId;
    }

    public String toString()
    {
        return "LeafEntry(" + super.toString() + ", " + "recordId(" + recordId + "), " + "blockId(" + blockId + "))";
    }

    @Override
    public byte[] toBytes()
    {
        byte[] leafEntryAsBytes = super.toBytes();
        int destPos = BoundingBox.BYTES + Long.BYTES;

        System.arraycopy(longToBytes(recordId), 0, leafEntryAsBytes, destPos, Long.BYTES);
        destPos += Long.BYTES;
        System.arraycopy(longToBytes(blockId), 0, leafEntryAsBytes, destPos, Long.BYTES);

        return leafEntryAsBytes;
    }


    public static LeafEntry fromBytes(byte[] bytes)
    {
        byte[] boundingBoxAsBytes = new byte[BoundingBox.BYTES],
                childNodeIdAsBytes = new byte[Long.BYTES],
                recordIdAsBytes = new byte[Long.BYTES],
                blockIdAsBytes = new byte[Long.BYTES];
        int srcPos = 0;

        System.arraycopy(bytes, srcPos, boundingBoxAsBytes, 0, boundingBoxAsBytes.length);
        srcPos += boundingBoxAsBytes.length;
        System.arraycopy(bytes, srcPos, childNodeIdAsBytes, 0, childNodeIdAsBytes.length);
        srcPos += childNodeIdAsBytes.length;
        System.arraycopy(bytes, srcPos, recordIdAsBytes, 0, recordIdAsBytes.length);
        srcPos += recordIdAsBytes.length;
        System.arraycopy(bytes, srcPos, blockIdAsBytes, 0, blockIdAsBytes.length);

        return new LeafEntry(
                    BoundingBox.fromBytes(boundingBoxAsBytes),
                    bytesToLong(recordIdAsBytes),
                    bytesToLong(blockIdAsBytes)
            );
    }
}
