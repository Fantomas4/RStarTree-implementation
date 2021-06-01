import queries.LocationQueryResult;
import queries.SequentialNNQuery;
import queries.SequentialRangeQuery;
import tree.RStarTree;
import tree.Record;

import java.util.ArrayList;

public class CLI {

    public static void main(String[] args) {
        RStarTree rStarTree = new RStarTree();
        rStarTree.initialize();

        double[] testCoords = new double[2];
        testCoords[0] = 0;
        testCoords[1] = 0;
//
        ArrayList<LocationQueryResult> queryResults;

//        queryResults = rStarTree.executeNNQuery(testCoords,10);
//        queryResults = rStarTree.executeRangeQuery(testCoords, 49.2604);

//        queryResults = new SequentialNNQuery(testCoords, 10).execute();
        queryResults = new SequentialRangeQuery(testCoords, 49.2604).execute();

        for (LocationQueryResult result : queryResults) {
            System.out.println("-----------------------------------------");
            System.out.println("Record ID: " + result.getRecordId());
            System.out.println("Name: " + result.getName());
            double[] coordinates = result.getCoordinates();
            System.out.format("Coordinates: %f,%f\n", coordinates[0], coordinates[1]);
            System.out.println("Distance: " + result.getDistance());
            System.out.println("-----------------------------------------\n");
        }

        System.out.println("DONE!");
    }

}

