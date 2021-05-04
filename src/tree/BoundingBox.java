package tree;

import java.io.Serializable;
import java.util.List;


public class BoundingBox implements Serializable {
    private double[] lowerLeftPoint; // The bottom left point of the rectangle
    private double[] upperRightPoint; // The bottom right point of the rectangle
    private int dimensions;

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
        return lowerLeftPoint;
    }

    public double[] getUpperRightPoint() {
        return upperRightPoint;
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

    public boolean checkOverlap(BoundingBox otherBB) {
        for (int i = 0; i < dimensions; i ++) {
            double overlapDiff = Math.min(upperRightPoint[i], otherBB.getUpperRightPoint()[i])
                    - Math.max(lowerLeftPoint[i], otherBB.getLowerLeftPoint()[i]);

            if (overlapDiff < 0) {
                return false;
            }
        }
        return true;
    }

    public double calculateOverlap(BoundingBox otherBB) {
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
}
