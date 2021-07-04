package utils;

public class IndexMetaData {
        private static int numOfNodes = 0;
        protected static long rootNodeId = 1;
        protected static long nextAvailableNodeId = 2;
        public static final int maxEntriesInNode = 3;
        // (numOfNodes, rootNodeId, nextAvailableNodeId)
        public static final int BYTES = Integer.BYTES + Long.BYTES + Long.BYTES;

        public static void addOneNode() { numOfNodes++; }
        public static int getNumOfNodes() { return numOfNodes; }
        public static long getNextAvailableNodeId()
        {
                return nextAvailableNodeId++;
        }
}
