package queries;

import tree.*;
import utils.DataMetaData;
import utils.FileHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import static java.lang.Math.sqrt;

public class SequentialNNQuery extends Query{
    private final int k;
    private double searchRadius;
    PriorityQueue<Neighbor> kClosestNeighborsQueue; // Stores the k closest neighbors found, in descending order of distance.

    public SequentialNNQuery(double[] targetPoint, int k) {
        super(targetPoint);
        this.k = k;
        searchRadius = Double.MAX_VALUE;
        kClosestNeighborsQueue = new PriorityQueue<>();
    }

    private double calculateDistanceFromTarget(double[] candidatePoint) {
        int dimensions = targetPoint.length;
        double sum = 0;

        for (int d = 0; d < dimensions; d++) {
            double diff = targetPoint[d] - candidatePoint[d];
            sum += Math.pow(diff, 2);
        }

        return sqrt(sum);
    }

    public ArrayList<LocationQueryResult> execute() {
        search();

        // Prepare the Array List that contains the result Records
        for (int i = 0; i < k; i++) {
            Neighbor neighbor = kClosestNeighborsQueue.remove();
            Record record = FileHandler.getRecord(neighbor.getBlockId(), neighbor.getRecordId()); // TODO: Get record from File Handler using neighbor.getRecordId(). CHECK!

            // Add the record to the results list
            queryResults.add(new LocationQueryResult(record, neighbor.getDistance()));
        }

        Collections.sort(queryResults);

        return queryResults;
    }

    private void search() {
        int numBlocks = DataMetaData.getNumberOfBlocks();

        for (int blockId = 1; blockId < numBlocks; blockId++) {
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

                        // Update the search radius
                        searchRadius = candidateDistance;
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
