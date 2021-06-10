package queries;

import tree.*;
import utils.DataMetaData;
import utils.FileHandler;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.sqrt;

/**
 * Class used to perform sequential range queries on the datafile to detect the neighbors
 * of a given point in a specified range.
 */
public class SequentialRangeQuery {
    private final double range;
    private final double[] targetPoint;
    private final ArrayList<LocationQueryResult> queryResults;

    public SequentialRangeQuery(double[] targetPoint, double range) {
        this.targetPoint = targetPoint;
        this.range = range;

        queryResults = new ArrayList<>();
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
        Collections.sort(queryResults);

        return queryResults;
    }

    /**
     * Performs a search to locate the neighbors of the given target point inside
     * the specified radius.
     */
    private void search() {
        int numBlocks = DataMetaData.getNumberOfBlocks();

        for (int blockId = 0; blockId < numBlocks; blockId++) {
            ArrayList<Record> blockRecords = FileHandler.getDataBlock(blockId);
            for (Record record : blockRecords) {
                double candidateDistance = calculateDistanceFromTarget(record.getCoordinates());
                if (candidateDistance <= range) {
                    queryResults.add(new LocationQueryResult(record,candidateDistance));
                }
            }
        }
    }
}
