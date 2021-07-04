package utils;

import tree.Record;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DataMetaData {
        public static final int MAX_RECORDS_IN_BLOCK = FileHandler.BLOCK_SIZE / Record.BYTES;

        private static long numberOfBlocks = 1;
        private static long numberOfRecords = 0;

        public static final int BYTES = Long.BYTES + Long.BYTES;

        public static long getNumberOfBlocks() { return numberOfBlocks; }
        public static long getNumberOfRecords() { return numberOfRecords; }
        public static void addOneBlock() { numberOfBlocks++; }
        public static void addOneRecord() { numberOfRecords++; }

        public static void write()
        {
                byte[] block = new byte[FileHandler.BLOCK_SIZE];
                System.arraycopy(toBytes(), 0, block, 0, BYTES);
                try {
                        RandomAccessFile raf = new RandomAccessFile(FileHandler.DATA_FILE_NAME, "rw");
                        raf.seek(0);
                        raf.write(block);
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static void read()
        {
                byte[] dataMetaDataAsBytes = new byte[BYTES];
                try {
                        RandomAccessFile raf = new RandomAccessFile(FileHandler.DATA_FILE_NAME, "r");
                        raf.seek(0);
                        raf.readFully(dataMetaDataAsBytes);
                } catch (IOException e) {
                        e.printStackTrace();
                }
                fromBytes(dataMetaDataAsBytes);
        }


        public static byte[] toBytes()
        {
                byte[] dataMetaDataAsBytes = new byte[BYTES],
                        numberOfBlocksAsBytes = ByteConvertible.longToBytes(numberOfBlocks),
                        numberOfRecordsAsBytes = ByteConvertible.longToBytes(numberOfRecords);
                int destPos = 0;

                System.arraycopy(numberOfBlocksAsBytes, 0, dataMetaDataAsBytes, destPos, Long.BYTES);
                destPos += Long.BYTES;
                System.arraycopy(numberOfRecordsAsBytes, 0, dataMetaDataAsBytes, destPos, Long.BYTES);

                return dataMetaDataAsBytes;
        }

        public static void fromBytes(byte[] bytes)
        {
                byte[] numberOfBlocksAsBytes = new byte[Long.BYTES],
                        numberOfRecordsAsBytes = new byte[Long.BYTES];
                int srcPos = 0;

                System.arraycopy(bytes, srcPos, numberOfBlocksAsBytes, 0, Long.BYTES);
                srcPos += Long.BYTES;
                System.arraycopy(bytes, srcPos, numberOfRecordsAsBytes, 0, Long.BYTES);

                numberOfBlocks = ByteConvertible.bytesToLong(numberOfBlocksAsBytes);
                numberOfRecords = ByteConvertible.bytesToLong(numberOfRecordsAsBytes);
        }
}
