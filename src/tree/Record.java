package tree;

import java.io.Serializable;
import java.util.Arrays;

public class Record implements Serializable {
    private long id;
    private String name;
    private double[] coordinates;

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
