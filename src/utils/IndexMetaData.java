package utils;

public class IndexMetaData {
        private static int numOfNodes = 0;

        public static void addOneNode() { numOfNodes++; }
        public static int getNumOfNodes() { return numOfNodes; }
}
