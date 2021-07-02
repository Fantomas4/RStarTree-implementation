package tree.comparators;

import tree.BoundingBox;
import tree.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom comparator used to compare two entries based on the area enlargement effect on their bounding boxes
 * from the addition of a specified entry to them.
 */
public class AreaEnlargementComparator implements java.util.Comparator<Entry> {
    private final Map<Entry, Double> enlargementMap;

    public AreaEnlargementComparator(ArrayList<Entry> candidateEntries, Entry targetEntry) {
        enlargementMap = new HashMap<>();

        for (Entry candidateEntry : candidateEntries) {
            ArrayList<BoundingBox> mbrBoundingBoxes = new ArrayList<>();
            mbrBoundingBoxes.add(candidateEntry.getBoundingBox());
            mbrBoundingBoxes.add(targetEntry.getBoundingBox());
            BoundingBox enlargedBB = BoundingBox.calculateMBR(mbrBoundingBoxes);

            double areaBefore = candidateEntry.getBoundingBox().calculateArea();
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
            return Double.compare(a.getBoundingBox().calculateArea(), b.getBoundingBox().calculateArea());
        }
    }
}