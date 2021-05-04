package tree;

import utils.EntryComparator;

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
        ArrayList<Distribution> distributions;
        double marginSum = 0;

        public AxisDistributions() {
            distributions = new ArrayList<>();
        }

        public void addDistribution(Distribution newDistribution, double marginValue) {
            distributions.add(newDistribution);
            marginSum += marginValue;
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

    }

    private void chooseSplitAxis() {
        for (int d = 0; d < DIMENSIONS; d++) {
            ArrayList<Entry> sortedByLowerValue = new ArrayList<>(entries);
            entries.sort(new EntryComparator.LowerValueComparator(DIMENSIONS));
            ArrayList<Entry> sortedByUpperValue = new ArrayList<>(entries);
            entries.sort(new EntryComparator.UpperValueComparator(DIMENSIONS));

            ArrayList<ArrayList<Entry>> sortedValueLists = new ArrayList<>();
            sortedValueLists.add(sortedByLowerValue);
            sortedValueLists.add(sortedByUpperValue);

            double sumOfMargins = 0;
            for (ArrayList<Entry> sortedValueList : sortedValueLists) {

                for (int k = 0; k < MAX_ENTRIES - 2 * MIN_ENTRIES + 2; k++) {
                    List<Entry> groupA = sortedValueList.subList(0, MIN_ENTRIES - 1 + k);
                    List<Entry> groupB = sortedValueList.subList(MIN_ENTRIES - 1 + k, sortedValueList.size());

                    
                }
            }

        }
    }


    private void chooseSplitIndex() {

    }

    //TODO: Could return fixed array instead of ArrayList?
    public ArrayList<Entry> getEntries() {
        return entries;
    }

}
