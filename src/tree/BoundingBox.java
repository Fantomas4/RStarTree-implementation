package tree;

import utils.ByteConvertible;
import utils.FileHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.sqrt;


public class BoundingBox extends ByteConvertible {
    private final double[] lowerLeftPoint; // The bottom left point of the bounding box
    private final double[] upperRightPoint; // The bottom right point of the bounding box
    private final int dimensions; // The number of dimensions of this bounding box
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

    /** Getter method used to retrieve the values of the bounding box's lower left point.
     * @return a copy of the bounding box's array that contains the values of its lower left point.
     */
    public double[] getLowerLeftPoint() {
        return lowerLeftPoint.clone();
    }

    /**
     * Getter method used to retrieve the values of the bounding box's upper right point.
     * @return a copy of the bounding box's array that contains the values of its upper right point.
     */
    public double[] getUpperRightPoint() {
        return upperRightPoint.clone();
    }

    /**
     * Getter method used to retrieve the number of dimensions of the bounding box.
     * @return  a number representing the number of dimensions.
     */
    public int getDimensions() {
        return dimensions;
    }

    /**
     * Calculates the margin of the bounding box.
     * @return a number representing the margin of the bounding box.
     */
    public double calculateMargin() {
        double sum = 0;

        for (int i = 0; i < dimensions; i ++) {
            sum += Math.abs(upperRightPoint[i] - lowerLeftPoint[i]);
        }

        return sum;
    }

    /**
     * Calculates the area of the bounding box.
     * @return a number representing the area of the bounding box.
     */
    public double calculateArea() {
        double product = 1;

        for (int i = 0; i < dimensions; i ++) {
            product *= upperRightPoint[i] - lowerLeftPoint[i];
        }

        return Math.abs(product);
    }

    /**
     * Calculates the coordinates of this bounding box's center.
     * @return an array containing the coordinates of the bounding box's center.
     */
    public double[] calculateCenter() {
        double[] centerCoordinates = new double[dimensions];

        for (int d = 0; d < dimensions; d++) {
            centerCoordinates[d] = (upperRightPoint[d] - lowerLeftPoint[d]) / 2;
        }

        return centerCoordinates;
    }

    /**
     * Calculates the overlap between this bounding box and a given bounding box.
     * @param otherBB the bounding box for which the overlap with this bounding box is calculated.
     * @return a number representing the calculated overlap.
     */
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

    /**
     * Calculates the min distance between the bounding box and a given point.
     * @param targetPoint the given point for which the min distance from the bounding box is to be calculated.
     * @return a number representing the distance between the bounding box and the given point.
     */
    public double calculateMinPointDistance(double[] targetPoint) {
        double sum = 0;

        for (int d = 0; d < dimensions; d++) {
            double rp;

            if (targetPoint[d] < lowerLeftPoint[d])
                rp = lowerLeftPoint[d];
            else if (targetPoint[d] > upperRightPoint[d])
                rp = upperRightPoint[d];
            else
                rp = targetPoint[d];

            sum += Math.pow(targetPoint[d] - rp, 2);
        }
        return sqrt(sum);
    }

    /** Calculates the min-max distance between the bounding box and a given point.
     * @param targetPoint the given point for which the min-max distance from the bounding box is to be calculated,
     * @return a number representing the min-max distance between the bounding box and the given point.
     */
    public double calculateMinMaxPointDistance(double[] targetPoint) {
        double minValue = Double.MAX_VALUE;

        for (int d = 0; d < dimensions; d++) {
            double calculatedValue = 0;

            double rm;
            if (targetPoint[d] <= (lowerLeftPoint[d] + upperRightPoint[d]) / 2) {
                rm = lowerLeftPoint[d];
            } else {
                rm = upperRightPoint[d];
            }

            calculatedValue += Math.pow(targetPoint[d] - rm, 2);

            double sum = 0;
            for (int d2 = 0; d2 < dimensions; d2++) {
                if (d2 != d) {
                    double rM;

                    if (targetPoint[d2] >= (lowerLeftPoint[d2] + upperRightPoint[d2]) / 2) {
                        rM = lowerLeftPoint[d2];
                    } else {
                        rM = upperRightPoint[d2];
                    }

                    sum += Math.pow(targetPoint[d2] - rM, 2);
                }
            }

            calculatedValue += sum;

            if (calculatedValue < minValue) {
                minValue = calculatedValue;
            }
        }

        return minValue;
    }

    /**
     * Used to calculate the Minimum Bounding Rectangle (MBR) of a set of bounding boxes.
     * @param boundingBoxes the bounding boxes for which the MBR is to be calculated.
     * @return a BoundingBox object representing the MBR for the given set of bounding boxes.
     */
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

    /**
     * Used to calculate the Minimum Bounding Rectangle (MBR) of a set of Entries.
     * @param entries the Entries for which the MBR is to be calculated.
     * @return a BoundingBox object representing the MBR for the given set of Entries.
     */
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
