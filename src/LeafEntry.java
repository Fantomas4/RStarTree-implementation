public class LeafEntry extends Entry {
    private long recordId;
//    private long blockId;

    public LeafEntry(BoundingBox rectangle, long childNodeId, long recordId) {
        super(rectangle, childNodeId);
        this.recordId = recordId;
    }
}
