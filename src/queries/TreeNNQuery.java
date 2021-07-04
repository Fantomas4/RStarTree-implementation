package queries;

import tree.*;
import utils.FileHandler;

import java.io.File;
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

    private class CandidateBranch implements Comparable<CandidateBranch> {
        Entry entry;
        double minDistance;
        double minMaxDistance;

        public CandidateBranch(Entry entry, double minDistance, double minMaxDistance) {
            this.entry = entry;
            this.minDistance = minDistance;
            this.minMaxDistance = minMaxDistance;
        }

        public Entry getEntry() {
            return entry;
        }

        public double getMinDistance() {
            return minDistance;
        }

        public double getMinMaxDistance() {
            return minMaxDistance;
        }

        @Override
        public int compareTo(CandidateBranch otherBranch) {
            double distanceA = this.entry.getBoundingBox().calculateMinPointDistance(targetPoint);
            double distanceB = otherBranch.getEntry().getBoundingBox().calculateMinPointDistance(targetPoint);

            return Double.compare(distanceA, distanceB);
        }
    }

    /**
     * Called to initialize the recursive nearest neighbor search and return the sorted query results.
     * @return an ArrayList containing the query results, sorted in an ascending order of distance.
     */
    public ArrayList<LocationQueryResult> execute() {
        search(rootNode);

        // Prepare the Array List that contains the result Records
        Neighbor neighbor = kClosestNeighborsQueue.poll();
        while (neighbor != null) {
            Record record = FileHandler.getRecord(neighbor.getBlockId(), neighbor.getRecordId()); // TODO: Get record from File Handler using neighbor.getRecordId(). CHECK!

            // Add the record to the results list
            queryResults.add(new LocationQueryResult(record, neighbor.getDistance()));

            neighbor = kClosestNeighborsQueue.poll();
        }

        Collections.sort(queryResults);

        return queryResults;
    }

    /**
     * Recursive method used to search for the nearest "k" neighbors of a given point.
     * @param currentNode the node which is to be processed.
     */
    private void search(Node currentNode) {
        if (currentNode.getLevel() != RStarTree.getLeafLevel()) {
            // Sort the activeBranches of the current node in ascending order of their
            // bounding box's distance from the target point.
            ArrayList<CandidateBranch> activeBranches = new ArrayList<>();

            for (Entry entry : currentNode.getEntries()) {
                double minDistance = entry.getBoundingBox().calculateMinPointDistance(targetPoint);
                double minMaxDistance = entry.getBoundingBox().calculateMinMaxPointDistance(targetPoint);

                activeBranches.add(new CandidateBranch(entry, minDistance, minMaxDistance));

            }

            Collections.sort(activeBranches);

            // Apply 3 pruning theorems
            ArrayList<CandidateBranch> afterPrune = new ArrayList<>();

            // Downward pruning (theorem 1)
            outerloop:
            for (int i = 0; i < activeBranches.size(); i++) {
                for (int j = 0; j < activeBranches.size(); j++) {
                    if (i != j && activeBranches.get(i).getMinDistance() > activeBranches.get(j).getMinMaxDistance()) {
                        break outerloop;
                    }
                }

                afterPrune.add(activeBranches.get(i));
            }

            activeBranches = afterPrune;

            // Apply pruning theorem 2
            for (CandidateBranch branch : activeBranches) {
                if (searchRadius > branch.getMinMaxDistance()) {
                    searchRadius = Double.MAX_VALUE;
                    break;
                }
            }

            // Recursively visit all active branches
            for (CandidateBranch branch : activeBranches) {
                // Pruning theorem 3
                if (branch.getMinDistance() <= searchRadius) {
                    Node nextNode = FileHandler.getNode(branch.getEntry().getChildNodeId());
                    search(nextNode);
                }
            }
        } else {
            ArrayList<Entry> entries = currentNode.getEntries();

            for (Entry entry : entries) {
                LeafEntry leafEntry = (LeafEntry)entry;
                double candidateDistance = leafEntry.getBoundingBox().calculateMinPointDistance(targetPoint);

                if (kClosestNeighborsQueue.size() < k) {
                    // The priority queue contains less than k neighbors, so leafEntry is
                    // simply added to the queue.
                    kClosestNeighborsQueue.add(new Neighbor(leafEntry.getBlockId(), leafEntry.getRecordId(), candidateDistance));


                } else {
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
                }
            }
        }
    }
}
