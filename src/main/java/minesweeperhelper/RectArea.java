package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class RectArea {
    
    public Rect rectangle;
    public BigDecimal width;
    public BigDecimal widthDecreased;
    public BigDecimal widthIncreased;
    public BigDecimal height;
    public BigDecimal heightDecreased;
    public BigDecimal heightIncreased;

    public RectArea(MatOfPoint contour, BigDecimal tollerance){
        rectangle = Imgproc.boundingRect(contour);

        width = BigDecimal.valueOf(rectangle.width).setScale(2, RoundingMode.HALF_EVEN);
        widthDecreased = width.subtract(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        widthIncreased = width.add(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        
        height = BigDecimal.valueOf(rectangle.height).setScale(2, RoundingMode.HALF_EVEN);
        heightDecreased = height.subtract(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        heightIncreased = height.add(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

    }

}
