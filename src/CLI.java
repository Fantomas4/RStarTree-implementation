import queries.LocationQueryResult;
import queries.SequentialNNQuery;
import queries.SequentialRangeQuery;
import tree.RStarTree;
import tree.Record;
import utils.DataMetaData;
import utils.FileHandler;

import java.util.ArrayList;

public class CLI {

    public static void main(String[] args) {
        RStarTree rStarTree = new RStarTree();

        double[] testCoords = new double[2];
        testCoords[0] = 0;
        testCoords[1] = 0;
//
        ArrayList<LocationQueryResult> queryResults;
        // 49.261

        queryResults = rStarTree.executeNNQuery(testCoords,36);
//        queryResults = rStarTree.executeRangeQuery(testCoords, 49.27);

//        queryResults = new SequentialNNQuery(testCoords, 40).execute();
//        queryResults = new SequentialRangeQuery(testCoords, 49.27).execute();

//        ArrayList<Record[]> datafile = FileHandler.getDummyDataFile();
//        for (Record[] records : datafile) {
//            for (int i = 0; i < DataMetaData.getMaxRecordsInBlock(); i++) {
//                System.out.println(records[i]);
//            }
//        }
//
        System.out.println("*** Found " + queryResults.size() + " query results.");

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

