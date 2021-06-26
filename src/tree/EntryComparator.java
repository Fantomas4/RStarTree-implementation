package tree;

import java.util.*;

/**
 * Class that contains custom entry comparators based on different comparison criteria.
 */
public class EntryComparator {

    /**
     * Custom comparator used to compare two entries based on the overlap enlargement effect on their bounding boxes
     * from the addition of a specified entry to them.
     */
    public static class OverlapEnlargementComparator implements Comparator<Entry> {
        private final ArrayList<Entry> candidateEntries;
        private final Entry targetEntry;
        private final Map<Entry, Double> enlargementMap;

        public OverlapEnlargementComparator(ArrayList<Entry> candidateEntries, Entry targetEntry) {
            this.candidateEntries = candidateEntries;
            this.targetEntry = targetEntry;
            enlargementMap = new HashMap<>();

            double overlapBefore = calculateOverlap(targetEntry.getBoundingBox());
            for (Entry candidateEntry : candidateEntries) {
                ArrayList<BoundingBox> mbrBoundingBoxes = new ArrayList<>();
                mbrBoundingBoxes.add(candidateEntry.getBoundingBox());
                mbrBoundingBoxes.add(targetEntry.getBoundingBox());
                BoundingBox enlargedBB = BoundingBox.calculateMBR(mbrBoundingBoxes);

                double overlapAfter = calculateOverlap(enlargedBB);
                double overlapDiff = overlapAfter - overlapBefore;

                if (overlapDiff < 0) {
                    throw new IllegalStateException("The overlap difference should not be a negative number.");
                }

                enlargementMap.put(candidateEntry, overlapDiff);
            }

        }

        private double calculateOverlap(BoundingBox targetBB) {
            double overlapSum = 0;

            for (Entry candidateEntry: candidateEntries) {
                BoundingBox candidateBoundingBox = candidateEntry.getBoundingBox();
                //TODO: Check example for a different approach here
                overlapSum += targetBB.calculateBoundingBoxOverlap(candidateBoundingBox);
            }
            return overlapSum;
        }


        @Override
        public int compare(Entry a, Entry b) {
            double overlapScoreA = enlargementMap.get(a);
            double overlapScoreB = enlargementMap.get(b);

            if (overlapScoreA > overlapScoreB) {
                return 1;
            } else if (overlapScoreA < overlapScoreB) {
                return -1;
            } else {
                // both Entry objects have equal overlap enlargement values,
                // so the tie is resolved by choosing the entry whose rectangle
                // needs the least area enlargement
                ArrayList<Entry> candidateEntries= new ArrayList<>();
                candidateEntries.add(a);
                candidateEntries.add(b);
                Entry resultEntry = Collections.min(candidateEntries, new AreaEnlargementComparator(candidateEntries, targetEntry));

                if (resultEntry == a) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }

    /**
     * Custom comparator used to compare two entries based on the area enlargement effect on their bounding boxes
     * from the addition of a specified entry to them.
     */
    public static class AreaEnlargementComparator implements Comparator<Entry> {
        private final ArrayList<Entry> candidateEntries;
        private final Entry targetEntry;
        private final Map<Entry, Double> enlargementMap;

        public AreaEnlargementComparator(ArrayList<Entry> candidateEntries, Entry targetEntry) {
            this.candidateEntries = candidateEntries;
            this.targetEntry = targetEntry;
            enlargementMap = new HashMap<>();

            double areaBefore = targetEntry.getBoundingBox().calculateArea();
            for (Entry candidateEntry : candidateEntries) {
                ArrayList<BoundingBox> mbrBoundingBoxes = new ArrayList<>();
                mbrBoundingBoxes.add(candidateEntry.getBoundingBox());
                mbrBoundingBoxes.add(targetEntry.getBoundingBox());
                BoundingBox enlargedBB = BoundingBox.calculateMBR(mbrBoundingBoxes);
                double areaAfter = enlargedBB.calculateArea();
                double areaDiff = areaAfter - areaBefore;

                if (areaDiff < 0 ) {
                    throw new IllegalStateException("The area difference should not be a negative number.");
                }

                enlargementMap.put(candidateEntry, areaDiff);
            }
        }

        @Override
        public int compare(Entry a, Entry b) {
            double areaScoreA = enlargementMap.get(a);
            double areaScoreB = enlargementMap.get(b);

            if (areaScoreA > areaScoreB) {
                return 1;
            } else if (areaScoreA < areaScoreB) {
                return -1;
            } else {
                // both Entry objects have equal area enlargement values,
                // so the tie is resolved by choosing the entry with the rectangle
                // of smallest area
                if (a.getBoundingBox().calculateArea() > b.getBoundingBox().calculateArea()) {
                    return 1;
                } else if (a.getBoundingBox().calculateArea() < b.getBoundingBox().calculateArea())  {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    /**
     * Custom comparator used to compare entries based on their upper right point's value for a specified dimension.
     */
    public static class UpperValueComparator implements Comparator<Entry> {
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

    /**
     * Custom comparator used to compare entries based on their lower left point's value for a specified dimension.
     */
    public static class LowerValueComparator implements Comparator<Entry> {
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

    /**
     * Custom comparator used to compare two entries based on their bounding boxes centers' distances from the center of
     * a specified bounding box.
     */
    public static class BBCenterDistanceComparator implements Comparator<Entry> {
        private final BoundingBox targetBoundingBox;

        public BBCenterDistanceComparator(BoundingBox targetBoundingBox) {
            this.targetBoundingBox = targetBoundingBox;
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
            double[] centerTargetBB = targetBoundingBox.calculateCenter();
            double[] centerA = a.getBoundingBox().calculateCenter();
            double[] centerB = b.getBoundingBox().calculateCenter();

            double distanceA = calculateDistance(centerA, centerTargetBB);
            double distanceB = calculateDistance(centerB, centerTargetBB);

            return Double.compare(distanceA, distanceB);
        }
    }

    /**
     * Custom comparator used to compare two entries based on their bounding boxes' distances from a specified point.
     */
    public static class DistanceToPointComparator implements Comparator<Entry> {
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
}
