package queries;

import tree.*;
import utils.FileHandler;

import java.util.ArrayList;
import java.util.Collections;

public class RangeQuery extends Query {
    private final double range;
    protected Node rootNode;

    public RangeQuery(double[] targetPoint, double range, Node rootNode) {
        super(targetPoint);

        this.rootNode = rootNode;
        this.range = range;
    }

    public ArrayList<LocationQueryResult> execute() {
        search(rootNode);
        Collections.sort(queryResults);

        return queryResults;
    }

    private void search(Node currentNode) {
        ArrayList<Entry> nodeEntries = currentNode.getEntries();

        if (currentNode.getLevel() != RStarTree.getLeafLevel()) {
            // The current node is not a leaf node and the overlap between its entries'
            // bounding boxes and the target point is checked
            for (Entry entry : nodeEntries) {
                boolean hasOverlap = entry.getBoundingBox().checkPointOverlap(targetPoint, range);
                if (hasOverlap) {
                    // The target point overlaps the entry's bounding box,
                    // so we proceed to search inside the entry's child node.
                    Node childNode = FileHandler.getNode(entry.getChildNodeId()); // TODO: Get child node from File Handler using entry.getChildNodeId(). CHECK!
                    search(childNode);
                }
            }
        } else {
            // The current node is a leaf node containing leaf entries
            for (Entry entry : nodeEntries) {
                LeafEntry leafEntry = (LeafEntry) entry;

                double candidateDistance = leafEntry.getBoundingBox().calculatePointDistance(targetPoint);
                if (candidateDistance <= range) {
                    // The distance between the leaf node's record and the target point is less than or equal to the
                    // specified range, so we proceed to add the leaf entry's record to the query results.
                    Record record = FileHandler.getRecord(leafEntry.getBlockId(), leafEntry.getRecordId()); // TODO: Get the leaf entry's record from File Handler using leafEntry.getRecordId(). CHECK!
                    queryResults.add(new LocationQueryResult(record, candidateDistance));
                }
            }
        }
    }


}
