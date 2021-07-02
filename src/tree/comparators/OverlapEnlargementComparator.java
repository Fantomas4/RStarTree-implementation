package tree.comparators;

import tree.BoundingBox;
import tree.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom comparator used to compare two entries based on the overlap enlargement effect on their bounding boxes
 * from the addition of a specified entry to them.
 */
public class OverlapEnlargementComparator implements java.util.Comparator<Entry> {
    private final ArrayList<Entry> candidateEntries;
    private final Entry targetEntry;
    private final Map<Entry, Double> enlargementMap;

    public OverlapEnlargementComparator(ArrayList<Entry> candidateEntries, Entry targetEntry) {
        this.candidateEntries = candidateEntries;
        this.targetEntry = targetEntry;
        enlargementMap = new HashMap<>();

        for (Entry candidateEntry : candidateEntries) {
            ArrayList<BoundingBox> mbrBoundingBoxes = new ArrayList<>();
            mbrBoundingBoxes.add(candidateEntry.getBoundingBox());
            mbrBoundingBoxes.add(targetEntry.getBoundingBox());
            BoundingBox enlargedBB = BoundingBox.calculateMBR(mbrBoundingBoxes);

            // A new entry generated from candidateEntry using the enlarged Bounding Box that includes targetEntry.
            Entry newEntry = new Entry(enlargedBB, candidateEntry.getChildNodeId());

            double overlapBefore = calculateOverlap(candidateEntry, candidateEntry.getBoundingBox());
            double overlapAfter = calculateOverlap(candidateEntry, newEntry.getBoundingBox());
            double overlapDiff = overlapAfter - overlapBefore;

            if (overlapDiff < 0) {
                throw new IllegalStateException("The overlap difference should not be a negative number.");
            }

            enlargementMap.put(candidateEntry, overlapDiff);
        }

    }

    private double calculateOverlap(Entry excludedEntry, BoundingBox testBB) {
        double overlapSum = 0;

        for (Entry candidateEntry: candidateEntries) {
            BoundingBox candidateBoundingBox = candidateEntry.getBoundingBox();
            //TODO: Check example for a different approach here
            if (candidateEntry != excludedEntry) {
                overlapSum += testBB.calculateBoundingBoxOverlap(candidateBoundingBox);
            }
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
            // Both Entry objects have equal overlap enlargement values,
            // so the tie is resolved by choosing the entry whose rectangle
            // needs the least area enlargement
            ArrayList<Entry> candidateEntries = new ArrayList<>();
            candidateEntries.add(a);
            candidateEntries.add(b);

            return new AreaEnlargementComparator(candidateEntries, targetEntry).compare(a, b);
        }
    }
}