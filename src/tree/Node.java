package tree;

import utils.EntryComparator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private static final int DIMENSIONS = 3;
    private static final int MAX_ENTRIES = 10; //TODO: Get max entries per node from filehelper
    private static final double MIN_LOAD_FACTOR = 0.3;
    private static final int MIN_ENTRIES = (int)Math.floor(MAX_ENTRIES * MIN_LOAD_FACTOR);

    private long nodeId; //TODO: Determine how nodeIds are distributed and set.
    private ArrayList<Entry> entries;
    private int level;


//    public Node(ArrayList<Entry> entries, int level) {
//        this.entries = entries;
//        this.level = level;
//        this.nodeId = -1; // The node id has not been set
//    }

    public Node(ArrayList<Entry> entries, int level, long nodeId) {
        this.entries = entries;
        this.level = level;
        this.nodeId = nodeId;
    }

    // Used fpr creating a root Node
    public Node(int level, long nodeId) {
        this.entries = new ArrayList<>();
        this.level = level;
        this.nodeId = nodeId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addEntry(Entry newEntry) {
        entries.add(newEntry);
    }

    //TODO: Could return fixed array instead of ArrayList?
    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<Entry> entries) {
        this.entries = entries;
    }

    public boolean isOverflowed() {
        return entries.size() > MAX_ENTRIES;
    }

    public long getId() {
        return nodeId;
    }

    public void setId(long nodeId) {
        this.nodeId = nodeId;
    }

    public static int getMaxEntriesLimit() {
        return MAX_ENTRIES;
    }

    public ArrayList<Node> splitNode() {
        AxisDistributions axisDistributions = chooseSplitAxis();
        Distribution chosenDistribution = chooseSplitIndex(axisDistributions);

        ArrayList<Node> resultNodes = new ArrayList<>();
        // TODO: Set Node IDs for split nodes!
        resultNodes.add(new Node(chosenDistribution.getEntriesGroupA(), level, nodeId));
        // TODO: Get new node ID for the second split node from File Handler
        long newNodeId;
        resultNodes.add(new Node(chosenDistribution.getEntriesGroupB(), level, newNodeId));

        return resultNodes;
    }

    private class AxisDistributions {
        private final ArrayList<Distribution> distributions = new ArrayList<>();
        private double marginSum = 0;

        public void addDistribution(Distribution newDistribution, double marginValue) {
            distributions.add(newDistribution);
            marginSum += marginValue;
        }

        public ArrayList<Distribution> getDistributions() {
            return distributions;
        }

        public double getMarginSum() {
            return  marginSum;
        }
    }

    private class Distribution {
        private final ArrayList<Entry> entriesGroupA;
        private final ArrayList<Entry> entriesGroupB;

        public Distribution(ArrayList<Entry> entriesGroupA, ArrayList<Entry> entriesGroupB) {
            this.entriesGroupA = entriesGroupA;
            this.entriesGroupB = entriesGroupB;
        }

        public ArrayList<Entry> getEntriesGroupA() {
            return entriesGroupA;
        }

        public ArrayList<Entry> getEntriesGroupB() {
            return entriesGroupB;
        }
        //TODO: Replace duplicate code with a method/optimize?
        public double getDistributionMargin() {
            ArrayList<BoundingBox> boundingBoxesA = new ArrayList<>();
            for (Entry entry : entriesGroupA) {
                boundingBoxesA.add(entry.getBoundingBox());
            }

            ArrayList<BoundingBox> boundingBoxesB = new ArrayList<>();
            for (Entry entry : entriesGroupB) {
                boundingBoxesB.add(entry.getBoundingBox());
            }

            double marginA = BoundingBox.calculateMBR(boundingBoxesA).getMargin();
            double marginB = BoundingBox.calculateMBR(boundingBoxesB).getMargin();
            return marginA + marginB;
        }

        public double getDistributionOverlap() {
            ArrayList<BoundingBox> boundingBoxesA = new ArrayList<>();

            for (Entry entry : entriesGroupA) {
                boundingBoxesA.add(entry.getBoundingBox());
            }

            ArrayList<BoundingBox> boundingBoxesB = new ArrayList<>();
            for (Entry entry : entriesGroupB) {
                boundingBoxesB.add(entry.getBoundingBox());
            }
            return BoundingBox.calculateMBR(boundingBoxesA).calculateOverlap(BoundingBox.calculateMBR(boundingBoxesB));
        }

        public double getDistributionArea() {
            ArrayList<BoundingBox> boundingBoxesA = new ArrayList<>();

            for (Entry entry : entriesGroupA) {
                boundingBoxesA.add(entry.getBoundingBox());
            }

            ArrayList<BoundingBox> boundingBoxesB = new ArrayList<>();
            for (Entry entry : entriesGroupB) {
                boundingBoxesB.add(entry.getBoundingBox());
            }

            double areaA = BoundingBox.calculateMBR(boundingBoxesA).getArea();
            double areaB = BoundingBox.calculateMBR(boundingBoxesB).getArea();
            return areaA + areaB;
        }
    }

    private AxisDistributions chooseSplitAxis() {
        double minMarginSum = Double.MAX_VALUE;
        AxisDistributions minAxisDistributions = null;

        for (int d = 0; d < DIMENSIONS; d++) {
            ArrayList<Entry> sortedByLowerValue = new ArrayList<>(entries);
            sortedByLowerValue.sort(new EntryComparator.LowerValueComparator(DIMENSIONS));
            ArrayList<Entry> sortedByUpperValue = new ArrayList<>(entries);
            sortedByUpperValue.sort(new EntryComparator.UpperValueComparator(DIMENSIONS));

            ArrayList<ArrayList<Entry>> sortedValueLists = new ArrayList<>();
            sortedValueLists.add(sortedByLowerValue);
            sortedValueLists.add(sortedByUpperValue);

            AxisDistributions axisDistributions = new AxisDistributions();
            for (ArrayList<Entry> sortedValueList : sortedValueLists) {
                for (int k = 0; k < MAX_ENTRIES - 2 * MIN_ENTRIES + 2; k++) {
                    List<Entry> groupA = sortedValueList.subList(0, MIN_ENTRIES - 1 + k);
                    List<Entry> groupB = sortedValueList.subList(MIN_ENTRIES - 1 + k, sortedValueList.size());
                    Distribution distribution = new Distribution(new ArrayList<>(groupA), new ArrayList<>(groupB));
                    axisDistributions.addDistribution(distribution, distribution.getDistributionMargin());
                }
            }

            double axisMarginSum = axisDistributions.getMarginSum();
            if (axisMarginSum < minMarginSum) {
                minMarginSum = axisMarginSum;
                minAxisDistributions = axisDistributions;
            }
        }

        return minAxisDistributions;
    }


    private Distribution chooseSplitIndex(AxisDistributions chosenAxisDistributions) {
        ArrayList<Distribution> distributions = chosenAxisDistributions.getDistributions();
        int minIndex = 0;
        double minOverlapValue = distributions.get(minIndex).getDistributionOverlap();

        for (int i = 1; i < distributions.size(); i++) {
            Distribution distribution = distributions.get(i);

            double distributionOverlap = distribution.getDistributionOverlap();
            if (distributionOverlap < minOverlapValue) {
                // Choose the distribution with the minimum overlap-value.
                minOverlapValue = distributionOverlap;
                minIndex = i;
            } else if (distributionOverlap == minOverlapValue) {
                // Resole ties by choosing the distribution with minimum area-value.
                double minDistributionArea = distributions.get(minIndex).getDistributionArea();
                double currentDistributionArea = distribution.getDistributionArea();

                if (currentDistributionArea < minDistributionArea) {
                    minIndex = i;
                }
            }
        }

        return distributions.get(minIndex);
    }
}
