package queries;

public class Neighbor implements Comparable<Neighbor> {
    private final long blockId;
    private final long recordId;
    private final double distance;

    public Neighbor(long blockId, long recordId, double distance) {
        this.blockId = blockId;
        this.recordId = recordId;
        this.distance = distance;
    }

    public long getBlockId() {
        return blockId;
    }

    public long getRecordId() {
        return recordId;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Neighbor other) {
        // Using this method, class objects are sorted
        // in descending order of distance.
        return Double.compare(other.distance, this.distance);
    }
}
