package minesweeperhelper;

import org.opencv.core.Point;

public class Intersection {

    public LineArea lineArea1;
    public LineArea lineArea2;
    public Point intersectionPoint;

    public Intersection(LineArea lineArea1, LineArea lineArea2, Point intersectionPoint) {
        this.lineArea1 = lineArea1;
        this.lineArea2 = lineArea2;
        this.intersectionPoint = intersectionPoint;
    }
}
