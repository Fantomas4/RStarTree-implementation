package tree;

public class LeafEntry extends Entry {
    private long recordId;
    private long blockId;

    public LeafEntry(BoundingBox rectangle, long recordId, long blockId) {
        super(rectangle);
        this.recordId = recordId;
        this.blockId = blockId;
    }

    public long getRecordId() {
        return recordId;
    }

    public long getBlockId() {
        return blockId;
    }
}
