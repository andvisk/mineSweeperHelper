package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.opencv.core.Core;
import org.opencv.core.Point;

public class LineArea {

    public UUID id;
    public double[] coord;
    public BigDecimal leftAngle;
    public BigDecimal length;
    public BigDecimal lengthDecreased;

    public ColorsEnum color;

    public LineArea() {

    }

    public LineArea(double[] coord, BigDecimal tollerance) {

        this.id = UUID.randomUUID();
        this.coord = coord;

        length = BigDecimal.valueOf(Math.sqrt(Math.pow(coord[3] - coord[1], 2) + Math.pow(coord[2] - coord[0], 2)))
                .setScale(2);

        Point topPoint = (coord[1] < coord[3]) ? new Point(coord[0], coord[1]) : new Point(coord[2], coord[3]);
        Point steepAnglePoint = (coord[1] < coord[3]) ? new Point(coord[0], coord[1]) : new Point(coord[2], coord[3])


    }

}
