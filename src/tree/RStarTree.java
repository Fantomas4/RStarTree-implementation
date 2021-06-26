package tree;

import queries.LocationQueryResult;
import queries.TreeNNQuery;
import queries.TreeRangeQuery;
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

    int debugRecordCounter = 0;
    int debugInsertRecursionCounter = 0;

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

        for (int i = 0; i < numBlocks; i++) {
            ArrayList<Record> blockRecords = FileHandler.getDataBlock(i);
            for (Record record : blockRecords) {
                insertRecord(record, i);
                dRecordsCount ++;
            }
        }

        System.out.println("Number of records inserted: " + dRecordsCount);

//        // FOR TESTING PURPOSES ONLY!
//        double[] coordinates = new double[2];
//
//        coordinates[0] = -100;
//        coordinates[1] = 1;
//        insertRecord(new Record(1, "TR1", coordinates),1);
////
//        coordinates[0] = -80;
//        coordinates[1] = -1;
//        insertRecord(new Record(2, "TR2", coordinates),2);
////
//        coordinates[0] = 4;
//        coordinates[1] = 1;
//        insertRecord(new Record(3, "TR3", coordinates),3);
//
//        coordinates[0] = 5;
//        coordinates[1] = 0;
//        insertRecord(new Record(4, "TR4", coordinates),4);
//
//        coordinates[0] = 14;
//        coordinates[1] = 1;
//        insertRecord(new Record(5, "TR5", coordinates),5);
//
//        coordinates[0] = 2;
//        coordinates[1] = 1;
//        insertRecord(new Record(6, "TR6", coordinates),6);
//
//        coordinates[0] = 2;
//        coordinates[1] = 0.1;
//        insertRecord(new Record(7, "TR7", coordinates),7);
//
//        coordinates[0] = -101;
//        coordinates[1] = 0.1;
//        insertRecord(new Record(8, "TR8", coordinates),8);
////
//        coordinates[0] = -102;
//        coordinates[1] = 0.1;
//        insertRecord(new Record(9, "TR9", coordinates),9);
//
//        coordinates[0] = -125;
//        coordinates[1] = 1;
//        insertRecord(new Record(10, "TR10", coordinates),10);
//
//        coordinates[0] = 9;
//        coordinates[1] = 0.9;
//        insertRecord(new Record(11, "TR11", coordinates),11);
//
//        coordinates[0] = -1;
//        coordinates[1] = 0;
//        insertRecord(new Record(12, "TR12", coordinates),12);
//
//        coordinates[0] = 23;
//        coordinates[1] = 1.7;
//        insertRecord(new Record(13, "TR13", coordinates),13);
//
//        coordinates[0] = 12;
//        coordinates[1] = 10;
//        insertRecord(new Record(14, "TR14", coordinates),14);
//
//        coordinates[0] = 20;
//        coordinates[1] = -2;
//        insertRecord(new Record(15, "TR15", coordinates),15);
//
//        coordinates[0] = 2;
//        coordinates[1] = -0.1;
//        insertRecord(new Record(16, "TR16", coordinates),16);
//
//        coordinates[0] = -1;
//        coordinates[1] = -2;
//        insertRecord(new Record(17, "TR17", coordinates),17);
//
//        coordinates[0] = 1;
//        coordinates[1] = 1;
//        insertRecord(new Record(18, "TR18", coordinates),18);
//
//        coordinates[0] = 15;
//        coordinates[1] = -1;
//        insertRecord(new Record(19, "TR19", coordinates),19);
//
//        coordinates[0] = -136;
//        coordinates[1] = 1;
//        insertRecord(new Record(20, "TR20", coordinates),20);
    }

    public static int getLeafLevel() {
        return LEAF_LEVEL;
    }

    private int getTreeHeight() {
        return rootLevel;
    }

    private int getTreeLevels() {
        return getTreeHeight() + 1;
    }

    /**
     * Used to find the optimal insertion path to a specified tree level for a new entry, by choosing the optimal entry
     * from the node being examined.
     * @param newEntry the new entry for which the optimal insertion path must be found.
     * @param currentNode the node that is to be processed in order to find its optimal entry that the insertion
     *                    of the new entry will follow.
     * @param targetLevel the new entry's desired insertion tree level.
     * @return
     */
    private Entry chooseSubTree(Entry newEntry, Node currentNode, int targetLevel) {
        //        System.out.println("currentNode level: " + currentNode.getLevel());
        if (currentNode.getLevel() - 1 == targetLevel) {
            // The childpointers in currentNode point to nodes located at the target level,
            // so the minimum overlap cost is calculated
            ArrayList<Entry> candidateEntries = currentNode.getEntries();
            return Collections.min(candidateEntries,
                    new EntryComparator.OverlapEnlargementComparator(candidateEntries, newEntry));
        } else {
            // The childpointers in currentNode do not point to nodes located at the target level,
            // so the minimum area cost is calculated.
            ArrayList<Entry> candidateEntries = currentNode.getEntries();
            return Collections.min(candidateEntries,
                    new EntryComparator.AreaEnlargementComparator(candidateEntries, newEntry));
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
        debugRecordCounter++;
        debugInsertRecursionCounter = 0;

        System.out.println("----------------->>>>Insertion " + debugRecordCounter + " -Inserting record: " + newRecord.getId());
        BoundingBox newBoundingBox = new BoundingBox(newRecord.getCoordinates(), newRecord.getCoordinates().clone());
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
        debugInsertRecursionCounter ++;
        Node currentNode;

        if (parentNode == null && parentEntry == null) {
            // Load the root node to currentNode
            currentNode = FileHandler.getRootNode(); // TODO: Get the root node from File Handler. Check!
        } else {
            currentNode = FileHandler.getNode(parentEntry.getChildNodeId());
        }

        if (currentNode.getLevel() == targetLevel) {
            // The target level has been reached, so newEntry is added to currentNode
            currentNode.addEntry(newEntry);

            System.out.println("currentNode update");
            FileHandler.updateNode(currentNode); // TODO: Update childNode in index file (as nodeA) using File Handler. CHECK!

            if (parentEntry != null) {
                // Adjust the bounding box of the parent Entry so that it's a minimum bounding box enclosing
                // the child entries (nodeA entries) inside its child node (nodeA).
                parentEntry.adjustBoundingBox(currentNode);
            }

            if (parentNode != null) {
                if (parentNode.isOverflowed()) {
                    System.out.println("**********************");
                }
                System.out.println("parentNode update");
                FileHandler.updateNode(parentNode); // TODO: Update parent Node in index file using File Handler. CHECK!
            }
        } else {
            // Continue the recursion to reach the target level, by using chooseSubTree
            // to determine the path that should be followed.
            Entry chosenEntry = chooseSubTree(newEntry, currentNode, targetLevel);
            insert(newEntry, currentNode, chosenEntry, targetLevel);
            System.out.println("=======================>>> Returned from recursive insertion " + debugInsertRecursionCounter);

        }

        if (currentNode.isOverflowed()) {
            // Invoke Overflow Treatment
            ArrayList<Node> overflowTreatmentResult = overflowTreatment(currentNode, parentNode, parentEntry);

            if (overflowTreatmentResult != null) {
                // Overflow Treatment caused a node split
                Node nodeA = overflowTreatmentResult.get(0);
                Node nodeB = overflowTreatmentResult.get(1);

                // Update the child Node reference
                currentNode = nodeA;
                FileHandler.insertNode(nodeB); // TODO: Save nodeB to index file using File Handler. CHECK!

                if (currentNode.getLevel() != rootLevel) {
                    // If a non-root node was split
                    // Create a new Entry for the second node of the split
                    Entry newParentNodeEntry = new Entry(BoundingBox.calculateMBR(nodeB.getEntries()), nodeB.getId());
                    // Add the created Entry to the parent Node
                    parentNode.addEntry(newParentNodeEntry);
                } else {
                    // If the root node was split
                    Entry rootEntryA = new Entry(BoundingBox.calculateMBR(nodeA.getEntries()), nodeA.getId());
                    Entry rootEntryB = new Entry(BoundingBox.calculateMBR(nodeB.getEntries()), nodeB.getId());
                    ArrayList<Entry> rootEntries = new ArrayList<>();
                    rootEntries.add(rootEntryA);
                    rootEntries.add(rootEntryB);

                    // Create the new root entry
                    long newRootNodeId = FileHandler.getNextAvailableNodeId(); // TODO: Get a new node ID for the new root from File Handler. CHECK!
                    Node newRoot = new Node(rootEntries, ++rootLevel, newRootNodeId);
                    FileHandler.setRootNode(newRoot); // TODO: Save the new root Node using File Handler. CHECK!
                }

                System.out.println("currentNode update");
                FileHandler.updateNode(currentNode); // TODO: Update childNode in index file (as nodeA) using File Handler. CHECK!

                if (parentEntry != null) {
                    // Adjust the bounding box of the parent Entry so that it's a minimum bounding box enclosing
                    // the child entries (nodeA entries) inside its child node (nodeA).
                    parentEntry.adjustBoundingBox(currentNode);
                }

                if (parentNode != null) {
                    if (parentNode.isOverflowed()) {
                        System.out.println("**********************");
                    }
                    System.out.println("parentNode update");
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
    private ArrayList<Node> overflowTreatment(Node overflowedNode, Node parentNode, Entry parentEntry) {
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
        overflowedNodeEntries.sort(Collections.reverseOrder(new EntryComparator.BBCenterDistanceComparator(overflowedBB)));

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
        System.out.println("Exiting reinsert...");
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
