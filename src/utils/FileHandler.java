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

public class FileHandler {

        public static final String DATA_FILE_NAME = "datafile.dat";
        public static final String INDEX_FILE_NAME = "indexfile.dat";
        public static final String OSM_FILE_PATH = "map.osm";
        public static final int DIMENSIONS = 2;
        public static final int BLOCK_SIZE = Integer.BYTES + 2 * Record.BYTES; // 32 * 1024

        public static void print_tree()
        {
                ArrayList<Node> new_nodes, nodes = new ArrayList<>();
                nodes.add(getNode(IndexMetaData.rootNodeId));

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


        // TODO: Make DataFile and IndexFile classes
        public static void deleteIndexAndDataFile()
        {
                File indexfile = new File(INDEX_FILE_NAME),
                        datafile = new File(DATA_FILE_NAME);
                indexfile.delete();
                datafile.delete();
        }



        public static void insertNode(Node newNode)
        {
                try {
                        FileOutputStream fos = new FileOutputStream(INDEX_FILE_NAME, true);
                        fos.write(newNode.toBytes());
                        IndexMetaData.addOneNode();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static Node getNode(long nodeId)
        {
                byte[] nodeAsBytes = new byte[Node.BYTES];
                Node node;
                try {
                        RandomAccessFile raf = new RandomAccessFile(INDEX_FILE_NAME, "r");
                        for (long i = 0; i < IndexMetaData.nextAvailableNodeId; ++i)
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

        public static void updateNode(Node updatedNode)
        {
                byte[] nodeAsBytes = new byte[Node.BYTES];
                Node node;
                try {
                        RandomAccessFile raf = new RandomAccessFile(INDEX_FILE_NAME, "rw");
                        for (long i = 0; i < IndexMetaData.getNumOfNodes(); ++i) // TODO: !!! NODE 0
                        {
                                //raf.seek(i * getNodeSizeInBytes());
                                raf.readFully(nodeAsBytes);
                                node = Node.fromBytes(nodeAsBytes);
                                if (node.getId() == updatedNode.getId())
                                {
                                        raf.seek(i * Node.BYTES); // TODO: May not be needed
                                        raf.write(updatedNode.toBytes());
                                }
                        }
                        raf.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static void setRootNode(Node newRootNode)
        {
                insertNode(newRootNode);
                IndexMetaData.rootNodeId = newRootNode.getId();
        }

        public static Node getRootNode()
        {
                return getNode(IndexMetaData.rootNodeId);
        }

        public static long getRootNodeId()
        {
                return IndexMetaData.rootNodeId;
        }





        private static void writeDataBlock(ArrayList<Record> records)
        {
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
                        RandomAccessFile raf = new RandomAccessFile(DATA_FILE_NAME, "rw");

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
        }

        public static ArrayList<Record> getDataBlock(long blockId)
        {
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
                DataMetaData.init();
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

}

