package utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
//import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tree.Node;
import tree.Record;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;

public class FileHandler {
        private static ArrayList<ArrayList<Record>> dataFile = new ArrayList<>(); // Dummy data file
        private static ArrayList<Node> indexFile = new ArrayList<>(); // Dummy index file

        private static long rootNodeId = 1;

        private static String osmFilePath = "map.osm";
        private static final int BLOCK_SIZE = 32 * 1024;
        private static final long maxRecordsInBlock = 2;  // Dummy maximum number of records in a block
        private static final long maxEntriesInBlock = 5;


        // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
        private static byte[] convertObjectToBytes(Object object) throws IOException
        {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(object);
                return byteArrayOutputStream.toByteArray();
        }

        // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
        private static Object convertBytesToObject(byte[] bytes) throws IOException, ClassNotFoundException
        {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                return objectInputStream.readObject();
        }

        // TODO: Make DataFile and IndexFile classes

        // Block: [records.length(), record0, record1, ..., ]
        // Block as bytes: [4Bytes + (8Bytes, String Bytes???, dimensions * 8Bytes) * records.length() <= BLOCK_SIZE]
        private static void writeBlockInDataFile(ArrayList<Record> records)
        {
                try {
                        byte[] recordsAsByteArray = convertObjectToBytes(records);
                        byte[] block = new byte[BLOCK_SIZE];

                        //System.arraycopy(recordsAsByteArray, 0, block, );

                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static long getMaxEntriesInBlock()
        {
                return maxEntriesInBlock;
        }

        public static Record getRecord(int blockId, long recordId)
        {
                for (Record record : dataFile.get(blockId))
                {
                        if (record.getId() == recordId)
                        {
                                return record;
                        }
                }
                return null;
        }

        public static Node getNode(long nodeId)
        {
                for (Node node : indexFile)
                {
                        if (node.getId() == nodeId)
                        {
                                return node;
                        }
                }
                return null;
        }

        public static long getNextAvailableNodeId()
        {
                return indexFile.size() + 1;
        }

        public static void insertNode(Node newNode)
        {
                indexFile.add(newNode);
        }

        public static void updateNode(Node updatedNode)
        {
                for (Node node : indexFile)
                {
                        if (node.getId() == updatedNode.getId())
                        {
                                node = updatedNode;
                                return;
                        }
                }
        }

        public static void setRootNode(Node newRootNode)
        {
                indexFile.add(newRootNode);
                rootNodeId = newRootNode.getId();
        }

        public static Node getRootNode()
        {
                return indexFile.get((int) getRootNodeId());
        }

        public static long getRootNodeId()
        {
                return rootNodeId;
        }

        public static void getIndexMetadata()
        {
                // root node id
        }

        public static void getDataMetadata()
        {
                // #records
                // #blocks
                // TODO: Create MetaDataClass
        }

        /*
        Extracts Record data (id, name and coordinates) from Open Street Maps XML file and loads them into
        the datafile
        */
        public static void loadDatafile()
        {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                ArrayList<Record> records = new ArrayList<>();
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
                                                                records.add(
                                                                        new Record(id, name, new double[]{lat, lon})
                                                                );
                                                                // TODO: Add Records to real datafile
                                                        }
                                                        if (records.size() >= maxRecordsInBlock)
                                                        {
                                                                dataFile.add(records);
                                                                records.clear();
                                                        }
                                                }
                                        }
                                }
                        }
                } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                } catch (SAXException e) {
                        e.printStackTrace();
                }
        }
}

