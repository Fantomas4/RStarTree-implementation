package tree;

import utils.EntryComparator;

import java.util.ArrayList;
import java.util.Collections;

public class RStarTree {
    private final double REINSERT_P_PARAMETER = 0.3;
    private final int REINSERT_AMOUNT = (int) Math.round(REINSERT_P_PARAMETER * Node.getMaxEntriesLimit());

    private long rootNodeId;
    private int leafLevel;

    public RStarTree() {
        leafLevel = 0;
    }

    private Node chooseSubTree(Entry newEntry, Node currentNode, int targetLevel) {
        if (currentNode.getLevel() == targetLevel) {
            // currentNode is the target node (is located at the target level)
            return currentNode;
        } else if (currentNode.getLevel() + 1 == targetLevel) {
            // The childpointers in currentNode point to nodes located at the target level,
            // so the minimum overlap cost is calculated
            ArrayList<Entry> candidateEntries = currentNode.getEntries();
            Entry optimalEntry = Collections.min(candidateEntries,
                    new EntryComparator.OverlapEnlargementComparator(candidateEntries, newEntry));
            long nextNodeId = optimalEntry.getChildNodeId();
            // TODO: Add call to FileHandler method to get the next node (optimalEntry.getChildNodeId())
            Node nextNode;
            return chooseSubTree(newEntry, nextNode, targetLevel);
        } else {
            // The childpointers in currentNode do not point to nodes located at the target level,
            // so the minimum area cost is calculated.
            ArrayList<Entry> candidateEntries = currentNode.getEntries();
            Entry optimalEntry = Collections.min(candidateEntries,
                    new EntryComparator.AreaEnlargementComparator(candidateEntries, newEntry));
            long nextNodeId = optimalEntry.getChildNodeId();
            // TODO: Add call to FileHandler method to get the next node (optimalEntry.getChildNodeId())
            Node nextNode;
            return chooseSubTree(newEntry, nextNode, targetLevel);
        }
    }

    private void insertRecord() {
        // R* Tree paper reference: ID1 - InsertData
        boolean[] levelOverflowCalled = new boolean[leafLevel];

    }

    private void insert(Entry newEntry, int targetLevel, boolean[] levelOverflowCalled) {
        // R* Tree paper reference: I1 - Insert

        Node rootNode; // TODO: Add call to FileHandler method to get the root node
        Node insertionNode = chooseSubTree(newEntry, rootNode, targetLevel);
        insertionNode.addEntry(newEntry); // Try adding the new Entry to the insertion Node.

        if (insertionNode.isOverflowed()) {
            // After the insertion, the node contains M + 1 entries and is overflowed,
            // so OverFlowTreatment is called.
            overflowTreatment(insertionNode, levelOverflowCalled[insertionNode.getLevel()]);

        }

    }

    private void overflowTreatment(Node overflowedNode, Entry parentEntry, boolean isFirstCall) {
        if (overflowedNode.getLevel() > 0 && isFirstCall) {
            // If the overflowed Node's level is not the root level and this is the first call
            // of overflowTreatment() in the given level during the insertion of one record,
            // invoke reInsert().



        } else {
            // Invoke splitNode() on the overflowed Node.



        }
    }

    private void reInsert(Node overflowedNode) {
        // R* Tree paper reference: RI - ReInsert
        ArrayList<Entry> sortedEntries = Collections.sort(new EntryComparator.BBCenterDistanceComparator(overflowedNode)
    }
}
