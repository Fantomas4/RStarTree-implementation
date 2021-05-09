package tree;

import utils.EntryComparator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class RStarTree {
    private final double REINSERT_P_PARAMETER = 0.3;
    private final int REINSERT_AMOUNT = (int) Math.round(REINSERT_P_PARAMETER * Node.getMaxEntriesLimit());
    private final int LEAF_LEVEL = 0;

    private long rootNodeId;
    private int rootLevel;

    public RStarTree() {
        rootLevel = 0;

        // Create root node
        long rootNodeId; //TODO: Get root Node ID from File Handler.
        Node rootNode = new Node(rootLevel, rootNodeId);
        // TODO: Save root node using File Handler.
    }

    private int getTreeHeight() {
        return rootLevel + 1;
    }

    // Returns the bottom-up Entry path that represents the optimal insertion sub-tree. The first Entry
    // of the list returned represents the Entry in the child node of which the new Entry is to be inserted.
    private ArrayList<Entry> chooseSubTree(Entry newEntry, Node currentNode, int targetLevel) {
        if (currentNode.getLevel() - 1 == targetLevel) {
            // The childpointers in currentNode point to nodes located at the target level,
            // so the minimum overlap cost is calculated
            ArrayList<Entry> candidateEntries = currentNode.getEntries();
            Entry optimalEntry = Collections.min(candidateEntries,
                    new EntryComparator.OverlapEnlargementComparator(candidateEntries, newEntry));
            ArrayList<Entry> entriesPath = new ArrayList<>();
            entriesPath.add(optimalEntry);
            return entriesPath;
        } else {
            // The childpointers in currentNode do not point to nodes located at the target level,
            // so the minimum area cost is calculated.
            ArrayList<Entry> candidateEntries = currentNode.getEntries();
            Entry optimalEntry = Collections.min(candidateEntries,
                    new EntryComparator.AreaEnlargementComparator(candidateEntries, newEntry));
            // Get the next Node from the File Handler.
            long nextNodeId = optimalEntry.getChildNodeId();
            // TODO: Add call to FileHandler method to get the next node (optimalEntry.getChildNodeId())
            Node nextNode;
            ArrayList<Entry> entriesPath = chooseSubTree(newEntry, nextNode, targetLevel);
            entriesPath.add(optimalEntry);
            return entriesPath;
        }
    }

    private void insertRecord(Record newRecord, int blockId) {
        // R* Tree paper reference: ID1 - InsertData

        // Create a new LeafEntry for the record
        BoundingBox newBoundingBox = new BoundingBox(newRecord.getCoordinates(), newRecord.getCoordinates());
        LeafEntry leafEntry = new LeafEntry(newBoundingBox, newRecord.getId(), blockId);

        // Insert the new Leaf Entry into the tree
        insert(leafEntry, LEAF_LEVEL);

    }

    private void insert(Entry newEntry, int targetLevel) {
        // R* Tree paper reference: I1 - Insert

        // Reset the level overflow call status list.
        // (A boolean array indicating whether Overflow Treatment has been called
        // for a specific level of the RStar Tree during the insertion of a new
        // Record).
        boolean[] levelOverflowCalled = new boolean[getTreeHeight()];

        // Get the root node from File Handler
        Node rootNode; // TODO: Get the root node from File Handler using rootNodeId
        
        if (rootLevel == LEAF_LEVEL) {
            // If the root Node is the only node, directly insert the new Entry into it.
            rootNode.addEntry(newEntry);
            // TODO: Update root Node using File Handler

        } else {
            // If the RStar Tree has a height equal or greater than 1, invoke chooseSubTree() to
            // get the insertion path for the new Entry.
            ArrayList<Entry> bottomUpPathEntries = chooseSubTree(newEntry, rootNode, targetLevel);

            for (int i = 0; i < bottomUpPathEntries.size(); i++) {
                Entry parentEntry = bottomUpPathEntries.get(i);
                Node parentNode;
                if (i == bottomUpPathEntries.size() - 1) {
                    // The current iteration processes the root node as the Parent Entry.
                    parentNode; // TODO: Get the parent node (root node) from File Handler using rootNodeId
                } else {
                    // If the current iteration does not process the root node as the Parent Entry.
                    // TODO: Get the parent node from File Handler using bottomUpPathEntries.get(i + 1).getChildNodeId()
                    parentNode;
                }
                Node childNode; // TODO: Get the child node from File Handler using parentEntry.getChildNodeId();
                if (childNode.getLevel() == targetLevel) {
                    // If the child Node is the insertion node
                    childNode.addEntry(newEntry);
                }

                if (childNode.isOverflowed()) {
                    // Invoke Overflow Treatment
                    ArrayList<Node> overflowTreatmentResult = overflowTreatment(childNode, levelOverflowCalled);
                    if (overflowTreatmentResult != null) {
                        // Overflow Treatment caused a node split
                        Node nodeA = overflowTreatmentResult.get(0);
                        Node nodeB = overflowTreatmentResult.get(1);

                        // TODO: Update childNode in index file as nodeA using File Handler
                        // Create a new Entry for the second node of the split
                        Entry newParentNodeEntry = new Entry(BoundingBox.calculateMBR(nodeB.getEntries()), nodeB.getId());
                        // Add the created Entry to the parent Node
                        parentNode.addEntry(newParentNodeEntry);
                    }
                }
                // Adjust the bounding box of the parent Entry so that it's a minimum bounding box enclosing
                // the child entries inside its child node.
                parentEntry.adjustBoundingBox(childNode);
            }
        }

        // Check the root Node for Overflow
        if (rootNode.isOverflowed()) {
            // Invoke Overflow Treatment
            ArrayList<Node> overflowTreatmentResult = overflowTreatment(rootNode, levelOverflowCalled);
            if (overflowTreatmentResult != null) {
                // Overflow Treatment caused a root node split, so a new
                // root node has to be created
                Node nodeA = overflowTreatmentResult.get(0);
                Node nodeB = overflowTreatmentResult.get(1);
                Entry rootEntryA = new Entry(BoundingBox.calculateMBR(nodeA.getEntries()), nodeA.getId());
                Entry rootEntryB = new Entry(BoundingBox.calculateMBR(nodeB.getEntries()), nodeB.getId());
                ArrayList<Entry> rootEntries = new ArrayList<>();
                rootEntries.add(rootEntryA);
                rootEntries.add(rootEntryB);

                // Create the new root entry
                // TODO: Get a new node ID for the new root from File Handler
                long newRootNodeId;
                Node newRoot = new Node(rootEntries, newRootNodeId, ++rootLevel);
                // Update the root node ID of RStar Tree.
                rootNodeId = newRootNodeId;
                // TODO: Save the new root Node using File Handler.
            }
        }
    }

    private ArrayList<Node> overflowTreatment(Node overflowedNode, boolean[] levelOverflowCalled) {
        int overflowedNodeLevel = overflowedNode.getLevel();
        boolean isFirstCall = !levelOverflowCalled[overflowedNodeLevel];
        // Update levelOverflowCalled status
        levelOverflowCalled[overflowedNodeLevel] = true;

        if (overflowedNode.getLevel() != rootLevel && isFirstCall) {
            // If the overflowed Node's level is not the root level and this is the first call
            // of overflowTreatment() in the given level during the insertion of one record,
            // invoke reInsert().
            reInsert(overflowedNode);
            return null;
        } else {
            // Invoke splitNode() on the overflowed Node.
            return overflowedNode.splitNode();
        }
    }

    private void reInsert(Node overflowedNode) {
        // R* Tree paper reference: RI - ReInsert
        // TODO: Verify correctness of implementation
        ArrayList<Entry> overflowedNodeEntries = overflowedNode.getEntries();
        BoundingBox overflowedBB = BoundingBox.calculateMBR(overflowedNodeEntries);

        // Sort the M+1 entries in decreasing order of their rectangle centers' distances to the center of the bounding
        // rectangle of overflowedNode.
        overflowedNodeEntries.sort(Collections.reverseOrder(new EntryComparator.BBCenterDistanceComparator(overflowedBB)));

        // Remove the first p entries from the overflowed Node and adjust its bounding box.
        ArrayList<Entry> removedEntries = new ArrayList<>();
        for (int i = 0; i < REINSERT_AMOUNT; i++) {
            removedEntries.add(overflowedNodeEntries.remove(0));
        }

        // Update the overflowed Node's entries
        overflowedNode.setEntries(overflowedNodeEntries);

        // Reinsert the removed entries
        for (Entry removedEntry : removedEntries) {
            insert(removedEntry, overflowedNode.getLevel());
        }

        // TODO: Write the updated (former overflowed) Node to IndexFile using File Handler.
    }
}
