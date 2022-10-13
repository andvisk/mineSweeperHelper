package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

                Mat groupedIntersectionsMat = cdst.clone();

                Mat intersectionsMat = cdst.clone();

                Mat linesMat = new Mat();
                Imgproc.HoughLinesP(gray, linesMat, 1, Math.PI / 180, 50, 50, 10);

                List<double[]> lines = new ArrayList<>();

                for (int i = 0; i < linesMat.rows(); i++) {
                        double[] coord = linesMat.get(i, 0);
                        lines.add(coord);
                        Point point1 = new Point(coord[0], coord[1]);
                        Point point2 = new Point(coord[2], coord[3]);

                        if (App.debug)
                                Imgproc.line(cdst, point1, point2,
                                                new Scalar(0, 0, 255), 1,
                                                Imgproc.LINE_AA, 0);
                }

                List<LineArea> lineAreas = lines.stream()
                                .map(p -> new LineArea(p, BigDecimal.valueOf(5).setScale(2))).toList();

                List<LineArea> lineAreasHorizontal = lineAreas.stream().filter(p -> p.isHorizontal()).toList();
                List<LineArea> lineAreasVertical = lineAreas.stream().filter(p -> p.isVertical()).toList();

                List<Intersection> intersections = new ArrayList<>();
                for (LineArea lineH : lineAreasHorizontal) {
                        for (LineArea lineV : lineAreasVertical) {
                                Point intersectionPoint = GridUtils.getLinesItersection(lineH.point1, lineH.point2,
                                                lineV.point1, lineV.point2);
                                if (intersectionPoint != null) {
                                        intersections.add(new Intersection(lineH, lineV, intersectionPoint));

                                        if (App.debug)
                                                Imgproc.circle(intersectionsMat, intersectionPoint, 2,
                                                                new Scalar(0, 255, 0),
                                                                -1);

                                }
                        }
                }

                Map<UUID, Set<Intersection>> mapGroupedIntersections = new HashMap<>();
                for (Intersection intersection : intersections) {
                        Map.Entry<UUID, Set<Intersection>> entryFromMap = mapGroupedIntersections.entrySet()
                                        .stream().filter(
                                                        i -> {
                                                                Intersection inter = i
                                                                                .getValue().stream().filter(
                                                                                                p -> p.lineArea1.id
                                                                                                                .compareTo(
                                                                                                                                intersection.lineArea1.id) == 0

                                                                                                                || p.lineArea1.id
                                                                                                                                .compareTo(
                                                                                                                                                intersection.lineArea2.id) == 0
                                                                                                                || p.lineArea2.id
                                                                                                                                .compareTo(
                                                                                                                                                intersection.lineArea1.id) == 0
                                                                                                                || p.lineArea2.id
                                                                                                                                .compareTo(
                                                                                                                                                intersection.lineArea2.id) == 0)
                                                                                .findFirst().orElse(null);
                                                                if (inter != null)
                                                                        return true;
                                                                else
                                                                        return false;
                                                        })
                                        .findAny().orElse(null);
                        if (entryFromMap != null) {
                                mapGroupedIntersections.get(entryFromMap.getKey()).add(intersection);
                        } else {
                                mapGroupedIntersections.put(UUID.randomUUID(),
                                                new HashSet<>(Arrays.asList(intersection)));
                        }

                }

                Map<UUID, Set<Point>> points = mapGroupedIntersections.entrySet().stream().collect(Collectors.toMap(
                                k -> k.getKey(),
                                v -> v.getValue().stream().map(o -> o.intersectionPoint).collect(Collectors.toSet())));

                if (App.debug) {
                        Random rng = new Random(12345);

                        mapGroupedIntersections.entrySet().stream().forEach(p -> {
                                Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
                                p.getValue().forEach(i -> {
                                        Imgproc.line(groupedIntersectionsMat, i.lineArea1.point1, i.lineArea1.point2,
                                                        color, 1,
                                                        Imgproc.LINE_AA, 0);
                                        Imgproc.line(groupedIntersectionsMat, i.lineArea2.point1, i.lineArea2.point2,
                                                        color, 1,
                                                        Imgproc.LINE_AA, 0);
                                });
                        });
                }

                if (App.debug) {
                        Imgcodecs.imwrite("debug_lines_intersections_grouped.png", groupedIntersectionsMat);
                        Imgcodecs.imwrite("debug_lines.png", cdst);
                        Imgcodecs.imwrite("debug_lines_intersections.png", intersectionsMat);
                }

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
