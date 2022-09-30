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
    public BigDecimal width;
    public BigDecimal widthIncreased;
    public BigDecimal widthDecreased;
    public BigDecimal height;
    public BigDecimal heightIncreased;
    public BigDecimal heightDecreased;
    public BigDecimal x;
    public BigDecimal xIncreased;
    public BigDecimal xDecreased;
    public BigDecimal y;
    public BigDecimal yIncreased;
    public BigDecimal yDecreased;

    public int positionInGridX = -1;
    public int positionInGridY = -1;

    public RectArea(){
        
    }

    public RectArea(MatOfPoint contour, BigDecimal tollerance){

        this.id = UUID.randomUUID();

        rectangle = Imgproc.boundingRect(contour);

        width = BigDecimal.valueOf(rectangle.width).setScale(2, RoundingMode.HALF_EVEN);
        widthIncreased = width.add(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        widthDecreased = width.subtract(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        
        height = BigDecimal.valueOf(rectangle.height).setScale(2, RoundingMode.HALF_EVEN);
        heightIncreased = height.add(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        heightDecreased = height.subtract(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        x = BigDecimal.valueOf(rectangle.x).setScale(2, RoundingMode.HALF_EVEN);
        xIncreased = x.add(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        xDecreased = x.subtract(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        y = BigDecimal.valueOf(rectangle.y).setScale(2, RoundingMode.HALF_EVEN);
        yIncreased = y.add(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        yDecreased = y.subtract(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

    }

}
