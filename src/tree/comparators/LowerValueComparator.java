package tree.comparators;

import tree.Entry;

/**
 * Custom comparator used to compare entries based on their lower left point's value for a specified dimension.
 */
public class LowerValueComparator implements java.util.Comparator<Entry> {
    private final int targetDimension;

    public LowerValueComparator(int targetDimension) {
        this.targetDimension = targetDimension;
    }

    @Override
    public int compare(Entry a, Entry b) {
        double[] lowerDimensionA = a.getBoundingBox().getLowerLeftPoint();
        double[] lowerDimensionB = b.getBoundingBox().getLowerLeftPoint();

        return Double.compare(lowerDimensionA[targetDimension], lowerDimensionB[targetDimension]);
    }
}