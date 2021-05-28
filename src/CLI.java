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

        ArrayList<Record> nnQueryResult = rStarTree.executeNNQuery(testCoords,5);
        ArrayList<Record> rangeQueryResult = rStarTree.executeRangeQuery(testCoords, 50);
        System.out.println("DONE!");
    }

}

