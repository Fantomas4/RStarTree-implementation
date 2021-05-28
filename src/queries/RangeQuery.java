package queries;

import tree.*;
import utils.FileHandler;

import java.util.ArrayList;

public class RangeQuery extends Query {
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
//        System.out.println("current node id: " + currentNode.getId());

        if (currentNode.getLevel() != RStarTree.getLeafLevel()) {
            // The current node is not a leaf node and the overlap between its entries'
            // bounding boxes and the target point is checked
            for (Entry entry : nodeEntries) {
                boolean hasOverlap = entry.getBoundingBox().checkPointOverlap(targetPoint, range);
                if (hasOverlap) {
                    // The target point overlaps the entry's bounding box,
                    // so we proceed to search inside the entry's child node.
                    System.out.println("childNode id: " + entry.getChildNodeId());
                    Node childNode = FileHandler.getNode(entry.getChildNodeId()); // TODO: Get child node from File Handler using entry.getChildNodeId(). CHECK!
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
                    Record record = FileHandler.getRecord(leafEntry.getBlockId(), leafEntry.getRecordId()); // TODO: Get the leaf entry's record from File Handler using leafEntry.getRecordId(). CHECK!
                    queryResults.add(record);
                }
            }
        }
    }


}
