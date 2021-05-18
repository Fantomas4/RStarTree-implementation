package utils;

public class DataMetaData {
        private static final int maxRecordsInBlock = 2; // Dummy maximum number of records in a block
        private static int numberOfBlocks = 1;

        public static int getMaxRecordsInBlock()
        {
                // TODO: Needs to be calculated
                return maxRecordsInBlock;
        }

        public static int getNumberOfBlocks()
        {
                return numberOfBlocks;
        }

        public static void addOneBlock()
        {
                numberOfBlocks++;
        }
}
