package tree;

import queries.LocationQueryResult;
import queries.TreeNNQuery;
import queries.TreeRangeQuery;
import tree.comparators.AreaEnlargementComparator;
import tree.comparators.BBCenterDistanceComparator;
import tree.comparators.OverlapEnlargementComparator;
import utils.DataMetaData;
import utils.FileHandler;
import java.util.*;

/**
 * Class used to create RStarTree data structure instances.
 */
public class RStarTree {
    private static final double REINSERT_P_PARAMETER = 0.3;
    private static final int REINSERT_AMOUNT = (int) Math.round(REINSERT_P_PARAMETER * Node.getMaxEntriesLimit());
    private static final int LEAF_LEVEL = 0;

    private int rootLevel;
    boolean[] levelOverflowCalled;
    Queue<RIEntry> reInsertQueue;


    private class RIEntry {
        private final Entry entry;
        private final int insertionLevel;

        public RIEntry(Entry entry, int level) {
            this.entry = entry;
            insertionLevel = level;
        }

        public Entry getEntry() {
            return entry;
        }

        public int getInsertionLevel() {
            return insertionLevel;
        }
    }


    public RStarTree() {
        FileHandler.deleteIndexAndDataFile();
        FileHandler.loadDatafile();

        rootLevel = 0;

        // Create root node
        long rootNodeId = FileHandler.getRootNodeId(); //TODO: Get root Node ID from File Handler. CHECK!
        Node rootNode = new Node(rootLevel, rootNodeId);
        FileHandler.setRootNode(rootNode); // TODO: Save root node using File Handler. CHECK!

        long numBlocks = DataMetaData.getNumberOfBlocks();

        int dRecordsCount = 0;

        for (int i = 1; i < numBlocks; i++) {
            ArrayList<Record> blockRecords = FileHandler.getDataBlock(i);
            for (Record record : blockRecords) {
                insertRecord(record, i);
                dRecordsCount ++;
            }
        }

        System.out.println("Number of records inserted: " + dRecordsCount);
    }

    public static int getLeafLevel() {
        return LEAF_LEVEL;
    }

    public int getTreeHeight() {
        return rootLevel;
    }

    public int getTreeLevels() {
        return getTreeHeight() + 1;
    }

    /**
     * Used to find the optimal insertion path to a specified tree level for a new entry, by choosing the optimal entry
     * from the node being examined.
     * @param newEntry the new entry for which the optimal insertion path must be found.
     * @param currentNode the node that is to be processed in order to find its optimal entry that the insertion
     *                    of the new entry will follow.
     * @param targetLevel the new entry's desired insertion tree level.
     * @return the optimal entry for the insertion path of newEntry at the current tree level.
     */
    private Entry chooseSubTree(Entry newEntry, Node currentNode, int targetLevel) {
        ArrayList<Entry> candidateEntries = currentNode.getEntries();

        if (currentNode.getLevel() - 1 == targetLevel) {
            // The childpointers in currentNode point to nodes located at the target level,
            // so the minimum overlap cost is calculated
            return Collections.min(candidateEntries,
                    new OverlapEnlargementComparator(candidateEntries, newEntry));
        } else {
            // The childpointers in currentNode do not point to nodes located at the target level,
            // so the minimum area cost is calculated.
            return Collections.min(candidateEntries,
                    new AreaEnlargementComparator(candidateEntries, newEntry));
        }
    }

    /**
     * Used to insert a new record into the tree structure.
     * @param newRecord the new record that is to be inserted into the tree structure.
     * @param blockId the unique ID of the datafile block where the new record is saved.
     */
    private void insertRecord(Record newRecord, int blockId) {
        // R* Tree paper reference: ID1 - InsertData
        // Create a new LeafEntry for the record

        BoundingBox newBoundingBox = new BoundingBox(newRecord.getCoordinates(), newRecord.getCoordinates());
        LeafEntry leafEntry = new LeafEntry(newBoundingBox, newRecord.getId(), blockId);

        // Reset the level overflow call status HashMap.
        // (A boolean array indicating whether Overflow Treatment has been called
        // for a specific level of the RStar Tree during the insertion of a new
        // Record).
        levelOverflowCalled = new boolean[getTreeLevels()];
        reInsertQueue = new LinkedList<>();

        // Insert the new Leaf Entry into the tree
        insert(leafEntry, null, null, LEAF_LEVEL);

        // Re-insert all entries that have been added to the reinsert queue
        int reInsertSize = reInsertQueue.size();
        for (int i = 0; i < reInsertSize; i++) {
            RIEntry riEntry = reInsertQueue.remove();

            if (riEntry.getInsertionLevel() == LEAF_LEVEL) {
                //TODO: CHECK Type casting!
                insert((LeafEntry)riEntry.getEntry(), null, null, LEAF_LEVEL);
            } else {
                insert(riEntry.getEntry(), null, null, riEntry.getInsertionLevel());
            }
        }

    }

    /**
     * Recursive method used to perform the insertion of a new entry into the tree structure.
     * @param newEntry the entry that is to be inserted into the tree structure.
     * @param parentNode the parent node currently being processed.
     * @param parentEntry the parent node's entry currently being processed.
     * @param targetLevel the tree level where the new entry is to be placed.
     */
    private void insert(Entry newEntry, Node parentNode, Entry parentEntry, int targetLevel) {
        Node currentNode;

        if (parentEntry == null) {
            // The insertion begins from the root node.
            currentNode = FileHandler.getRootNode();
        } else {
            currentNode = FileHandler.getNode(parentEntry.getChildNodeId());
        }

        if (currentNode.getLevel() == targetLevel) {
            // The target level has been reached, so newEntry is inserted into currentNode.
            currentNode.addEntry(newEntry);
        } else {
            // The target level has not been reached, so the optimal insertion path for the next lower tree level
            // is determined and insert() is called recursively.
            Entry chosenEntry = chooseSubTree(newEntry, currentNode, targetLevel);
            insert(newEntry, currentNode, chosenEntry, targetLevel);
        }

        if (currentNode.isOverflowed()) {
            overflowTreatment(currentNode, parentNode, parentEntry);
        }

        // Update tree structure
        FileHandler.updateNode(currentNode);

        if (parentEntry != null) {
            parentEntry.adjustBoundingBox(currentNode);
            FileHandler.updateNode(parentNode);
        }



    }

    /**
     * Used to handle an overflowed node by either reinserting some of its entries to the tree structure
     * or splitting it into 2 new nodes.
     * @param overflowedNode the overflowed node that needs to be processed.
     * @param parentNode the parent node of the overflowed node.
     * @param parentEntry the entry inside the parent node that points to the overflowed node.
     * @return null if reinsertion was chosen to mitigate the overflowed node, or an ArrayList containing
     * the 2 new nodes the overflowed node was split into.
     */
    private void overflowTreatment(Node overflowedNode, Node parentNode, Entry parentEntry) {
        int overflowedNodeLevel = overflowedNode.getLevel();

        if (overflowedNodeLevel != rootLevel && !levelOverflowCalled[overflowedNodeLevel]) {
            // If the overflowed Node's level is not the root level and this is the first call
            // of overflowTreatment() in the given level during the insertion of one record,
            // invoke reInsert().

            // Update levelOverflowCalled status
            levelOverflowCalled[overflowedNodeLevel] = true;

            reInsert(overflowedNode, parentNode, parentEntry);
        } else {
            // Invoke splitNode() on the overflowed Node.
            Node splitNode = overflowedNode.splitNode();
            FileHandler.updateNode(overflowedNode);
            FileHandler.insertNode(splitNode);

            if (overflowedNode.getLevel() != rootLevel) {
                // If the overflowed node is not the root, create a new entry in the parent node for the new split node.
                parentNode.addEntry(new Entry(BoundingBox.calculateMBR(splitNode.getEntries()), splitNode.getId()));
                FileHandler.updateNode(parentNode);
            } else {
                // If the overflowed node is the root, create a new root containing the two new split nodes of the old root.
                ArrayList<Entry> newRootEntries = new ArrayList<>();
                newRootEntries.add(new Entry(BoundingBox.calculateMBR(overflowedNode.getEntries()), overflowedNode.getId()));
                newRootEntries.add(new Entry(BoundingBox.calculateMBR(splitNode.getEntries()), splitNode.getId()));

                Node newRootNode = new Node(newRootEntries, ++rootLevel, FileHandler.getNextAvailableNodeId());
                FileHandler.setRootNode(newRootNode);
            }
        }

    }

    /**
     * Used to remove a portion of the overflowed node's entries (REINSERT_AMOUNT) and reinsert them into the tree
     * structure, in order to re-balance the overflowed node.
     * @param overflowedNode the overflowed node.
     * @param parentNode the overflowed node's parent node.
     * @param parentEntry the entry inside the parent node that points to the overflowed node.
     */
    private void reInsert(Node overflowedNode, Node parentNode, Entry parentEntry) {
        // R* Tree paper reference: RI - ReInsert
        // TODO: Verify correctness of implementation (far-reinsert vs close-reinsert)
        BoundingBox overflowedBB = BoundingBox.calculateMBR(overflowedNode.getEntries());

        // Sort the M+1 entries in decreasing order of their rectangle centers' distances from the center of the bounding
        // rectangle of overflowedNode.
        overflowedNode.getEntries().sort(Collections.reverseOrder(new BBCenterDistanceComparator(overflowedBB)));

        // Remove the first p entries from the overflowed Node
        ArrayList<Entry> removedEntries = new ArrayList<>();
        for (int i = 0; i < REINSERT_AMOUNT; i++) {
            removedEntries.add(overflowedNode.getEntries().remove(0));
        }

        FileHandler.updateNode(overflowedNode);// TODO: Update overflowedNode in IndexFile using FileHandler. CHECK!

        // Adjust the parent entry of the updated formerly overflowed node
        parentEntry.adjustBoundingBox(overflowedNode);

        // Update the parent entry's node (parent node) in IndexFile using FileHandler
        FileHandler.updateNode(parentNode);// TODO: Update the parent entry's node (parent node) in IndexFile using FileHandler. CHECK!

        // Starting with the minimum distance stated in the sorting step above (close reinsert), invoke insert() to
        // reinsert the entries.
        Collections.reverse(removedEntries); // Organize the removed entries in an ascending order based on distance.
        for (Entry removedEntry : removedEntries) {
            reInsertQueue.add(new RIEntry(removedEntry, overflowedNode.getLevel()));
        }
    }

    /** Used to instantiate a RangeQuery object and execute a range query for a given point in a specified range.
     * @param targetPoint the point for which the range query is to be executed.
     * @param range the range of the range query.
     * @return an ArrayList that contains LocationQueryResult objects representing the range query's results.
     */
    public ArrayList<LocationQueryResult> executeRangeQuery(double[] targetPoint, double range) {
        Node rootNode = FileHandler.getRootNode(); // TODO: Get root node from File Handler. CHECK!
        TreeRangeQuery rangeQuery = new TreeRangeQuery(targetPoint, range, rootNode);

        return rangeQuery.execute();
    }

    /**
     * Used to instantiate an NNQuery object and execute a k-nearest neighbors query for a given point, using a specified
     * "k" value.
     * @param targetPoint the point for which the NN query is to be executed.
     * @param k the specified "k" value.
     * @return an ArrayList that contains LocationQueryResult objects representing the NN query's results.
     */
    public ArrayList<LocationQueryResult> executeNNQuery(double[] targetPoint, int k) {
        Node rootNode = FileHandler.getRootNode(); // TODO: Get root node from File Handler. CHECK!
        TreeNNQuery nnQuery = new TreeNNQuery(targetPoint, k, rootNode);

        return nnQuery.execute();
    }
}
