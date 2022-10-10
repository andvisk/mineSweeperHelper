package minesweeperhelper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;

import org.opencv.core.Core;
import org.opencv.core.Point;

public class LineArea {

    public UUID id;
    public double[] coord;

    public Point point1;
    public Point point2;

    public BigDecimal length;
    public BigDecimal lengthDecreased;

    public ColorsEnum color;

    public BigDecimal angleIndegrees;

    public LineArea() {

    }

    public LineArea(double[] coord, BigDecimal tollerance) {

        this.id = UUID.randomUUID();
        this.coord = coord;

        double x1 = coord[0];
        double x2 = coord[2];
        double y1 = coord[1];
        double y2 = coord[3];

        length = length(new Point(x1, y1), new Point(x2, y2));

        lengthDecreased = length
                .subtract(length.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        point1 = new Point(x1, y1);
        point2 = new Point(x2, y2);

        Point topPoint = (y1 < y2) ? point1 : point2;
        Point steepAnglePoint = (y1 < y2) ? new Point(x1, y2) : new Point(x2, y1);

        BigDecimal lengthTopToSteepAngle = length(topPoint, steepAnglePoint);

        angleIndegrees = BigDecimal.valueOf(Math
                .toDegrees(Math.asin(lengthTopToSteepAngle.divide(length, 4, RoundingMode.HALF_EVEN).doubleValue()))).setScale(2, RoundingMode.HALF_EVEN);

    }

    public boolean isHorizontal(){
        return angleIndegrees.equals(BigDecimal.valueOf(0).setScale(2));
    }

    public boolean isVertical(){
        return angleIndegrees.equals(BigDecimal.valueOf(90).setScale(2));
    }

    private BigDecimal length(Point point1, Point point2) {
        return BigDecimal.valueOf(Math.sqrt(Math.pow(point2.y - point1.y, 2) + Math.pow(point2.x - point1.x, 2)))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

}
