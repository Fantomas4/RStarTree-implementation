package utils;

import tree.Record;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DataMetaData {
        private static final int maxRecordsInBlock = FileHandler.BLOCK_SIZE / Record.BYTES; // Dummy maximum number of records in a block
        private static long numberOfBlocks = 1;
        public static final int BYTES = Long.BYTES;

        public static void init()
        {
                try {
                        RandomAccessFile raf = new RandomAccessFile(FileHandler.DATA_FILE_NAME, "rw");
                        raf.seek(0);
                        raf.write(toBytes());
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static int getMaxRecordsInBlock()
        {
                // TODO: Needs to be calculated
                return maxRecordsInBlock;
        }

        public static long getNumberOfBlocks()
        {
                return numberOfBlocks;
        }

        public static void addOneBlock()
        {
                numberOfBlocks++;
        }

        public static byte[] toBytes()
        {
                byte[] dataMetaDataAsBytes = new byte[BYTES];
                int destPos = 0;

                System.arraycopy(ByteConvertible.longToBytes(numberOfBlocks), 0, dataMetaDataAsBytes, destPos, Long.BYTES);
                destPos += Long.BYTES;

                return dataMetaDataAsBytes;
        }

        public static void fromBytes(byte[] bytes)
        {
                byte[] numberOfBlocksAsBytes = new byte[Long.BYTES];
                int srcPos = 0;

                System.arraycopy(bytes, srcPos, numberOfBlocksAsBytes, 0, Long.BYTES);
                srcPos += Long.BYTES;

                numberOfBlocks = ByteConvertible.bytesToLong(numberOfBlocksAsBytes);
        }
}
