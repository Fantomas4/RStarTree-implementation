package queries;

import tree.*;
import utils.DataMetaData;
import utils.FileHandler;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.sqrt;

public class SequentialRangeQuery extends Query{
    private final double range;

    public SequentialRangeQuery(double[] targetPoint, double range) {
        super(targetPoint);

        this.range = range;
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
        Collections.sort(queryResults);

        return queryResults;
    }

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
