package utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tree.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;

/**
 * Contains functionality for reading from and writing to ROM
 */
public class FileHandler {

        public static final String DATA_FILE_NAME = "datafile.dat";
        public static final String INDEX_FILE_NAME = "indexfile.dat";
        public static final String OSM_FILE_PATH = "map.osm";
        public static final int DIMENSIONS = 2;
        public static final int BLOCK_SIZE = Integer.BYTES + 2 * Record.BYTES; // 32 * 1024

        /**
         * Initialises the .dat files
         */
        public static void init()
        {
                File indexfile = new File(INDEX_FILE_NAME),
                        datafile = new File(DATA_FILE_NAME);
                indexfile.delete();
                datafile.delete();

                DataMetaData.write();
                IndexMetaData.write();
        }

        /**
         * Writes a new Node to the index file
         * @param newNode to be written
         */
        public static void insertNode(Node newNode)
        {
                try {
                        FileOutputStream fos = new FileOutputStream(INDEX_FILE_NAME, true);
                        fos.write(newNode.toBytes());
                        IndexMetaData.addOneNode();
                } catch (IOException e) {
                        e.printStackTrace();
                }
                IndexMetaData.write();
        }

        /**
         * Reads a Node from the index file
         * @param nodeId of the Node to be read
         * @return the node with id nodeId if present or null if it was not found
         */
        public static Node getNode(long nodeId)
        {
                IndexMetaData.read();
                byte[] nodeAsBytes = new byte[Node.BYTES];
                Node node;
                try {
                        RandomAccessFile raf = new RandomAccessFile(INDEX_FILE_NAME, "r");
                        for (long i = 1; i <= IndexMetaData.getNumOfNodes(); ++i)
                        {
                                raf.seek(i * Node.BYTES);
                                raf.readFully(nodeAsBytes);

                                node = Node.fromBytes(nodeAsBytes);
                                if (node.getId() == nodeId)
                                {
                                        return node;
                                }
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return null;
        }

        /**
         * Changes a nodes data in the index file
         * @param updatedNode the updated version of the Node
         */
        public static void updateNode(Node updatedNode)
        {
                IndexMetaData.read();
                byte[] nodeAsBytes = new byte[Node.BYTES];
                Node node;
                try {
                        RandomAccessFile raf = new RandomAccessFile(INDEX_FILE_NAME, "rw");
                        for (long i = 1; i <= IndexMetaData.getNumOfNodes(); ++i)
                        {
                                raf.seek(i * Node.BYTES);
                                raf.readFully(nodeAsBytes);

                                node = Node.fromBytes(nodeAsBytes);
                                if (node.getId() == updatedNode.getId())
                                {
                                        raf.seek(i * Node.BYTES);
                                        raf.write(updatedNode.toBytes());
                                }
                        }
                        raf.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        /**
         * Inserts a new root Node and updates its id in the index metadata
         * @param newRootNode the new root Node
         */
        public static void setRootNode(Node newRootNode)
        {
                insertNode(newRootNode);
                IndexMetaData.rootNodeId = newRootNode.getId();
                IndexMetaData.write();
        }

        /**
         *
         * @return the root Node
         */
        public static Node getRootNode()
        {
                IndexMetaData.read();
                return getNode(IndexMetaData.rootNodeId);
        }

        /**
         *
         * @return the root Nodes id
         */
        public static long getRootNodeId()
        {
                IndexMetaData.read();
                return IndexMetaData.rootNodeId;
        }


        /**
         * Inserts given records into a block of memory and writes the block to the data file
         * @param records to be written in the data file
         */
        private static void writeDataBlock(ArrayList<Record> records)
        {
                if (records.size() > DataMetaData.MAX_RECORDS_IN_BLOCK)
                {
                        throw new IllegalArgumentException("records array doesn't fit in block");
                }
                DataMetaData.read();
                int numberOfRecords = records.size();

                byte[] block = new byte[BLOCK_SIZE];
                int destPos = 0;

                System.arraycopy(ByteConvertible.intToBytes(numberOfRecords), 0, block, destPos, Integer.BYTES);
                destPos += Integer.BYTES;

                for (Record record : records)
                {
                        System.arraycopy(record.toBytes(), 0, block, destPos, Record.BYTES);
                        destPos += Record.BYTES;
                }

                try {
                        RandomAccessFile raf = new RandomAccessFile(DATA_FILE_NAME, "rw");

                        raf.seek(DataMetaData.getNumberOfBlocks() * BLOCK_SIZE);
                        raf.write(block);

                        DataMetaData.addOneBlock();
                        DataMetaData.write();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        /**
         *
         * @param blockId of the block to be read
         * @return list of records in the block with the given id
         */
        public static ArrayList<Record> getDataBlock(long blockId)
        {
                DataMetaData.read();
                byte[] block = new byte[BLOCK_SIZE];
                try {
                        RandomAccessFile raf = new RandomAccessFile(DATA_FILE_NAME, "r");
                        raf.seek(blockId * BLOCK_SIZE);
                        raf.readFully(block);
                } catch (IOException e) {
                        e.printStackTrace();
                }
                byte[] numberOfRecordsAsBytes = new byte[Integer.BYTES];
                int srcPos = 0;
                System.arraycopy(block, srcPos, numberOfRecordsAsBytes, 0, Integer.BYTES);
                srcPos += Integer.BYTES;
                int numberOfRecords = ByteConvertible.bytesToInt(numberOfRecordsAsBytes);
                ArrayList<Record> records = new ArrayList<>();
                for (int i = 0; i < numberOfRecords; ++i)
                {
                        byte[] recordAsBytes = new byte[Record.BYTES];
                        System.arraycopy(block, srcPos, recordAsBytes, 0, Record.BYTES);
                        srcPos += Record.BYTES;
                        records.add(Record.fromBytes(recordAsBytes));
                }
                return records;
        }

        /**
         * Reads a single Record in the data file
         * @param blockId of the block containing the Record
         * @param recordId of the Record
         * @return the Record with the given record id
         */
        public static Record getRecord(long blockId, long recordId)
        {
                for (Record record : getDataBlock(blockId))
                {
                        if (record.getId() == recordId)
                        {
                                return record;
                        }
                }
                return null;
        }

        /**
         * Parses the XML file and extracts Records containing an id, a name and lat, long attributes.
         * The Records are written in the data file.
         */
        public static void loadDatafile()
        {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                ArrayList<Record> records = new ArrayList<>();
                Record record;
                try {
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document doc = builder.parse(OSM_FILE_PATH);
                        // Get all <node> elements from osm file
                        NodeList nodeList = doc.getElementsByTagName("node");
                        for (int i = 0; i < nodeList.getLength(); ++i)
                        {
                                org.w3c.dom.Node node = nodeList.item(i);
                                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                                {
                                        Element nodeElement = (Element) node;
                                        NodeList tagList = nodeElement.getChildNodes();
                                        for (int j = 0; j < tagList.getLength(); ++j)
                                        {
                                                org.w3c.dom.Node tag = tagList.item(j);
                                                if (tag.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                                                {
                                                        Element tagElement = (Element) tag;
                                                        if (tagElement.getAttribute("k").equals("name"))
                                                        {
                                                                long id = Long.parseLong(
                                                                        nodeElement.getAttribute("id"));
                                                                double lat = Double.parseDouble(
                                                                        nodeElement.getAttribute("lat"));
                                                                double lon = Double.parseDouble(
                                                                        nodeElement.getAttribute("lon"));
                                                                String name = tagElement.getAttribute("v");
                                                                record = new Record(id, name, new double[]{lat, lon});
                                                                records.add(record);
                                                                DataMetaData.addOneRecord();
                                                                DataMetaData.write();
                                                        }
                                                        if (records.size() == DataMetaData.MAX_RECORDS_IN_BLOCK)
                                                        {
                                                                writeDataBlock(records);
                                                                records.clear();
                                                        }
                                                }
                                        }
                                }
                        }
                } catch (ParserConfigurationException | SAXException | IOException e) {
                        e.printStackTrace();
                }
        }
}

