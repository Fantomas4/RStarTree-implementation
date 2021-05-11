package queries;

import tree.*;

import java.util.ArrayList;

public class RangeQuery extends Query{
    private final double range;

    public RangeQuery(double[] targetPoint, double range, Node rootNode) {
        super(targetPoint, rootNode);
        this.range = range;
    }

    public ArrayList<Record> execute() {
        search(rootNode);
        return queryResults;
    }

    private void search(Node currentNode) {
        ArrayList<Entry> nodeEntries = currentNode.getEntries();

        if (currentNode.getLevel() != RStarTree.getLEAF_LEVEL()) {
            // The current node is not a leaf node and the overlap between the center
            // point and its entries' bounding boxes is checked
            for (Entry entry : nodeEntries) {
                boolean hasOverlap = entry.getBoundingBox().checkPointOverlap(targetPoint, range);
                if (hasOverlap) {
                    // The target point overlaps the entry's bounding box,
                    // so we proceed to search inside the entry's child node.
                    Node childNode; // TODO: Get child node from File Handler using entry.getChildNodeId()
                    search(childNode);
                }
            }
        } else {
            // The current node is a leaf node containing leaf entries
            for (Entry entry : nodeEntries) {
                LeafEntry leafEntry = (LeafEntry) entry;

                boolean hasOverlap = leafEntry.getBoundingBox().checkPointOverlap(targetPoint, range);
                if (hasOverlap) {
                    // The target point overlaps the leaf entry's bounding box,
                    // so we proceed to add the leaf entry's record to the query results.
                    Record record; // TODO: Get the leaf entry's record from File Handler using leafEntry.getRecordId();
                    queryResults.add(record);
                }
            }
        }
    }


}
