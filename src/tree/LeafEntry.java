package tree;

public class LeafEntry extends Entry {
    private long recordId;
    private long blockId;

    public LeafEntry(BoundingBox boundingBox, long recordId, long blockId) {
        super(boundingBox);
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
