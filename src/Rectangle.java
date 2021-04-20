import java.io.Serializable;

public class Rectangle implements Serializable {
    private double[] bottomLeft;
    private double[] upperRight;

    public Rectangle(double[] bottomLeft, double[] upperRight) {
        this.bottomLeft = bottomLeft;
        this.upperRight = upperRight;
    }

    public double[] getBottomLeft() {
        return bottomLeft;
    }

    public double[] getUpperRight() {
        return upperRight;
    }

    public double getPerimeter() {

    }

    public double getArea() {

    }

    public double calculateOverlap(Rectangle otherRectangle) {

    }
}
