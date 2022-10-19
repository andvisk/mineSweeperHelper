package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class RectArea {

    public UUID id;
    public Rect rectangle;
    public BigDecimal areaSize;
    public BigDecimal decreasedAreaSize;
    public BigDecimal width;
    public BigDecimal widthDecreased;
    public BigDecimal height;
    public BigDecimal heightDecreased;
    public BigDecimal x;
    public BigDecimal xDecreased;
    public BigDecimal y;
    public BigDecimal yDecreased;

    public int positionInGridX = -1;
    public int positionInGridY = -1;

    public ColorsEnum color;

    public RectArea() {

    }

    public RectArea(Rect rectangle, BigDecimal tolleranceInPercent) {
        this(null, tolleranceInPercent, ColorsEnum.YELLOW, rectangle);
    }

    public RectArea(MatOfPoint contour, BigDecimal tollerance, ColorsEnum color) {
        this(contour, tollerance, color, null);
    }

    public RectArea(MatOfPoint contour, BigDecimal tollerance, ColorsEnum color, Rect rect) {

        this.id = UUID.randomUUID();

        if (contour != null)
            rectangle = Imgproc.boundingRect(contour);
        else
            rectangle = rect;

        this.color = color;

        width = BigDecimal.valueOf(rectangle.width).setScale(2, RoundingMode.HALF_EVEN);
        widthDecreased = width
                .subtract(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        height = BigDecimal.valueOf(rectangle.height).setScale(2, RoundingMode.HALF_EVEN);
        heightDecreased = height
                .subtract(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        x = BigDecimal.valueOf(rectangle.x).setScale(2, RoundingMode.HALF_EVEN);
        xDecreased = x.subtract(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        y = BigDecimal.valueOf(rectangle.y).setScale(2, RoundingMode.HALF_EVEN);
        yDecreased = y.subtract(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        areaSize = width.multiply(height);
        decreasedAreaSize = widthDecreased.multiply(heightDecreased);

    }

}
