package utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
//import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tree.BoundingBox;
import tree.Entry;
import tree.Node;
import tree.Record;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FileHandler {

        private static final String DATAFILE_NAME = "datafile.dat";
        private static final String INDEXFILE_NAME = "indexfile.dat";

        private static long rootNodeId = 1;
        private static int dimensions = 2;

        private static String osmFilePath = "map.osm";
        private static final int BLOCK_SIZE = 2 * 1024; // 32 * 1024
        private static long nextAvailableNodeId = 0;
        // (RecordId, nameLength, name, Coordinates)
        private static final int RECORD_SIZE = Long.BYTES + Integer.BYTES + Character.BYTES * 256 + Double.BYTES * dimensions;
        private static final int maxEntriesInBlock = 5;
        private static final int maxEntriesInNode = 3;

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



        public static long getNextAvailableNodeId()
        {
                return nextAvailableNodeId++;
        }



        private static int getBoundingBoxSizeInBytes()
        {
                return 2 * Double.BYTES * dimensions;
        }

        private static int getEntrySizeInBytes()
        {
                return getBoundingBoxSizeInBytes() + Long.BYTES;
        }

        private static int getNodeSizeInBytes()
        {
                return Long.BYTES + Integer.BYTES + maxEntriesInNode * getEntrySizeInBytes() + Integer.BYTES;
        }


        private static byte[] getBoundingBoxAsBytes(BoundingBox boundingBox)
        {
                byte[] boundingBoxAsBytes = new byte[getBoundingBoxSizeInBytes()];
                int destPos = 0;
                for (int i = 0; i < dimensions; ++i)
                {
                        System.arraycopy(doubleToBytes(boundingBox.getLowerLeftPoint()[i]), 0, boundingBoxAsBytes, destPos, Double.BYTES);
                        destPos += Double.BYTES;
                }
                for (int i = 0; i < dimensions; ++i)
                {
                        System.arraycopy(doubleToBytes(boundingBox.getUpperRightPoint()[i]), 0, boundingBoxAsBytes, destPos, Double.BYTES);
                        destPos += Double.BYTES;
                }
                return boundingBoxAsBytes;
        }

        private static byte[] getEntryAsBytes(Entry entry)
        {
                byte[] entryAsBytes = new byte[getEntrySizeInBytes()];
                System.arraycopy(getBoundingBoxAsBytes(entry.getBoundingBox()), 0, entryAsBytes, 0, getBoundingBoxSizeInBytes());
                System.arraycopy(longToBytes(entry.getChildNodeId()), 0, entryAsBytes, getBoundingBoxSizeInBytes(), Long.BYTES);
                return entryAsBytes;
        }

        private static byte[] getEntriesAsBytes(ArrayList<Entry> entries)
        {
                byte[] entriesAsBytes = new byte[Integer.BYTES + maxEntriesInNode * getEntrySizeInBytes()];

                int destPos = 0;
                System.arraycopy(intToBytes(entries.size()), 0, entriesAsBytes, destPos, Integer.BYTES);
                destPos += Integer.BYTES;

                for (int i = 0; i < entries.size(); ++i)
                {
                        System.arraycopy(getEntryAsBytes(entries.get(i)), 0, entriesAsBytes, destPos, getEntrySizeInBytes());
                        destPos += getEntrySizeInBytes();
                }

                return entriesAsBytes;
        }

        private static byte[] getNodeAsBytes(Node node)
        {
                if (node.getEntries().size() > maxEntriesInNode)
                {
                        throw new IllegalStateException("Node contains more entries than allowed");
                }
                byte[] idAsBytes = longToBytes(node.getId()),
                        entriesAsBytes = getEntriesAsBytes(node.getEntries()),
                        levelAsBytes = intToBytes(node.getLevel()),
                        nodeAsBytes = new byte[getNodeSizeInBytes()];
                System.out.println(getNodeSizeInBytes() + " vs " + (idAsBytes.length + entriesAsBytes.length + levelAsBytes.length));
                int destPos = 0;
                System.arraycopy(idAsBytes, 0, nodeAsBytes, destPos, idAsBytes.length);
                destPos += idAsBytes.length;
                System.arraycopy(entriesAsBytes, 0, nodeAsBytes, destPos, entriesAsBytes.length);
                destPos += entriesAsBytes.length;
                System.arraycopy(levelAsBytes, 0, nodeAsBytes, destPos, levelAsBytes.length);
                return nodeAsBytes;
        }

        public static void insertNode(Node newNode)
        {
                byte[] nodeAsBytes = getNodeAsBytes(newNode);
                try {
                        FileOutputStream fos = new FileOutputStream(INDEXFILE_NAME, true);
                        fos.write(nodeAsBytes);
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        private static BoundingBox getBoundingBoxFromBytes(byte[] bytes)
        {
                byte[] lowerLeftPointAsBytes = new byte[Double.BYTES * dimensions],
                        upperRightPointAsBytes = new byte[Double.BYTES * dimensions];

                int srcPos = 0;
                System.arraycopy(bytes, srcPos, lowerLeftPointAsBytes, 0, lowerLeftPointAsBytes.length);
                srcPos += lowerLeftPointAsBytes.length;
                System.arraycopy(bytes, srcPos, upperRightPointAsBytes, 0, upperRightPointAsBytes.length);

                double[] lowerLeftPoint = new double[dimensions],
                        upperRightPoint = new double[dimensions];
                byte[] pointValueAsBytes = new byte[Double.BYTES];
                for (int i = 0; i < dimensions; ++i)
                {
                        System.arraycopy(lowerLeftPointAsBytes, i * Double.BYTES, pointValueAsBytes, 0, Double.BYTES);
                        lowerLeftPoint[i] = bytesToDouble(pointValueAsBytes);

                        System.arraycopy(upperRightPointAsBytes, i * Double.BYTES, pointValueAsBytes, 0, Double.BYTES);
                        upperRightPoint[i] = bytesToDouble(pointValueAsBytes);
                }
                return new BoundingBox(lowerLeftPoint, upperRightPoint);
        }

        private static Entry getEntryFromBytes(byte[] bytes)
        {
                byte[] boundingBoxAsBytes = new byte[getBoundingBoxSizeInBytes()],
                        childNodeIdAsBytes = new byte[Long.BYTES];
                int srcPos = 0;
                System.arraycopy(bytes, srcPos, boundingBoxAsBytes, 0, boundingBoxAsBytes.length);
                srcPos += boundingBoxAsBytes.length;
                System.arraycopy(bytes, srcPos, childNodeIdAsBytes, 0, childNodeIdAsBytes.length);
                return new Entry(getBoundingBoxFromBytes(boundingBoxAsBytes), bytesToLong(childNodeIdAsBytes));
        }

        private static ArrayList<Entry> getEntriesFromBytes(byte[] bytes)
        {
                byte[] sizeAsBytes = new byte[Integer.BYTES];
                int srcPos = 0;
                System.arraycopy(bytes, srcPos, sizeAsBytes, 0, sizeAsBytes.length);
                int size = bytesToInt(sizeAsBytes);
                if (size == 0)
                {
                        return null;
                }
                ArrayList<Entry> entries = new ArrayList<>();
                for (int i = 0; i < size; ++i)
                {
                        byte[] entryAsBytes = new byte[getEntrySizeInBytes()];
                        System.arraycopy(bytes, srcPos, entryAsBytes, 0, entryAsBytes.length);
                        srcPos += entryAsBytes.length;
                        entries.add(getEntryFromBytes(entryAsBytes));
                }
                return entries;
        }

        private static Node getNodeFromBytes(byte[] bytes)
        {
                byte[] idAsBytes = new byte[Long.BYTES],
                        entriesAsBytes = new byte[Integer.BYTES + maxEntriesInNode * getEntrySizeInBytes()],
                        levelAsBytes = new byte[Integer.BYTES];
                int srcPos = 0;
                System.arraycopy(bytes, srcPos, idAsBytes, 0, idAsBytes.length);
                srcPos += idAsBytes.length;
                System.arraycopy(bytes, srcPos, entriesAsBytes, 0, entriesAsBytes.length);
                srcPos += entriesAsBytes.length;
                System.arraycopy(bytes, srcPos, levelAsBytes, 0, levelAsBytes.length);

                long id = bytesToLong(idAsBytes);
                ArrayList<Entry> entries = getEntriesFromBytes(entriesAsBytes);
                int level = bytesToInt(levelAsBytes);

                return entries == null ? new Node(level, id) : new Node(entries, level, id);
        }

        // TODO: MIGHT NEED TO LINEAR SEARCH FOR THE RIGHT NODE ID
        public static Node getNode(long nodeId)
        {
                byte[] nodeAsBytes = new byte[getNodeSizeInBytes()];
                Node node;
                try {
                        RandomAccessFile raf = new RandomAccessFile(INDEXFILE_NAME, "r");
                        for (int i = 0; i < nextAvailableNodeId; ++i)
                        {
                                raf.seek(i * getNodeSizeInBytes());
                                raf.readFully(nodeAsBytes);
                                node = getNodeFromBytes(nodeAsBytes);
                                if (node.getId() == nodeId)
                                {
                                        return node;
                                }
                        }

                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return null;
        }

        public static void updateNode(Node updatedNode)
        {
                byte[] nodeAsBytes = new byte[getNodeSizeInBytes()];
                Node node;
                try {
                        RandomAccessFile raf = new RandomAccessFile(INDEXFILE_NAME, "rw");
                        for (int i = 0; i < nextAvailableNodeId; ++i)
                        {
                                raf.seek(i * getNodeSizeInBytes());
                                raf.readFully(nodeAsBytes);
                                node = getNodeFromBytes(nodeAsBytes);
                                if (node.getId() == updatedNode.getId())
                                {
                                        raf.seek(i * getNodeSizeInBytes()); // TODO: May not be needed
                                        raf.write(getNodeAsBytes(updatedNode));
                                }
                        }
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static void setRootNode(Node newRootNode)
        {
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

        public static void getIndexMetadata()
        {
                // root node id
        }



        // https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java
        private static byte[] longToBytes(long l)
        {
                byte[] result = new byte[Long.BYTES];
                for (int i = Long.BYTES - 1; i >= 0; --i)
                {
                        result[i] = (byte)(l & 0xFF);
                        l >>= Byte.SIZE;
                }
                return result;
        }

        private static long bytesToLong(final byte[] b)
        {
                long result = 0;
                for (int i = 0; i < Long.BYTES; ++i)
                {
                        result <<= Byte.SIZE;
                        result |= (b[i] & 0xFF);
                }
                return result;
        }

        // https://stackoverflow.com/questions/2905556/how-can-i-convert-a-byte-array-into-a-double-and-back?answertab=votes#tab-top
        private static byte[] doubleToBytes(double value)
        {
                byte[] bytes = new byte[Double.BYTES];
                ByteBuffer.wrap(bytes).putDouble(value);
                return bytes;
        }

        private static double bytesToDouble(byte[] bytes)
        {
                return ByteBuffer.wrap(bytes).getDouble();
        }

        // https://stackoverflow.com/questions/1936857/convert-integer-into-byte-array-java
        private static byte[] intToBytes(int value)
        {
                byte[] bytes = new byte[Integer.BYTES];
                ByteBuffer.wrap(bytes).putInt(value);
                return bytes;
        }

        private static int bytesToInt(byte[] bytes)
        {
                return ByteBuffer.wrap(bytes).getInt();
        }

        private static int getRecordLengthInBytes()
        {
                // (RecordId, nameLength, name, Coordinates)
                return Long.BYTES + Integer.BYTES + Character.BYTES * 256 + Double.BYTES * dimensions;
        }

        private static byte[] getRecordAsBytes(Record record)
        {
                byte[] idAsBytes = longToBytes(record.getId());
                byte[] nameAsBytes = record.getName().getBytes(StandardCharsets.UTF_8);
                byte[] nameLengthAsBytes = intToBytes(nameAsBytes.length);
                byte[] coordinatesAsBytes = new byte[record.getCoordinates().length * Double.BYTES];
                for (int i = 0; i < record.getCoordinates().length; ++i)
                {
                        double coordinate = record.getCoordinates()[i];
                        System.arraycopy(doubleToBytes(coordinate), 0, coordinatesAsBytes, i * Double.BYTES, Double.BYTES);
                }

                byte[] recordAsBytes = new byte[getRecordLengthInBytes()];
                int destPos = 0;
                System.arraycopy(idAsBytes, 0, recordAsBytes, destPos, idAsBytes.length);
                destPos += idAsBytes.length;
                System.arraycopy(nameLengthAsBytes, 0, recordAsBytes, destPos, nameLengthAsBytes.length);
                destPos += nameLengthAsBytes.length;
                System.arraycopy(nameAsBytes, 0, recordAsBytes, destPos, nameAsBytes.length);
                destPos += nameAsBytes.length;
                System.arraycopy(new byte[256 - nameAsBytes.length], 0, recordAsBytes, destPos, 256 - nameAsBytes.length);
                destPos += (256 - nameAsBytes.length);
                System.arraycopy(coordinatesAsBytes, 0, recordAsBytes, destPos, coordinatesAsBytes.length);

                return recordAsBytes;
        }

        private static Record getRecordFromBytes(byte[] bytes)
        {
                byte[] idAsBytes = new byte[Long.BYTES],
                        nameLengthAsBytes = new byte[Integer.BYTES],
                        nameAsBytes = new byte[256],
                        coordinatesAsBytes = new byte[dimensions * Double.BYTES];
                int srcPos = 0;
                System.arraycopy(bytes, srcPos, idAsBytes, 0, Long.BYTES);
                srcPos += Long.BYTES;
                System.arraycopy(bytes, srcPos, nameLengthAsBytes, 0, Integer.BYTES);
                srcPos += Integer.BYTES;
                System.arraycopy(bytes, srcPos, nameAsBytes, 0, 256);
                srcPos += 256;
                System.arraycopy(bytes, srcPos, coordinatesAsBytes, 0, dimensions * Double.BYTES);

                long id = bytesToLong(idAsBytes);
                int nameLength = bytesToInt(nameLengthAsBytes);
                String name = new String(nameAsBytes, 0, nameLength);
                double[] coordinates = new double[dimensions];
                for (int i = 0; i < dimensions; ++i)
                {
                        byte[] coordinateAsBytes = new byte[Double.BYTES];
                        System.arraycopy(coordinatesAsBytes, i * Double.BYTES, coordinateAsBytes, 0, Double.BYTES);
                        coordinates[i] = bytesToDouble(coordinateAsBytes);
                }

                return new Record(id, name, coordinates);
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
                System.arraycopy(intToBytes(numberOfRecords), 0, block, destPos, Integer.BYTES);
                destPos += Integer.BYTES;
                for (int i = 0; i < records.size(); ++i)
                {
                        System.arraycopy(getRecordAsBytes(records.get(i)), 0, block, destPos, getRecordLengthInBytes());
                        destPos += getRecordLengthInBytes();
                }

                try {
                        FileOutputStream fos = new FileOutputStream(DATAFILE_NAME, true);
                        fos.write(block);
                        DataMetaData.addOneBlock();
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static ArrayList<Record> getDataBlock(long blockId)
        {
                byte[] block = new byte[BLOCK_SIZE];
                try {
                        RandomAccessFile raf = new RandomAccessFile(DATAFILE_NAME, "r");
                        raf.seek(blockId * BLOCK_SIZE);
                        raf.readFully(block);
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
                byte[] numberOfRecordsAsBytes = new byte[Integer.BYTES];
                int srcPos = 0;
                System.arraycopy(block, srcPos, numberOfRecordsAsBytes, 0, Integer.BYTES);
                srcPos += Integer.BYTES;
                int numberOfRecords = bytesToInt(numberOfRecordsAsBytes);
                ArrayList<Record> records = new ArrayList<>();
                for (int i = 0; i < numberOfRecords; ++i)
                {
                        byte[] recordAsBytes = new byte[getRecordLengthInBytes()];
                        System.arraycopy(block, srcPos, recordAsBytes, 0, getRecordLengthInBytes());
                        srcPos += getRecordLengthInBytes();
                        records.add(getRecordFromBytes(recordAsBytes));
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
                } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                } catch (SAXException e) {
                        e.printStackTrace();
                }
        }

        public static int getMaxEntriesInBlock()
        {
                return maxEntriesInBlock;
        }



        public static void main(String[] args) throws FileNotFoundException {
                Record my_record1 = new Record(1, "home", new double[]{1.0, 1.0});
                Record my_record2 = new Record(2, "office", new double[]{2.0, 2.0});
                Record my_record3 = new Record(3, "university", new double[]{3.0, 3.0});
                loadDatafile();
                /*
                ArrayList<Entry> entries = new ArrayList<>();
                entries.add(new Entry(new BoundingBox(new double[]{0.0, 0.0}, new double[]{1.0, 1.0})));
                Node my_node = new Node(entries, 0, getNextAvailableNodeId());
                insertNode(my_node);
                updateNode(new Node(entries, 90000, my_node.getId()));
                Node node = getNode(my_node.getId());
                if (node != null)
                {
                        System.out.println("found");
                        System.out.println(node.getLevel());
                }
                else
                        System.out.println("not found");
                */


                System.out.println("lets go");
        }
}

