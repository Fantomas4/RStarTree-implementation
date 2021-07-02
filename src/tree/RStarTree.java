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

        // Insert the new Leaf Entry into the tree
        insert(leafEntry, null, null, LEAF_LEVEL);

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
            // Load the root node to currentNode
            currentNode = FileHandler.getRootNode(); // TODO: Get the root node from File Handler. Check!
        } else {
            currentNode = FileHandler.getNode(parentEntry.getChildNodeId());
        }

        if (currentNode.getLevel() == targetLevel) {
            // The target level has been reached, so newEntry is added to currentNode
            currentNode.addEntry(newEntry);

            FileHandler.updateNode(currentNode); // TODO: Update childNode in index file using File Handler. CHECK!

            if (parentEntry != null) {
                // Adjust the bounding box of the parent Entry so that it's a minimum bounding box enclosing
                // the child entries (nodeA entries) inside its child node (nodeA).
                parentEntry.adjustBoundingBox(currentNode);
                FileHandler.updateNode(parentNode); // TODO: Update parent Node in index file using File Handler. CHECK!

            }

        } else {
            // Continue the recursion to reach the target level, by using chooseSubTree
            // to determine the path that should be followed.
            Entry chosenEntry = chooseSubTree(newEntry, currentNode, targetLevel);
            insert(newEntry, currentNode, chosenEntry, targetLevel);
        }

        if (currentNode.isOverflowed()) {

            // Invoke Overflow Treatment
            Node newSplitNode = overflowTreatment(currentNode, parentNode, parentEntry);

            if (newSplitNode != null) {
                // Overflow Treatment caused a node split
                // Update the child Node reference
                FileHandler.insertNode(newSplitNode); // TODO: Save newSplitNode to index file using File Handler. CHECK!

                if (currentNode.getLevel() != rootLevel) {
                    // If a non-root node was split
                    // Create a new Entry for the second node of the split
                    Entry newParentNodeEntry = new Entry(BoundingBox.calculateMBR(newSplitNode.getEntries()), newSplitNode.getId());
                    // Add the created Entry to the parent Node
                    parentNode.addEntry(newParentNodeEntry);
                } else {
                    // If the root node was split
                    Entry rootEntryA = new Entry(BoundingBox.calculateMBR(currentNode.getEntries()), currentNode.getId());
                    Entry rootEntryB = new Entry(BoundingBox.calculateMBR(newSplitNode.getEntries()), newSplitNode.getId());
                    ArrayList<Entry> rootEntries = new ArrayList<>();
                    rootEntries.add(rootEntryA);
                    rootEntries.add(rootEntryB);

                    // Create the new root entry
                    long newRootNodeId = FileHandler.getNextAvailableNodeId(); // TODO: Get a new node ID for the new root from File Handler. CHECK!
                    Node newRoot = new Node(rootEntries, ++rootLevel, newRootNodeId);
                    FileHandler.setRootNode(newRoot); // TODO: Save the new root Node using File Handler. CHECK!
                }

                FileHandler.updateNode(currentNode); // TODO: Update childNode in index file (as nodeA) using File Handler. CHECK!

                if (parentEntry != null) {
                    // Adjust the bounding box of the parent Entry so that it's a minimum bounding box enclosing
                    // the child entries (nodeA entries) inside its child node (nodeA).
                    parentEntry.adjustBoundingBox(currentNode);
                    FileHandler.updateNode(parentNode); // TODO: Update parent Node in index file using File Handler. CHECK!

                }

            }
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
    private Node overflowTreatment(Node overflowedNode, Node parentNode, Entry parentEntry) {
        int overflowedNodeLevel = overflowedNode.getLevel();
        if (overflowedNode.getLevel() != rootLevel) {
            boolean isFirstCall = !levelOverflowCalled[overflowedNodeLevel];
            // Update levelOverflowCalled status
            levelOverflowCalled[overflowedNodeLevel] = true;

            if (isFirstCall) {
                // If the overflowed Node's level is not the root level and this is the first call
                // of overflowTreatment() in the given level during the insertion of one record,
                // invoke reInsert().
                reInsert(overflowedNode, parentNode, parentEntry);

                return null;
            }
        }

        // Invoke splitNode() on the overflowed Node.
        return overflowedNode.splitNode();
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
        ArrayList<Entry> overflowedNodeEntries = overflowedNode.getEntries();
        BoundingBox overflowedBB = BoundingBox.calculateMBR(overflowedNodeEntries);

        // Sort the M+1 entries in decreasing order of their rectangle centers' distances from the center of the bounding
        // rectangle of overflowedNode.
        overflowedNodeEntries.sort(Collections.reverseOrder(new BBCenterDistanceComparator(overflowedBB)));

        // Remove the first p entries from the overflowed Node
        ArrayList<Entry> removedEntries = new ArrayList<>();
        for (int i = 0; i < REINSERT_AMOUNT; i++) {
            removedEntries.add(overflowedNodeEntries.remove(0));
        }

        // Update the overflowed Node's entries
        overflowedNode.setEntries(overflowedNodeEntries);
        FileHandler.updateNode(overflowedNode);// TODO: Update overflowedNode in IndexFile using FileHandler. CHECK!

        // Adjust the parent entry of the updated formerly overflowed node
        parentEntry.adjustBoundingBox(overflowedNode);

        // Update the parent entry's node (parent node) in IndexFile using FileHandler
        FileHandler.updateNode(parentNode);// TODO: Update the parent entry's node (parent node) in IndexFile using FileHandler. CHECK!

        // Starting with the minimum distance stated in the sorting step above (close reinsert), invoke insert() to
        // reinsert the entries.
        Collections.reverse(removedEntries); // Organize the removed entries in an ascending order based on distance.
        for (Entry removedEntry : removedEntries) {
            insert(removedEntry, null, null, overflowedNode.getLevel());
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
