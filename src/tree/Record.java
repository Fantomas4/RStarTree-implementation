package tree;

import utils.ByteConvertible;
import utils.FileHandler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * Class used to store the information of a location.
 */
public class Record extends ByteConvertible {
    private final long id; // The unique identifier of the record.
    private final String name; // The name of the location stored in the record.
    private final double[] coordinates; // The coordinates of the location stored in the record.
    // (RecordId, nameLength, name, Coordinates[DIMENSIONS])
    public static final int BYTES =
            Long.BYTES + Integer.BYTES + Character.BYTES * 256 + Double.BYTES * FileHandler.DIMENSIONS;

    public Record(long id, String name, double[] coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double[] getCoordinates() {
        return coordinates.clone();
    }

    public String toString()
    {
        return "Record(id(" + id + "), name(" + name + "), coordinates(" + Arrays.toString(coordinates) + "))";
    }

    @Override
    public byte[] toBytes()
    {
        byte[] idAsBytes = longToBytes(id),
                nameAsBytes = name.getBytes(StandardCharsets.UTF_8),
                nameLengthAsBytes = intToBytes(nameAsBytes.length),
                coordinatesAsBytes = new byte[coordinates.length * Double.BYTES],
                recordAsBytes = new byte[BYTES];

        for (int i = 0; i < coordinates.length; ++i)
        {
            double coordinate = coordinates[i];
            System.arraycopy(doubleToBytes(coordinate), 0, coordinatesAsBytes, i * Double.BYTES, Double.BYTES);
        }
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

    public static Record fromBytes(byte[] bytes)
    {
        byte[] idAsBytes = new byte[Long.BYTES],
                nameLengthAsBytes = new byte[Integer.BYTES],
                nameAsBytes = new byte[256],
                coordinatesAsBytes = new byte[FileHandler.DIMENSIONS * Double.BYTES];

        int srcPos = 0;
        System.arraycopy(bytes, srcPos, idAsBytes, 0, Long.BYTES);
        srcPos += Long.BYTES;
        System.arraycopy(bytes, srcPos, nameLengthAsBytes, 0, Integer.BYTES);
        srcPos += Integer.BYTES;
        System.arraycopy(bytes, srcPos, nameAsBytes, 0, 256);
        srcPos += 256;
        System.arraycopy(bytes, srcPos, coordinatesAsBytes, 0, FileHandler.DIMENSIONS * Double.BYTES);

        long id = bytesToLong(idAsBytes);
        int nameLength = bytesToInt(nameLengthAsBytes);
        String name = new String(nameAsBytes, 0, nameLength);
        double[] coordinates = new double[FileHandler.DIMENSIONS];
        for (int i = 0; i < FileHandler.DIMENSIONS; ++i)
        {
            byte[] coordinateAsBytes = new byte[Double.BYTES];
            System.arraycopy(coordinatesAsBytes, i * Double.BYTES, coordinateAsBytes, 0, Double.BYTES);
            coordinates[i] = bytesToDouble(coordinateAsBytes);
        }

        return new Record(id, name, coordinates);
    }
}
