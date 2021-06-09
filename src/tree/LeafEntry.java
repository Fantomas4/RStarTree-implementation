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
}
