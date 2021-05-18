package tree;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;

public class FileHandler {
        private ArrayList<ArrayList<Record>> dataFile; // Dummy data file
        private ArrayList<Node> indexFile; // Dummy index file

        private long rootNodeId;

        private String osmFilePath;
        private static final int BLOCK_SIZE = 32 * 1024;
        private static final long maxRecordsInBlock = 2;  // Dummy maximum number of records in a block
        private static final long maxEntriesInBlock = 5;

        FileHandler()
        {
                this.osmFilePath = "map.osm";
                this.rootNodeId = 1;
                this.dataFile = new ArrayList<>();
                this.indexFile = new ArrayList<>();
        }

        // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
        private byte[] convertObjectToBytes(Object object) throws IOException
        {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(object);
                return byteArrayOutputStream.toByteArray();
        }

        // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
        private Object convertBytesToObject(byte[] bytes) throws IOException, ClassNotFoundException
        {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                return objectInputStream.readObject();
        }

        // TODO: Make DataFile and IndexFile classes

        // Block: [records.length(), record0, record1, ..., ]
        // Block as bytes: [4Bytes + (8Bytes, String Bytes???, dimensions * 8Bytes) * records.length() <= BLOCK_SIZE]
        private void writeBlockInDataFile(ArrayList<Record> records)
        {
                try {
                        byte[] recordsAsByteArray = this.convertObjectToBytes(records);
                        byte[] block = new byte[BLOCK_SIZE];

                        //System.arraycopy(recordsAsByteArray, 0, block, );

                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public long getMaxEntriesInBlock()
        {
                return this.maxEntriesInBlock;
        }

        public Record getRecord(int blockId, long recordId)
        {
                for (Record record : this.dataFile.get(blockId))
                {
                        if (record.getId() == recordId)
                        {
                                return record;
                        }
                }
                return null;
        }

        public Node getNode(long nodeId)
        {
                for (Node node : this.indexFile)
                {
                        if (node.getId() == nodeId)
                        {
                                return node;
                        }
                }
                return null;
        }

        public long getNextAvailableNodeId()
        {
                return this.indexFile.size() + 1;
        }

        public void insertNode(Node newNode)
        {
                this.indexFile.add(newNode);
        }

        public void updateNode(Node updatedNode)
        {
                for (Node node : this.indexFile)
                {
                        if (node.getId() == updatedNode.getId())
                        {
                                node = updatedNode;
                                return;
                        }
                }
        }

        public void setRootNode(Node newRootNode)
        {
                this.indexFile.add(newRootNode);
                this.rootNodeId = newRootNode.getId();
        }

        public Node getRootNode()
        {
                return this.indexFile.get((int) this.getRootNodeId());
        }

        public long getRootNodeId()
        {
                return this.rootNodeId;
        }

        public void getIndexMetadata()
        {
                // root node id
        }

        public void getDataMetadata()
        {
                // #records
                // #blocks
                // TODO: Create MetaDataClass
        }

        /*
        Extracts Record data (id, name and coordinates) from Open Street Maps XML file and loads them into
        the datafile
        */
        public void loadDatafile()
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
                                Node node = nodeList.item(i);
                                if (node.getNodeType() == Node.ELEMENT_NODE)
                                {
                                        Element nodeElement = (Element) node;
                                        NodeList tagList = nodeElement.getChildNodes();
                                        for (int j = 0; j < tagList.getLength(); ++j)
                                        {
                                                Node tag = tagList.item(j);
                                                if (tag.getNodeType() == Node.ELEMENT_NODE)
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
                                                                this.dataFile.add(records);
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

