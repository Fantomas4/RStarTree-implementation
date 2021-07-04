package tree.comparators;

import tree.Entry;

/**
 * Custom comparator used to compare entries based on their upper right point's value for a specified dimension.
 */
public class UpperValueComparator implements java.util.Comparator<Entry> {
    private final int targetDimension;

    public UpperValueComparator(int targetDimension) {
        this.targetDimension = targetDimension;
    }

    @Override
    public int compare(Entry a, Entry b) {
        double[] upperValueA = a.getBoundingBox().getUpperRightPoint();
        double[] upperValueB = b.getBoundingBox().getUpperRightPoint();

        return Double.compare(upperValueA[targetDimension], upperValueB[targetDimension]);
    }
}