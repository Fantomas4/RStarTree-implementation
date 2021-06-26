package queries;

import tree.*;
import utils.DataMetaData;
import utils.FileHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import static java.lang.Math.sqrt;

/**
 * Class used to perform sequential Nearest Neighbor (NN) queries on the datafile to determine
 * the "k" closest neighbors of a given point.
 */
public class SequentialNNQuery {
    private final int k;
    private final double[] targetPoint;
    private final ArrayList<LocationQueryResult> queryResults;
    PriorityQueue<Neighbor> kClosestNeighborsQueue; // Stores the k closest neighbors found, in descending order of distance.


    public SequentialNNQuery(double[] targetPoint, int k) {
        this.k = k;
        this.targetPoint = targetPoint;
        queryResults = new ArrayList<>();

        kClosestNeighborsQueue = new PriorityQueue<>();
    }

    /**
     * Calculates the distance of a given point from the range query's specified target point.
     * @param candidatePoint the given point whose distance is calculated from the range query's
     *                       specified target point.
     * @return a number representing the calculated distance.
     */
    private double calculateDistanceFromTarget(double[] candidatePoint) {
        int dimensions = targetPoint.length;
        double sum = 0;

        for (int d = 0; d < dimensions; d++) {
            double diff = targetPoint[d] - candidatePoint[d];
            sum += Math.pow(diff, 2);
        }

        return sqrt(sum);
    }

    /**
     * Called to start the nearest neighbor search and return the sorted query results.
     * @return an ArrayList containing the query results, sorted in an ascending order of distance.
     */
    public ArrayList<LocationQueryResult> execute() {
        search();

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
     * Performs a search to locate the "k" Nearest Neighbors (NN) of the given target point.
     */
    private void search() {
        long numBlocks = DataMetaData.getNumberOfBlocks();

        for (int blockId = 0; blockId < numBlocks; blockId++) {
            ArrayList<Record> blockRecords = FileHandler.getDataBlock(blockId);
            for (Record record : blockRecords) {
                double candidateDistance = calculateDistanceFromTarget(record.getCoordinates());

                if (kClosestNeighborsQueue.size() >= k) {
                    // The priority queue already contains k neighbors, so the most distant neighbor inside
                    // the queue must be compared to the candidate leafEntry.
                    double maxDistance = kClosestNeighborsQueue.peek().getDistance();

                    if (candidateDistance < maxDistance) {
                        // Remove the most distant neighbor from the priority queue and add leafEntry
                        // as a new neighbor.
                        kClosestNeighborsQueue.remove();
                        kClosestNeighborsQueue.add(new Neighbor(blockId, record.getId(), candidateDistance));
                    }
                } else {
                    // The priority queue contains less than k neighbors, so leafEntry is
                    // simply added to the queue.
                    kClosestNeighborsQueue.add(new Neighbor(blockId, record.getId(), candidateDistance));
                }
            }
        }
    }
}
