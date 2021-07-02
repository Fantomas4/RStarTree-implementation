package tree.comparators;

import tree.Entry;

/**
 * Custom comparator used to compare two entries based on their bounding boxes' distances from a specified point.
 */
public class DistanceToPointComparator implements java.util.Comparator<Entry> {
    private final double[] targetPoint;

    public DistanceToPointComparator(double[] targetPoint) {
        this.targetPoint = targetPoint;
    }

    @Override
    public int compare(Entry a, Entry b) {
        double distanceA = a.getBoundingBox().calculatePointDistance(targetPoint);
        double distanceB = b.getBoundingBox().calculatePointDistance(targetPoint);

        return Double.compare(distanceA, distanceB);
    }
}