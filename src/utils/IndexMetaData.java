package utils;

import tree.Node;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IndexMetaData {
        private static int numOfNodes = 0;
        protected static long rootNodeId = 1;
        protected static long nextAvailableNodeId = 2;

        public static final int MAX_ENTRIES_IN_NODE = 3;
        // (numOfNodes, rootNodeId, nextAvailableNodeId)
        public static final int BYTES = Integer.BYTES + Long.BYTES + Long.BYTES;

        public static void addOneNode() { numOfNodes++; }
        public static int getNumOfNodes() { return numOfNodes; }
        public static long getNextAvailableNodeId()
        {
                return nextAvailableNodeId++;
        }

        public static void write()
        {
                byte[] node = new byte[Node.BYTES];
                System.arraycopy(toBytes(), 0, node, 0, BYTES);
                try {
                        RandomAccessFile raf = new RandomAccessFile(FileHandler.INDEX_FILE_NAME, "rw");
                        raf.seek(0);
                        raf.write(node);
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static void read()
        {
                byte[] indexMetaDataAsBytes = new byte[BYTES];
                try {
                        RandomAccessFile raf = new RandomAccessFile(FileHandler.INDEX_FILE_NAME, "r");
                        raf.seek(0);
                        raf.readFully(indexMetaDataAsBytes);
                } catch (IOException e) {
                        e.printStackTrace();
                }
                fromBytes(indexMetaDataAsBytes);
        }

        public static byte[] toBytes()
        {
                byte[] indexMetaDataAsBytes = new byte[BYTES],
                        numOfNodesAsBytes = ByteConvertible.intToBytes(numOfNodes),
                        rootNodeIdAsBytes = ByteConvertible.longToBytes(rootNodeId),
                        nextAvailableNodeIdAsBytes = ByteConvertible.longToBytes(nextAvailableNodeId);
                int destPos = 0;

                System.arraycopy(numOfNodesAsBytes, 0, indexMetaDataAsBytes, destPos, Integer.BYTES);
                destPos += Integer.BYTES;
                System.arraycopy(rootNodeIdAsBytes, 0, indexMetaDataAsBytes, destPos, Long.BYTES);
                destPos += Long.BYTES;
                System.arraycopy(nextAvailableNodeIdAsBytes, 0, indexMetaDataAsBytes, destPos, Long.BYTES);

                return indexMetaDataAsBytes;
        }

        public static void fromBytes(byte[] bytes)
        {
                byte[] numOfNodesAsBytes = new byte[Integer.BYTES],
                        rootNodeIdAsBytes = new byte[Long.BYTES],
                        nextAvailableNodeIdAsBytes = new byte[Long.BYTES];
                int srcPos = 0;

                System.arraycopy(bytes, srcPos, numOfNodesAsBytes, 0, numOfNodesAsBytes.length);
                srcPos += numOfNodesAsBytes.length;
                System.arraycopy(bytes, srcPos, rootNodeIdAsBytes, 0, rootNodeIdAsBytes.length);
                srcPos += rootNodeIdAsBytes.length;
                System.arraycopy(bytes, srcPos, nextAvailableNodeIdAsBytes, 0, nextAvailableNodeIdAsBytes.length);

                numOfNodes = ByteConvertible.bytesToInt(numOfNodesAsBytes);
                rootNodeId = ByteConvertible.bytesToLong(rootNodeIdAsBytes);
                nextAvailableNodeId = ByteConvertible.bytesToLong(nextAvailableNodeIdAsBytes);
        }
}
