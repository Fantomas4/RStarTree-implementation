package queries;

import tree.*;
import tree.comparators.DistanceToPointComparator;
import utils.FileHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

/**
 * Class used to perform RStarTree-based Nearest Neighbor (NN) queries to determine the "k"
 * closest neighbors of a given point.
 */
public class TreeNNQuery {
    private final int k;
    private double searchRadius;
    PriorityQueue<Neighbor> kClosestNeighborsQueue; // Stores the k closest neighbors found, in descending order of distance.
    private final Node rootNode;
    private final double[] targetPoint;
    private final ArrayList<LocationQueryResult> queryResults;


    public TreeNNQuery(double[] targetPoint, int k, Node rootNode) {
        this.k = k;
        this.targetPoint = targetPoint;
        this.rootNode = rootNode;

        queryResults = new ArrayList<>();
        searchRadius = Double.MAX_VALUE;
        kClosestNeighborsQueue = new PriorityQueue<>();
    }

    /**
     * Called to initialize the recursive nearest neighbor search and return the sorted query results.
     * @return an ArrayList containing the query results, sorted in an ascending order of distance.
     */
    public ArrayList<LocationQueryResult> execute() {
        search(rootNode);

        // Prepare the Array List that contains the result Records
        int numNeighbors = kClosestNeighborsQueue.size();
        for (int i = 0; i < numNeighbors; i++) {
            Neighbor neighbor = kClosestNeighborsQueue.remove();
            Record record = FileHandler.getRecord(neighbor.getBlockId(), neighbor.getRecordId()); // TODO: Get record from File Handler using neighbor.getRecordId(). CHECK!

            // Add the record to the results list
            queryResults.add(new LocationQueryResult(record, neighbor.getDistance()));
        }

        Collections.sort(queryResults);
        
        return queryResults;
    }

    /**
     * Recursive method used to search for the nearest "k" neighbors of a given point.
     * @param currentNode the node which is to be processed.
     */
    private void search(Node currentNode) {
        // Sort the entries of the current node in ascending order of their
        // bounding box's distance from the target point.
        ArrayList<Entry> entries = currentNode.getEntries();
        entries.sort(new DistanceToPointComparator(targetPoint));

        int i = 0;
        if (currentNode.getLevel() != RStarTree.getLeafLevel()) {
            // The current node is not a leaf node.
            while (i < entries.size() && kClosestNeighborsQueue.size() < k) {
                Entry entry = entries.get(i);
                if (entry.getBoundingBox().checkPointOverlap(targetPoint, searchRadius)) {
                    Node nextNode = FileHandler.getNode(entry.getChildNodeId()); // TODO: Get the next node from File Handler using entry.getChildNodeId(). CHECK!
                    search(nextNode);
                }
                i++;
            }
        } else {
            // The current node is a leaf node.
            while (i < entries.size() && kClosestNeighborsQueue.size() < k) {
                Entry entry = entries.get(i);

                LeafEntry leafEntry = (LeafEntry)entry;
                double candidateDistance = leafEntry.getBoundingBox().calculatePointDistance(targetPoint);

                if (kClosestNeighborsQueue.size() >= k) {
                    // The priority queue already contains k neighbors, so the most distant neighbor inside
                    // the queue must be compared to the candidate leafEntry.
                    double maxDistance = kClosestNeighborsQueue.peek().getDistance();

                    if (candidateDistance < maxDistance) {
                        // Remove the most distant neighbor from the priority queue and add leafEntry
                        // as a new neighbor.
                        kClosestNeighborsQueue.remove();
                        kClosestNeighborsQueue.add(new Neighbor(leafEntry.getBlockId(), leafEntry.getRecordId(), candidateDistance));

                        // Update the search radius
                        searchRadius = candidateDistance;
                    }
                } else {
                    // The priority queue contains less than k neighbors, so leafEntry is
                    // simply added to the queue.
                    kClosestNeighborsQueue.add(new Neighbor(leafEntry.getBlockId(), leafEntry.getRecordId(), candidateDistance));
                }

                i++;
            }
        }
    }
}
