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
        ArrayList<Record> nnQueryResult = rStarTree.executeNNQuery(testCoords,10);
//        ArrayList<Record> rangeQueryResult = rStarTree.executeRangeQuery(testCoords, 49.2604);

//        ArrayList<Record> seqNNQueryResult = new SequentialNNQuery(testCoords, 10).execute();
//        ArrayList<Record> seqRangeQueryResult = new SequentialRangeQuery(testCoords, 49.2604).execute();
        System.out.println("DONE!");
    }

}

