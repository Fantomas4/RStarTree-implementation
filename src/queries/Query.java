package queries;

import tree.Node;
import tree.Record;

import java.util.ArrayList;

public class Query {
    protected double[] targetPoint;
    protected Node rootNode;
    protected ArrayList<Record> queryResults;

    public Query(double[] targetPoint, Node rootNode) {
        this.targetPoint = targetPoint;
        this.rootNode = rootNode;
        queryResults = new ArrayList<>();
    }
}
