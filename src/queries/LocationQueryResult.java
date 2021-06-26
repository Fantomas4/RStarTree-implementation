package queries;

import tree.Record;

import java.util.Arrays;


/**
 * Class used to store the information of query results.
 */
public class LocationQueryResult implements Comparable<LocationQueryResult> {
    private final long recordId; // TEMP FOR DEBUGGING!
    private final String name;
    private final double[] coordinates;
    private final double distance;

    public LocationQueryResult(long recordId, String name, double[] coordinates, double distance) {
        this.recordId = recordId;
        this.name = name;
        this.coordinates = coordinates;
        this.distance = distance;
    }

    public LocationQueryResult(Record record, double distance) {
        this.recordId = record.getId();
        this.name = record.getName();
        this.coordinates = record.getCoordinates();
        this.distance = distance;
    }

    public long getRecordId() {
        return recordId;
    }

    public String getName() {
        return name;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(LocationQueryResult other) {
        int distanceCompRes = Double.compare(distance, other.getDistance());

        if (distanceCompRes == 0) {
            return name.compareTo(other.getName());
        } else {
            return distanceCompRes;
        }
    }

    @Override
    public String toString() {
        /*
        return "LocationQueryResult{" +
                "recordId=" + recordId +
                ", name='" + name + '\'' +
                ", coordinates=" + Arrays.toString(coordinates) +
                ", distance=" + distance +
                '}';
        */
        return "-----------------------------------------\n" +
//                "Record ID: " + recordId + "\n" +
                "Name: " + name + "\n" +
                "Coordinates: " + Arrays.toString(coordinates) + "\n" +
                "Distance: " + distance + "\n" +
                "-----------------------------------------\n";
    }
}
