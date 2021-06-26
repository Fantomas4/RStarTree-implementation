import org.w3c.dom.ls.LSInput;
import queries.LocationQueryResult;
import queries.SequentialNNQuery;
import queries.SequentialRangeQuery;
import tree.RStarTree;
import tree.Record;
import utils.DataMetaData;
import utils.FileHandler;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class CLI {

    public static void run()
    {
        Scanner scanner = new Scanner(System.in);
        String input;
        do {
            System.out.println("Options:");
            System.out.println("1) K - Nearest Neighbour Query");
            System.out.println("2) Range Query");
            System.out.println("0) Exit");

            System.out.print("Select option: ");
            input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);

            System.out.println();
            switch (input)
            {
                case "1":
                    System.out.println("K - Nearest Neighbour Query selected");
                    break;
                case "2":
                    System.out.println("Range Query selected");
                    break;
            }
        } while (!input.equals("0"));

    }

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
            System.out.println(result);
        }

        System.out.println("DONE!");



    }

}

