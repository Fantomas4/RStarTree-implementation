package tree;

import utils.ByteConvertable;
import utils.FileHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.Math.sqrt;


public class BoundingBox extends ByteConvertable {
    private final double[] lowerLeftPoint; // The bottom left point of the rectangle
    private final double[] upperRightPoint; // The bottom right point of the rectangle
    private final int dimensions;
    // (lowerLeftPoint[DIMENSIONS], upperRightPoint[DIMENSIONS])
    public static final int BYTES = 2 * Double.BYTES * FileHandler.DIMENSIONS;

    public BoundingBox(double[] lowerLeftPoint, double[] upperRightPoint) {
        if (lowerLeftPoint.length != upperRightPoint.length) {
            throw new IllegalArgumentException("The lower left point and the upper right point that define a bounding box" +
                    "must have the same number of dimensions.");
        } else {
            this.lowerLeftPoint = lowerLeftPoint;
            this.upperRightPoint = upperRightPoint;
            dimensions = lowerLeftPoint.length;
        }
    }

    public double[] getLowerLeftPoint() {
        return lowerLeftPoint.clone();
    }

    public double[] getUpperRightPoint() {
        return upperRightPoint.clone();
    }

    public int getDimensions() {
        return dimensions;
    }

    public double getMargin() {
        double sum = 0;
        for (int i = 0; i < dimensions; i ++) {
            sum += Math.abs(upperRightPoint[i] - lowerLeftPoint[i]);
        }
        return sum;
    }

    public double getArea() {
        double product = 1;
        for (int i = 0; i < dimensions; i ++) {
            product *= upperRightPoint[i] - lowerLeftPoint[i];
        }
        return Math.abs(product);
    }

    public double[] getCenter() {
        double[] centerCoordinates = new double[dimensions];

        for (int d = 0; d < dimensions; d++) {
            centerCoordinates[d] = (upperRightPoint[d] - lowerLeftPoint[d]) / 2;
        }
        return centerCoordinates;
    }

    public double calculateBoundingBoxOverlap(BoundingBox otherBB) {
        double overlapProduct = 1;
        for (int i = 0; i < dimensions; i ++) {
            double overlapDiff = Math.min(upperRightPoint[i], otherBB.getUpperRightPoint()[i])
                    - Math.max(lowerLeftPoint[i], otherBB.getLowerLeftPoint()[i]);

            if (overlapDiff <= 0) {
                return 0;
            } else {
                overlapProduct *= overlapDiff;
            }
        }
        return overlapProduct;
    }

    public double calculatePointDistance(double[] targetPoint) {
        double sum = 0;
        for (int d = 0; d < dimensions; d++) {
            double lowerLeftDistance = Math.pow(targetPoint[d] - lowerLeftPoint[d], 2);
            double upperRightDistance = Math.pow(targetPoint[d] - upperRightPoint[d], 2);

            sum += min(lowerLeftDistance, upperRightDistance);
        }

        return sqrt(sum);
    }

    public boolean checkPointOverlap(double[] targetPoint, double radius) {
        double minimumDistance = calculatePointDistance(targetPoint);

        return minimumDistance <= radius;
    }

    // Class used to calculate minimum bounding rectangles
    public static BoundingBox calculateMBR(List<BoundingBox> boundingBoxes) {
        BoundingBox primerBoundingBox = boundingBoxes.remove(0);
        int dimensions = primerBoundingBox.getDimensions();
        double[] minLowerLeft = primerBoundingBox.getLowerLeftPoint();
        double[] maxUpperRight = primerBoundingBox.getUpperRightPoint();

        for (BoundingBox boundingBox : boundingBoxes) {
            for (int j = 0; j < dimensions; j++) {
                if (minLowerLeft[j] > boundingBox.getLowerLeftPoint()[j]) {
                    minLowerLeft[j] = boundingBox.getLowerLeftPoint()[j];
                }

                if (maxUpperRight[j] < boundingBox.getUpperRightPoint()[j]) {
                    maxUpperRight[j] = boundingBox.getUpperRightPoint()[j];
                }
            }
        }

        return new BoundingBox(minLowerLeft, maxUpperRight);
    }

    public static BoundingBox calculateMBR(ArrayList<Entry> entries) {
        ArrayList<BoundingBox> boundingBoxes = new ArrayList<>();

        for (Entry entry : entries) {
            boundingBoxes.add(entry.getBoundingBox());
        }

        return calculateMBR(boundingBoxes);
    }

    public String toString()
    {
        return "BoundingBox(" + Arrays.toString(lowerLeftPoint) + ", " +  Arrays.toString(upperRightPoint) + ")";
    }

    @Override
    public byte[] toBytes() {
        byte[] boundingBoxAsBytes = new byte[BYTES];
        int destPos = 0;

        for (int i = 0; i < FileHandler.DIMENSIONS; ++i)
        {
            System.arraycopy(doubleToBytes(lowerLeftPoint[i]), 0, boundingBoxAsBytes, destPos, Double.BYTES);
            destPos += Double.BYTES;
        }
        for (int i = 0; i < FileHandler.DIMENSIONS; ++i)
        {
            System.arraycopy(doubleToBytes(upperRightPoint[i]), 0, boundingBoxAsBytes, destPos, Double.BYTES);
            destPos += Double.BYTES;
        }

        return boundingBoxAsBytes;
    }

    public static BoundingBox fromBytes(byte[] bytes)
    {
        byte[] lowerLeftPointAsBytes = new byte[Double.BYTES * FileHandler.DIMENSIONS],
                upperRightPointAsBytes = new byte[Double.BYTES * FileHandler.DIMENSIONS];
        int srcPos = 0;

        System.arraycopy(bytes, srcPos, lowerLeftPointAsBytes, 0, lowerLeftPointAsBytes.length);
        srcPos += lowerLeftPointAsBytes.length;
        System.arraycopy(bytes, srcPos, upperRightPointAsBytes, 0, upperRightPointAsBytes.length);

        double[] lowerLeftPoint = new double[FileHandler.DIMENSIONS],
                upperRightPoint = new double[FileHandler.DIMENSIONS];
        byte[] pointValueAsBytes = new byte[Double.BYTES];
        for (int i = 0; i < FileHandler.DIMENSIONS; ++i)
        {
            System.arraycopy(lowerLeftPointAsBytes, i * Double.BYTES, pointValueAsBytes, 0, Double.BYTES);
            lowerLeftPoint[i] = bytesToDouble(pointValueAsBytes);

            System.arraycopy(upperRightPointAsBytes, i * Double.BYTES, pointValueAsBytes, 0, Double.BYTES);
            upperRightPoint[i] = bytesToDouble(pointValueAsBytes);
        }

        return new BoundingBox(lowerLeftPoint, upperRightPoint);
    }
}
