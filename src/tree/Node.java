package tree;

import utils.ByteConvertible;
import utils.FileHandler;
import utils.IndexMetaData;

import java.util.ArrayList;
import java.util.List;


public class Node extends ByteConvertible {
    private static final int DIMENSIONS = 2; //TODO: Get number of dimensions from file handler.
    private static final int MAX_ENTRIES = 3; //TODO: Get max entries per node from File Handler using FileHandler.getMaxEntriesInBlock();
    private static final double MIN_LOAD_FACTOR = 0.4;
    private static final int MIN_ENTRIES = (int)Math.floor(MAX_ENTRIES * MIN_LOAD_FACTOR);
    // (NodeId, entriesSize, isLeafNode, entries, level)
    public static final int BYTES = Long.BYTES + Integer.BYTES + 1 + (IndexMetaData.maxEntriesInNode + 1) * Entry.BYTES + Integer.BYTES;

    private long nodeId; // The unique ID assigned to the node.
    private ArrayList<Entry> entries; // A list containing all the entries the node includes.
    private int level; // The tree level where the node is placed.

    /**
     * Constructor used to initialize a non-root node.
     * @param entries
     * @param level the tree level where the node will be placed.
     * @param nodeId the unique ID assigned to this node.
     */
    public Node(ArrayList<Entry> entries, int level, long nodeId) {
        this.entries = entries;
        this.level = level;
        this.nodeId = nodeId;
    }

    /**
     * Constructor used to initialize a root node
     * @param level the tree level where the node will be placed.
     * @param nodeId the unique ID assigned to this node.
     */
    //
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

    /**
     * Returns the maximum amount of entries a node can include.
     * @return an integer representing the maximum amount of entries that can be stored in a node.
     */
    public static int getMaxEntriesLimit() {
        return MAX_ENTRIES;
    }

    /**
     * Splits the original node into 2 nodes. The first node uses the same ID as the original node, while the
     * second node is assigned a new ID from the File Handler.
     * @return An ArrayList containing the 2 nodes produced by splitting the original node.
     */
    public ArrayList<Node> splitNode() {
        AxisDistributions axisDistributions = chooseSplitAxis();
        Distribution chosenDistribution = chooseSplitIndex(axisDistributions);

        ArrayList<Node> resultNodes = new ArrayList<>();
        // TODO: Set Node IDs for split nodes! CHECK!
        // Use the old node ID for the first split node produced
        setEntries(chosenDistribution.getEntriesGroupA());
        resultNodes.add(this);
        // TODO: Get new node ID for the second split node from File Handler. CHECK!
        // Use a new node ID for the second split node produced
        long newNodeId = FileHandler.getNextAvailableNodeId();
        resultNodes.add(new Node(chosenDistribution.getEntriesGroupB(), level, newNodeId));

        return resultNodes;
    }

    /**
     * Class that stores the entries of an axis-based node split into the 2 discrete groups specified.
     */
    private class Distribution {
        private final ArrayList<Entry> entriesGroupA;
        private final ArrayList<Entry> entriesGroupB;

        public Distribution(ArrayList<Entry> entriesGroupA, ArrayList<Entry> entriesGroupB) {
            this.entriesGroupA = entriesGroupA;
            this.entriesGroupB = entriesGroupB;
        }

        /**
         * Getter method that returns the first group of entries in this distribution.
         * @return an ArrayList that contains the entries of the first group in this distribution.
         */
        public ArrayList<Entry> getEntriesGroupA() {
            return entriesGroupA;
        }

        /**
         * Getter method that returns the second group of entries in this distribution.
         * @return an ArrayList that contains the entries of the second group in this distribution.
         */
        public ArrayList<Entry> getEntriesGroupB() {
            return entriesGroupB;
        }

        /**
         * Calculates the total margin of the bounding boxes of this distribution's entries.
         * @return a number representing the total margin of this distribution entries' bounding boxes.
         */
        public double getDistributionMargin() {
            //TODO: Replace duplicate code with a method/optimize?
            ArrayList<BoundingBox> boundingBoxesA = new ArrayList<>();
            for (Entry entry : entriesGroupA) {
                boundingBoxesA.add(entry.getBoundingBox());
            }

            ArrayList<BoundingBox> boundingBoxesB = new ArrayList<>();
            for (Entry entry : entriesGroupB) {
                boundingBoxesB.add(entry.getBoundingBox());
            }

            double marginA = BoundingBox.calculateMBR(boundingBoxesA).calculateMargin();
            double marginB = BoundingBox.calculateMBR(boundingBoxesB).calculateMargin();
            return marginA + marginB;
        }

        /**
         * Calculates the total overlap of the bounding boxes of this distribution's entries.
         * @return a number representing the total overlap of this distribution entries' bounding boxes.
         */
        public double getDistributionOverlap() {
            ArrayList<BoundingBox> boundingBoxesA = new ArrayList<>();

            for (Entry entry : entriesGroupA) {
                boundingBoxesA.add(entry.getBoundingBox());
            }

            ArrayList<BoundingBox> boundingBoxesB = new ArrayList<>();
            for (Entry entry : entriesGroupB) {
                boundingBoxesB.add(entry.getBoundingBox());
            }
            return BoundingBox.calculateMBR(boundingBoxesA).calculateBoundingBoxOverlap(BoundingBox.calculateMBR(boundingBoxesB));
        }

        /**
         * Calculate the total area of the bounding boxes of this distribution's entries.
         * @return a number representing the total area of this distribution entries' bounding boxes.
         */
        public double getDistributionArea() {
            ArrayList<BoundingBox> boundingBoxesA = new ArrayList<>();

            for (Entry entry : entriesGroupA) {
                boundingBoxesA.add(entry.getBoundingBox());
            }

            ArrayList<BoundingBox> boundingBoxesB = new ArrayList<>();
            for (Entry entry : entriesGroupB) {
                boundingBoxesB.add(entry.getBoundingBox());
            }

            double areaA = BoundingBox.calculateMBR(boundingBoxesA).calculateArea();
            double areaB = BoundingBox.calculateMBR(boundingBoxesB).calculateArea();
            return areaA + areaB;
        }
    }

    /**
     * Class used to store the chosen distributions (splits) of entries based on the optimal split axis.
     */
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

    /**
     * Determines the axis (dimension) of the node's entries where the split will be performed.
     * @return an AxisDistributions object containing the chosen distributions (splits) of entries based on
     * the optimal split axis.
     */
    private AxisDistributions chooseSplitAxis() {
        double minMarginSum = Double.MAX_VALUE;
        AxisDistributions minAxisDistributions = null;

        for (int d = 0; d < DIMENSIONS; d++) {
            ArrayList<Entry> sortedByLowerValue = new ArrayList<>(entries);
            sortedByLowerValue.sort(new EntryComparator.LowerValueComparator(d));
            ArrayList<Entry> sortedByUpperValue = new ArrayList<>(entries);
            sortedByUpperValue.sort(new EntryComparator.UpperValueComparator(d));

            ArrayList<ArrayList<Entry>> sortedValueLists = new ArrayList<>();
            sortedValueLists.add(sortedByLowerValue);
            sortedValueLists.add(sortedByUpperValue);

            AxisDistributions axisDistributions = new AxisDistributions();
            for (ArrayList<Entry> sortedValueList : sortedValueLists) {
                for (int k = 0; k < MAX_ENTRIES - 2 * MIN_ENTRIES + 2; k++) {
                    List<Entry> groupA = sortedValueList.subList(0, MIN_ENTRIES - 1 + k + 1);
                    List<Entry> groupB = sortedValueList.subList(MIN_ENTRIES - 1 + k + 1, sortedValueList.size());
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


    /**
     * Given the optimal AxisDistributions for the node's entries that need to be split, this method determines
     * the optimal split index of the node's entries list.
     * @param chosenAxisDistributions the optimal AxisDistributions that were determined for the node's entries.
     * @return the final node entries' distribution based on the determined optimal split index.
     */
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

    public String toString()
    {
        return "Node(" + "nodeId(" + nodeId + "), entries(" + entries + "), level(" + level + "))";

    }

    @Override
    public byte[] toBytes()
    {
        byte[] nodeAsBytes = new byte[BYTES],
                entriesAsBytes = entriesToBytes(entries);
        int destPos = 0;

        // Node Id
        System.arraycopy(longToBytes(nodeId), 0, nodeAsBytes, destPos, Long.BYTES);
        destPos += Long.BYTES;
        // Node entries
        System.arraycopy(entriesAsBytes, 0, nodeAsBytes, destPos, entriesAsBytes.length);
        destPos += entriesAsBytes.length;
        // Level of the node
        System.arraycopy(intToBytes(level), 0, nodeAsBytes, destPos, Integer.BYTES);

        return nodeAsBytes;
    }

    public static Node fromBytes(byte[] bytes)
    {
        byte[] idAsBytes = new byte[Long.BYTES],
                entriesAsBytes = new byte[Integer.BYTES + 1 + (IndexMetaData.maxEntriesInNode + 1) * Entry.BYTES],
                levelAsBytes = new byte[Integer.BYTES];
        int srcPos = 0;

        System.arraycopy(bytes, srcPos, idAsBytes, 0, idAsBytes.length);
        srcPos += idAsBytes.length;
        System.arraycopy(bytes, srcPos, entriesAsBytes, 0, entriesAsBytes.length);
        srcPos += entriesAsBytes.length;
        System.arraycopy(bytes, srcPos, levelAsBytes, 0, levelAsBytes.length);

        long id = bytesToLong(idAsBytes);
        ArrayList<Entry> entries = entriesFromBytes(entriesAsBytes);
        int level = bytesToInt(levelAsBytes);

        return entries == null ? new Node(level, id) : new Node(entries, level, id);
    }
}
