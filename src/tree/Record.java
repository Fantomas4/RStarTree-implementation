package tree;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Class used to store the information of a location.
 */
public class Record implements Serializable {
    private final long id; // The unique identifier of the record.
    private final String name; // The name of the location stored in the record.
    private final double[] coordinates; // The coordinates of the location stored in the record.

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
        return coordinates;
    }

    public String toString()
    {
        return "Record(id(" + id + "), name(" + name + "), coordinates(" + Arrays.toString(coordinates) + "))";
    }

}
