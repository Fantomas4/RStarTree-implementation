package tree.comparators;

import tree.Entry;

/**
 * Custom comparator used to compare two entries based on their bounding boxes' distances from a specified point.
 */
public class MinPointDistanceComparator implements java.util.Comparator<Entry> {
    private final double[] targetPoint;

    public MinPointDistanceComparator(double[] targetPoint) {
        this.targetPoint = targetPoint;
    }

    @Override
    public int compare(Entry a, Entry b) {
        double distanceA = a.getBoundingBox().calculateMinPointDistance(targetPoint);
        double distanceB = b.getBoundingBox().calculateMinPointDistance(targetPoint);

        return Double.compare(distanceA, distanceB);
    }
}