package minesweeperhelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ProcessingServiceTest {
        @Test
        void runTest() {

                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

                Mat screenShot = Imgcodecs.imread("debug_aaa1_screenshot.png");

                Mat gray = new Mat();
                Imgproc.cvtColor(screenShot, gray, Imgproc.COLOR_BGR2GRAY);
                Imgproc.Canny(gray, gray, 50, 200, 3, false);

                Mat cdst = new Mat();
                Imgproc.cvtColor(gray, cdst, Imgproc.COLOR_GRAY2BGR);

                Mat linesMat = new Mat();
                Imgproc.HoughLinesP(gray, linesMat, 1, Math.PI / 180, 50, 50, 10);

                List<double[]> lines = new ArrayList<>();

                for (int i = 0; i < linesMat.rows(); i++) {
                        double[] coord = linesMat.get(i, 0);
                        lines.add(coord);
                        Point point1 = new Point(coord[0], coord[1]);
                        Point point2 = new Point(coord[2], coord[3]);
                        Imgproc.line(cdst, point1, point2,
                                        new Scalar(0, 0, 255), 3,
                                        Imgproc.LINE_AA, 0);
                }

                List<LineArea> lineAreas = lines.stream()
                                .map(p -> new LineArea(p, BigDecimal.valueOf(5).setScale(2))).toList();


                                for(){
                                        for(){
                                                Point intersectionPoint = GridUtils.getLinesItersection(point1, point2);
                                        }
                                }

                List<LineArea> lineAreasHorizontal = lineAreas.stream().filter(p -> p.isHorizontal()).toList();
                List<LineArea> lineAreasVertical = lineAreas.stream().filter(p -> p.isVertical()).toList();

                Map<BigDecimal, ListArea<LineArea>> mapByLength = GridUtils.groupByInCollecting(lineAreas,
                                p -> p.length,
                                p -> p.lengthDecreased);

                Imgcodecs.imwrite("debug_lines.png", cdst);

                /*
                 * Mat screenShot = Imgcodecs.imread("debug_aaa1_screenshot.png");
                 * 
                 * ProcessingService service = new ProcessingService(screenShot,
                 * ControllerMain.MIN_WIDTH, ControllerMain.MIN_HEIGHT,
                 * ControllerMain.TOLLERANCE_IN_PERCENT);
                 * service.prepareData();
                 */
        }
}
