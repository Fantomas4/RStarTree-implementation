package tree.comparators;

import tree.BoundingBox;
import tree.Entry;

/**
 * Custom comparator used to compare two entries based on their bounding boxes centers' distances from the center of
 * a specified bounding box.
 */
public class BBCenterDistanceComparator implements java.util.Comparator<Entry> {
    private final double[] centerTargetBB;

    public BBCenterDistanceComparator(BoundingBox targetBoundingBox) {
        centerTargetBB = targetBoundingBox.calculateCenter();
    }

    private double calculateDistance(double[] pointA, double[] pointB) {
        int dimensions = pointA.length; // pointA and pointB have the same number of dimensions.

        // Calculate the Euclidean distance between point A and point B.
        double sum = 0;

        for (int d = 0; d < dimensions; d++) {
            sum += Math.pow(pointA[d] - pointB[d], 2);
        }

        return Math.sqrt(sum);
    }

    @Override
    public int compare(Entry a, Entry b) {
        double[] centerA = a.getBoundingBox().calculateCenter();
        double[] centerB = b.getBoundingBox().calculateCenter();

        double distanceA = calculateDistance(centerA, centerTargetBB);
        double distanceB = calculateDistance(centerB, centerTargetBB);

        return Double.compare(distanceA, distanceB);
    }
}