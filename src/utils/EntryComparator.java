package utils;

import tree.BoundingBox;
import tree.Entry;

import java.util.*;

public class EntryComparator {

    public static class OverlapEnlargementComparator implements Comparator<Entry> {
        private ArrayList<Entry> candidateEntries;
        private Entry targetEntry;
        private Map<Entry, Double> enlargementMap;

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
                overlapSum += targetBB.calculateOverlap(candidateBoundingBox);
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

    public static class AreaEnlargementComparator implements Comparator<Entry> {
        private ArrayList<Entry> candidateEntries;
        private Entry targetEntry;
        private Map<Entry, Double> enlargementMap;

        public AreaEnlargementComparator(ArrayList<Entry> candidateEntries, Entry targetEntry) {
            this.candidateEntries = candidateEntries;
            this.targetEntry = targetEntry;
            enlargementMap = new HashMap<>();

            double areaBefore = targetEntry.getBoundingBox().getArea();
            for (Entry candidateEntry : candidateEntries) {
                ArrayList<BoundingBox> mbrBoundingBoxes = new ArrayList<>();
                mbrBoundingBoxes.add(candidateEntry.getBoundingBox());
                mbrBoundingBoxes.add(targetEntry.getBoundingBox());
                BoundingBox enlargedBB = BoundingBox.calculateMBR(mbrBoundingBoxes);
                double areaAfter = enlargedBB.getArea();
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
                if (a.getBoundingBox().getArea() >= b.getBoundingBox().getArea()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }

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
}
