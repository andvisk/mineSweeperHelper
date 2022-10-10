package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.opencv.core.Core;
import org.opencv.core.Point;

public class LineArea {

    public UUID id;
    public double[] coord;
    public BigDecimal width;
    public BigDecimal widthDecreased;
    public BigDecimal height;
    public BigDecimal heightDecreased;
    public BigDecimal length;
    public BigDecimal lengthDecreased;
    public BigDecimal x;
    public BigDecimal xDecreased;
    public BigDecimal y;
    public BigDecimal yDecreased;

    public int positionInGridX = -1;
    public int positionInGridY = -1;

    public ColorsEnum color;

    public LineArea() {

    }

    public LineArea(double[] coord, BigDecimal tollerance) {

        this.id = UUID.randomUUID();
        this.coord = coord;

        width = BigDecimal.valueOf(Math.abs(coord[0] - coord[2])).setScale(2, RoundingMode.HALF_EVEN);
        widthDecreased = width
                .subtract(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        height = BigDecimal.valueOf(Math.abs(coord[1] - coord[3])).setScale(2, RoundingMode.HALF_EVEN);
        heightDecreased = height
                .subtract(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        length = BigDecimal.valueOf(Math.sqrt(Math.pow(coord[3]-coord[1], 2)+Math.pow(coord[2]-coord[0], 2))).setScale(2);

        aaa

        x = BigDecimal.valueOf((coord[0] < coord[2]) ? coord[0] : coord[2]).setScale(2, RoundingMode.HALF_EVEN);
        xDecreased = x.subtract(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        y = BigDecimal.valueOf((coord[1] < coord[3]) ? coord[1] : coord[3]).setScale(2, RoundingMode.HALF_EVEN);
        yDecreased = y.subtract(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

    }

}
