package utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
//import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tree.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;

public class FileHandler {

        private static ArrayList<Node> dummyIndexFile = new ArrayList<>();
        private static ArrayList<Record[]> dummyDataFile = new ArrayList<>();

        private static final int DEBUG_MODE = 0;

        public static final String DATAFILE_NAME = "datafile.dat";
        public static final String INDEXFILE_NAME = "indexfile.dat";

        private static long rootNodeId = 1;
        public static final int DIMENSIONS = 2;

        private static String osmFilePath = "map.osm";
        public static final int BLOCK_SIZE = Integer.BYTES + 2 * Record.BYTES; // 32 * 1024
        private static long nextAvailableNodeId = 2;

        public static final int maxEntriesInNode = 3;

        public static void print_tree()
        {
                ArrayList<Node> new_nodes, nodes = new ArrayList<>();
                nodes.add(getNode(rootNodeId));

                while (true)
                {
                        for (Node node : nodes)
                        {
                                System.out.println(node);
                        }
                        System.out.println("\n************************************************************************");

                        if (nodes.get(0).getEntries().get(0) instanceof LeafEntry)
                        {
                                break;
                        }

                        new_nodes = new ArrayList<>();
                        for (Node node : nodes)
                        {
                                for (Entry entry : node.getEntries())
                                {
                                        new_nodes.add(getNode(entry.getChildNodeId()));
                                }
                        }
                        nodes = new_nodes;
                }
        }


        public static ArrayList<Record[]> getDummyDataFile()
        {
                return dummyDataFile;
        }

        public static ArrayList<Node> getDummyIndexFile()
        {
                return dummyIndexFile;
        }

        // TODO: Make DataFile and IndexFile classes
        public static void deleteIndexAndDataFile()
        {
                File indexfile = new File(INDEXFILE_NAME),
                        datafile = new File(DATAFILE_NAME);
                indexfile.delete();
                datafile.delete();
        }


        public static long getNextAvailableNodeId()
        {
                return nextAvailableNodeId++;
        }

        public static void insertNode(Node newNode)
        {
                try {
                        if (DEBUG_MODE > 1)
                        {
                                System.out.println("Writing to indexfile: " + newNode );
                                for (Node dummyNode : dummyIndexFile)
                                {
                                        if (dummyNode.getId() == newNode.getId())
                                        {
                                                System.out.println("Trying to reinstert existing node: " + dummyNode);
                                        }
                                        for (Entry dummyEntry : dummyNode.getEntries())
                                        {
                                                for (Entry entry : newNode.getEntries())
                                                {
                                                        if (dummyEntry.equals(entry))
                                                        {
                                                                System.out.println("Trying to reinsert existing entry" + entry);
                                                        }
                                                        if (entry.getChildNodeId() != -1 && dummyEntry.getChildNodeId() == entry.getChildNodeId())
                                                        {
                                                                System.out.println("childe node duplicate");
                                                        }
                                                }
                                        }
                                }
                                dummyIndexFile.add(newNode);
                        }
                        FileOutputStream fos = new FileOutputStream(INDEXFILE_NAME, true);
                        fos.write(newNode.toBytes());
                        IndexMetaData.addOneNode();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        // TODO: MIGHT NEED TO LINEAR SEARCH FOR THE RIGHT NODE ID
        public static Node getNode(long nodeId)
        {
                byte[] nodeAsBytes = new byte[Node.BYTES];
                Node node;
                try {
                        RandomAccessFile raf = new RandomAccessFile(INDEXFILE_NAME, "r");
                        for (long i = 0; i < nextAvailableNodeId; ++i)
                        {
                                raf.seek(i * Node.BYTES);
                                raf.readFully(nodeAsBytes);
                                node = Node.fromBytes(nodeAsBytes);
                                if (node.getId() == nodeId)
                                {
                                        if (DEBUG_MODE > 1)
                                        {
                                                System.out.println("Reading node" + nodeId + " from indexfile");
                                                System.out.println("Node" + nodeId + ": " + node);


                                        }
                                        return node;
                                }
                        }

                } catch (IOException e) {
                        e.printStackTrace();
                }
                return null;
        }

        public static void updateNode(Node updatedNode)
        {
                byte[] nodeAsBytes = new byte[Node.BYTES];
                Node node;
                try {
                        RandomAccessFile raf = new RandomAccessFile(INDEXFILE_NAME, "rw");
                        for (long i = 0; i < IndexMetaData.getNumOfNodes(); ++i)
                        {
                                //raf.seek(i * getNodeSizeInBytes());
                                raf.readFully(nodeAsBytes);
                                node = Node.fromBytes(nodeAsBytes);
                                if (node.getId() == updatedNode.getId())
                                {
                                        if (DEBUG_MODE > 1)
                                        {
                                                System.out.println("Updating node" + updatedNode.getId());
                                                System.out.println("Old node" + updatedNode.getId() + ": " + node);
                                                System.out.println("New node" + updatedNode.getId() + ": " + updatedNode);

                                                for (int j = 0; j < dummyIndexFile.size(); ++j)
                                                {
                                                        if (dummyIndexFile.get(j).getId() == updatedNode.getId())
                                                        {
                                                                dummyIndexFile.set(j, updatedNode);
                                                                break;
                                                        }
                                                }
                                        }
                                        raf.seek(i * Node.BYTES); // TODO: May not be needed
                                        raf.write(updatedNode.toBytes());
                                        if (DEBUG_MODE > 1)
                                        {
                                                byte[] nodeWritten = new byte[Node.BYTES];
                                                raf.seek(i * Node.BYTES); // TODO: May not be needed
                                                raf.readFully(nodeWritten);
                                                System.out.println("Node written: " + Node.fromBytes(nodeWritten));
                                        }
                                }
                        }
                        raf.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static void setRootNode(Node newRootNode)
        {
                if (DEBUG_MODE > 1)
                {
                        System.out.println("Setting root node(" + newRootNode.getId() + ")");
                }
                insertNode(newRootNode);
                rootNodeId = newRootNode.getId();
        }

        public static Node getRootNode()
        {
                return getNode(rootNodeId);
        }

        public static long getRootNodeId()
        {
                return rootNodeId;
        }





        private static void writeDataBlock(ArrayList<Record> records)
        {
                if (DEBUG_MODE > 1)
                {
                        System.out.println("Writing data block with records " + records);
                }
                if (records.size() > DataMetaData.getMaxRecordsInBlock())
                {
                        throw new IllegalArgumentException("records array doesn't fit in block");
                }
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
                        RandomAccessFile raf = new RandomAccessFile(DATAFILE_NAME, "rw");

                        raf.seek(0);
                        byte[] dataMetaDataAsBytes = new byte[DataMetaData.BYTES];
                        raf.readFully(dataMetaDataAsBytes);
                        DataMetaData.fromBytes(dataMetaDataAsBytes);

                        raf.seek(DataMetaData.getNumberOfBlocks() * BLOCK_SIZE);
                        raf.write(block);

                        DataMetaData.addOneBlock();

                        raf.seek(0);
                        raf.write(DataMetaData.toBytes());
                } catch (IOException e) {
                        e.printStackTrace();
                }
                if (DEBUG_MODE > 1)
                {
                        System.out.println("Block" + (DataMetaData.getNumberOfBlocks() - 1) + " written successfully");
                        Record[] dummyBlock = new Record[DataMetaData.getMaxRecordsInBlock()];
                        for (int i = 0; i < records.size(); ++i)
                        {
                                dummyBlock[i] = records.get(i);
                        }
                        dummyDataFile.add(dummyBlock);
                }
        }

        public static ArrayList<Record> getDataBlock(long blockId)
        {
                if (DEBUG_MODE > 1)
                {
                        System.out.println("Reading data block" + blockId);
                }
                byte[] block = new byte[BLOCK_SIZE];
                try {
                        RandomAccessFile raf = new RandomAccessFile(DATAFILE_NAME, "r");
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
                if (DEBUG_MODE > 1)
                {
                        System.out.println("block" + blockId + ": " + records);
                }
                return records;
        }

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

        /*
        Extracts Record data (id, name and coordinates) from Open Street Maps XML file and loads them into
        the datafile
        */
        public static void loadDatafile()
        {
                if (DEBUG_MODE > 1)
                {
                        System.out.println("Reading OSM File:");
                }
                DataMetaData.init();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                ArrayList<Record> records = new ArrayList<>();
                Record record;
                try {
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document doc = builder.parse(osmFilePath);
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
                                                                if (DEBUG_MODE > 1)
                                                                {
                                                                        System.out.println("Reading " + record);
                                                                }
                                                        }
                                                        if (records.size() == DataMetaData.getMaxRecordsInBlock())
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



        public static void main(String[] args) throws FileNotFoundException {
                /*
                Record my_record1 = new Record(1, "home", new double[]{1.0, 1.0});
                Record my_record2 = new Record(2, "office", new double[]{2.0, 2.0});
                Record my_record3 = new Record(3, "university", new double[]{3.0, 3.0});
                System.out.println(my_record1);
                System.out.println(my_record2);
                System.out.println(my_record3);
                */
                /*
                loadDatafile();
                ArrayList<Record> out = new ArrayList<>();
                for (int i = 0; i < DataMetaData.getNumberOfBlocks(); ++i)
                {
                        ArrayList<Record> records = getDataBlock(i);
                        for (Record record : records)
                        {
                                out.add(record);
                        }
                }
                for (Record record : out)
                {
                        System.out.println(record);
                }
                System.out.println(out.size());
                */

                /*
                ArrayList<Entry> entries = new ArrayList<>();
                entries.add(new Entry(new BoundingBox(new double[]{0.0, 0.0}, new double[]{1.0, 1.0})));
                entries.add(new LeafEntry(new BoundingBox(new double[]{0.0, 0.0}, new double[]{1.0, 1.0}), 10, 0));
                Node my_node = new Node(entries, 0, getNextAvailableNodeId());
                System.out.println(my_node);

                insertNode(my_node);
                updateNode(new Node(entries, 90000, my_node.getId()));
                Node node = getNode(my_node.getId());
                */


                System.out.println("lets go");
        }
}

