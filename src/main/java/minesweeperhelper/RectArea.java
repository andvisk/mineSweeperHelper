package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class RectArea {
    
    public BigDecimal areaSize;
    public BigDecimal decreasedAreaSize;
    public BigDecimal increasedAreaSize;
    public Rect rectangle;
    public BigDecimal width;
    public BigDecimal widthDecreased;
    public BigDecimal widthIncreased;
    public BigDecimal height;
    public BigDecimal heightDecreased;
    public BigDecimal heightIncreased;

    public List<Set<RectArea>> areaGroups = new ArrayList<>();
    public List<Set<RectArea>> widthGroups = new ArrayList<>();
    public List<Set<RectArea>> heightGroups = new ArrayList<>();

    Set<RectArea> maxWidthGroup = new HashSet<>();
    Set<RectArea> maxHeightGroup = new HashSet<>();

    int sizeOfMaxGroup = -1;

    public RectArea(MatOfPoint contour, BigDecimal tollerance){
        rectangle = Imgproc.boundingRect(contour);

        width = BigDecimal.valueOf(rectangle.width).setScale(2, RoundingMode.HALF_EVEN);
        widthDecreased = width.subtract(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        widthIncreased = width.add(width.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        
        height = BigDecimal.valueOf(rectangle.height).setScale(2, RoundingMode.HALF_EVEN);
        heightDecreased = height.subtract(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));
        heightIncreased = height.add(height.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(tollerance));

        areaSize = width.multiply(height);
        decreasedAreaSize = widthDecreased.multiply(heightDecreased);
        increasedAreaSize = widthIncreased.multiply(heightIncreased);

    }


}
