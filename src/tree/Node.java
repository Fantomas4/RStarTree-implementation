package tree;

import utils.EntryComparator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Node {
    static final int DIMENSIONS = 3;
    static final int MAX_ENTRIES = 10; //TODO: Get max entries per node from filehelper
    static final double MIN_LOAD_FACTOR = 0.3;
    static final int MIN_ENTRIES = (int)Math.floor(MAX_ENTRIES * MIN_LOAD_FACTOR);

    private long nodeId; //TODO: Determine how nodeIds are distributed and set.

    private ArrayList<Entry> entries;

    public Node(long nodeId) {
        this.nodeId = nodeId;
        entries = new ArrayList<>();
    }

    public Node(long nodeId, ArrayList<Entry> entries) {
        this.nodeId = nodeId;
        this.entries = entries;
    }

    public void addEntry(Entry newEntry) {
        entries.add(newEntry);
    }

    public long getId() {
        return nodeId;
    }

    private void splitNode() {
        
    }

    private class AxisDistributions {
        private final ArrayList<Distribution> distributions = new ArrayList<>();
        private double marginSum = 0;

        public void addDistribution(Distribution newDistribution, double marginValue) {
            distributions.add(newDistribution);
            marginSum += marginValue;
        }

        public double getMarginSum() {
            return  marginSum;
        }
    }

    private class Distribution {
        private final List<Entry> entriesGroupA;
        private final List<Entry> entriesGroupB;

        public Distribution(List<Entry> entriesGroupA, List<Entry> entriesGroupB) {
            this.entriesGroupA = entriesGroupA;
            this.entriesGroupB = entriesGroupB;
        }

        public List<Entry> getEntriesGroupA() {
            return entriesGroupA;
        }

        public List<Entry> getEntriesGroupB() {
            return entriesGroupB;
        }

        public double getDistributionMargin() {
            List<BoundingBox> boundingBoxesA = new ArrayList<>();
            for (Entry entry : entriesGroupA) {
                boundingBoxesA.add(entry.getBoundingBox());
            }

            List<BoundingBox> boundingBoxesB = new ArrayList<>();
            for (Entry entry : entriesGroupB) {
                boundingBoxesB.add(entry.getBoundingBox());
            }

            double marginA = BoundingBox.calculateMBR(boundingBoxesA).getMargin();
            double marginB = BoundingBox.calculateMBR(boundingBoxesB).getMargin();
            return marginA + marginB;
        }
    }

    private AxisDistributions chooseSplitAxis() {
        double minMarginSum = Double.MAX_VALUE;
        AxisDistributions minAxisDistributions = null;

        for (int d = 0; d < DIMENSIONS; d++) {
            List<Entry> sortedByLowerValue = new ArrayList<>(entries);
            entries.sort(new EntryComparator.LowerValueComparator(DIMENSIONS));
            List<Entry> sortedByUpperValue = new ArrayList<>(entries);
            entries.sort(new EntryComparator.UpperValueComparator(DIMENSIONS));

            List<List<Entry>> sortedValueLists = new ArrayList<>();
            sortedValueLists.add(sortedByLowerValue);
            sortedValueLists.add(sortedByUpperValue);

            AxisDistributions axisDistributions = new AxisDistributions();
            for (List<Entry> sortedValueList : sortedValueLists) {
                for (int k = 0; k < MAX_ENTRIES - 2 * MIN_ENTRIES + 2; k++) {
                    List<Entry> groupA = sortedValueList.subList(0, MIN_ENTRIES - 1 + k);
                    List<Entry> groupB = sortedValueList.subList(MIN_ENTRIES - 1 + k, sortedValueList.size());
                    Distribution distribution = new Distribution(groupA, groupB);
                    axisDistributions.addDistribution(distribution, distribution.getDistributionMargin());
                }
            }

            if (axisDistributions.getMarginSum() < minMarginSum) {
                minMarginSum = axisDistributions.getMarginSum();
                minAxisDistributions = axisDistributions;
            }
        }

        return minAxisDistributions;
    }


    private void chooseSplitIndex() {

    }

    //TODO: Could return fixed array instead of ArrayList?
    public ArrayList<Entry> getEntries() {
        return entries;
    }

}
