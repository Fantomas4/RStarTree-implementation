package tree;

import java.io.Serializable;

public class Record implements Serializable {
    private int id;
    private String name;
    private double[] coordinates;

    public Record(int id, String name, double[] coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

}
