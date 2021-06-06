package queries;

import tree.Node;
import tree.Record;

import java.util.ArrayList;

public class Query {
    protected double[] targetPoint;
    protected ArrayList<LocationQueryResult> queryResults;

    public Query(double[] targetPoint) {
        this.targetPoint = targetPoint;
        queryResults = new ArrayList<>();
    }
}
