import java.util.ArrayList;

public class Node {
    // Use NodeId or blockId?
    private long nodeId;

    private final int maxSize;
    private ArrayList<Entry> entries = new ArrayList<>();

    public Node(int maxSize) {
        this.maxSize = maxSize;
    }

    // Could return fixed array instead of ArrayList?
    public ArrayList<Entry> getEntries() {
        return entries;
    }

}
