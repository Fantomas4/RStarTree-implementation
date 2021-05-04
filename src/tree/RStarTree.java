package tree;

import utils.EntryComparator;

import java.util.ArrayList;
import java.util.Collections;

public class RStarTree {
    private long rootNodeId;
    private int leafLevel;

    public RStarTree() {
        leafLevel = 0;
    }

    private Node chooseSubTree(Entry newEntry, Node currentNode, int currentLevel) {
        if (currentLevel == leafLevel) {
            // currentNode is a leaf node (contains tree.LeafEntry objects)
            return currentNode;
        } else if (currentLevel + 1 == leafLevel) {
            // The childpointers in currentNode point to leaves, so the
            // minimum overlap cost is calculated
            ArrayList<Entry> candidateEntries = currentNode.getEntries();
            Entry optimalEntry = Collections.min(candidateEntries,
                    new EntryComparator.OverlapEnlargementComparator(candidateEntries, newEntry));
            long nextNodeId = optimalEntry.getChildNodeId();
            // TODO: Add call to FileHandler method to get the next node (optimalEntry.getChildNodeId())
            Node nextNode;
            return chooseSubTree(newEntry, nextNode, ++currentLevel);
        } else {
            // The childpointers in currentNode do not point to leaves, so the
            // minimum area cost is calculated.
            ArrayList<Entry> candidateEntries = currentNode.getEntries();
            Entry optimalEntry = Collections.min(candidateEntries,
                    new EntryComparator.AreaEnlargementComparator(candidateEntries, newEntry));
            long nextNodeId = optimalEntry.getChildNodeId();
            // TODO: Add call to FileHandler method to get the next node (optimalEntry.getChildNodeId())
            Node nextNode;
            return chooseSubTree(newEntry, nextNode, ++currentLevel);
        }
    }

    private void insertRecord() {
        // R* Tree paper reference: ID1 - InsertData
    }

    private void insert() {
        // R* Tree paper reference: I1 - Insert
    }

    private void overflowTreatment() {

    }

    private void reInsertEntries() {
        // R* Tree paper reference: RI - ReInsert
    }
}
