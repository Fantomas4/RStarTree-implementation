package queries;

import tree.*;
import utils.FileHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

public class NearestNeighborsQuery extends Query {
    private class Neighbor implements Comparable<Neighbor> {
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

    private final int k;
    private double searchRadius;
    PriorityQueue<Neighbor> kClosestNeighborsQueue; // Stores the k closest neighbors found, in descending order of distance.

    public NearestNeighborsQuery(double[] targetPoint, int k, Node rootNode) {
        super(targetPoint, rootNode);
        this.k = k;
        searchRadius = Double.MAX_VALUE;
        kClosestNeighborsQueue = new PriorityQueue<>();
    }

    public ArrayList<Record> execute() {
        search(rootNode);

        // Prepare the Array List that contains the result Records
        for (int i = 0; i < kClosestNeighborsQueue.size(); i++) {
            Neighbor neighbor = kClosestNeighborsQueue.remove();
            Record record = FileHandler.getRecord(neighbor.getBlockId(), neighbor.getRecordId()); // TODO: Get record from File Handler using neighbor.getRecordId(). CHECK!

            // Add the record to the results list
            queryResults.add(record);
        }

        // Since the elements returned from the queue using remove() are given
        // in an descending order of distance from the specified target point,
        // the contents of queryResults are reversed so that they follow
        // an ascending order of distance.
        Collections.reverse(queryResults);
        
        return queryResults;
    }

    private void search(Node currentNode) {
        // Sort the entries of the current node in ascending order of their
        // bounding box's distance from the target point.
        ArrayList<Entry> entries = currentNode.getEntries();
        entries.sort(new EntryComparator.DistanceToPointComparator(targetPoint));

        if (currentNode.getLevel() != RStarTree.getLeafLevel()) {
            // The current node is not a leaf node.
            for (Entry entry : entries) {
                if (entry.getBoundingBox().calculatePointDistance(targetPoint) <= searchRadius) {
                    Node nextNode = FileHandler.getNode(entry.getChildNodeId()); // TODO: Get the next node from File Handler using entry.getChildNodeId(). CHECK!
                    search(nextNode);
                }
            }
        } else {
            // The current node is a leaf node.
            for (Entry entry : entries) {
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
            }
        }
    }
}
