package minesweeperhelper;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {

    private static final Logger logger = LogManager.getLogger(ImageProcessing.class);

    public Grid processView(Mat srcImage) {

        List<GridCell> cells = findCells(srcImage);

        return GridUtils.collectGrid(cells);
    }

    private List<GridCell> findCells(Mat srcImage) {
        List<GridCell> list = new ArrayList<>();

        int machMethod = Imgproc.TM_CCOEFF_NORMED;

        Mat srcForOutput = srcImage.clone();

        for (int i = 10; i >= 0; i--) {

            List<Rect> numbersLocations = new ArrayList<>();

            Mat numberImage = Imgcodecs.imread("src/main/resources/" + i + ".png");
            Imgproc.cvtColor(numberImage, numberImage, Imgproc.COLOR_BGR2BGRA);

            Mat outputImage = new Mat();
            Imgproc.matchTemplate(srcImage, numberImage, outputImage, machMethod);

            logger.debug("match finish for " + i);

            Core.MinMaxLocResult mmr = null;

            while (true) {
                mmr = Core.minMaxLoc(outputImage);
                if (mmr.maxVal >= 0.8) {
                    Rect rect = new Rect(mmr.maxLoc,
                            new Point(mmr.maxLoc.x + numberImage.cols(), mmr.maxLoc.y + numberImage.rows()));
                    numbersLocations.add(rect);

                    Imgproc.rectangle(srcForOutput, mmr.maxLoc,
                            new Point(mmr.maxLoc.x + numberImage.cols(), mmr.maxLoc.y + numberImage.rows()),
                            new Scalar(0, 255, 0, 255));

                    Imgproc.circle(outputImage, new Point(mmr.maxLoc.x, mmr.maxLoc.y),
                            (numberImage.width() + numberImage.height()) / 4,
                            new Scalar(0, 0, 0), -1);

                    CellTypeEnum cellTypeEnum = null;

                    if (i > 0 && i < 9)
                        cellTypeEnum = CellTypeEnum.NUMBER;
                    if (i == 0)
                        cellTypeEnum = CellTypeEnum.EMPTY;
                    if (i == 9)
                        cellTypeEnum = CellTypeEnum.FLAG;
                    if (i == 10)
                        cellTypeEnum = CellTypeEnum.UNCHECKED;

                    GridCell gridCell = new GridCell(cellTypeEnum, rect, (i >= 0 && i < 9) ? i : -1);

                    list.add(gridCell);

                } else {
                    break;
                }
            }

            logger.debug(i + " " + numbersLocations.size());

        }

        return list;
    }

    public static double calibrateScreenShot(Mat screenShot) {

        Mat grayMat = new Mat();
        Imgproc.cvtColor(screenShot, grayMat, Imgproc.COLOR_BGR2GRAY);

        Mat thresholdMat = new Mat();
        Imgproc.threshold(grayMat, thresholdMat, 127, 255, Imgproc.THRESH_BINARY);

        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresholdMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        int start = LocalTime.now().getSecond();

        // map by width
        Map<Integer, List<MatOfPoint>> mapByWidth = GroupingBy.approximate(contours,
                p -> {
                    Rect rect = Imgproc.boundingRect(p);
                    return rect.width;
                },
                Grid.TOLLERANCE_IN_PERCENT);

        for (List<MatOfPoint> listByWidth : mapByWidth.values()) {
            // map by height
            Map<Integer, List<MatOfPoint>> mapByHeight = GroupingBy.approximate(contours,
                    p -> {
                        Rect rect = Imgproc.boundingRect(p);
                        return rect.height;
                    },
                    Grid.TOLLERANCE_IN_PERCENT);
                    
        }

        // same maps entries list
        List<Map.Entry<Integer, List<MatOfPoint>>> list = mapByArea.entrySet().stream()
                .filter(p -> p.getValue().size() >= Grid.MIN_HEIGHT * Grid.MIN_WIDTH).collect(Collectors.toList());

        // cycle by area
        for (int i = 0; i < list.size(); i++) {
            List<MatOfPoint> listSameArea = list.get(i).getValue();

            // same area, maped by x
            Map<Integer, List<MatOfPoint>> mapByX = GroupingBy.approximateInArea(listSameArea,
                    p -> {
                        Rect rect = Imgproc.boundingRect(p);
                        return (int) rect.x;
                    },
                    p -> {
                        Rect rect = Imgproc.boundingRect(p);
                        return (int) rect.width;
                    }, Grid.TOLLERANCE_IN_PERCENT);

            // remove colums with less than minimum size
            List<Map.Entry<Integer, List<MatOfPoint>>> filteredMapByX = mapByX.entrySet().stream()
                    .filter(p -> p.getValue().size() >= Grid.MIN_HEIGHT).collect(Collectors.toList());

            // filtered area, maped by y
            listSameArea = filteredMapByX.stream().flatMap(p -> p.getValue().stream()).collect(Collectors.toList());
            Map<Integer, List<MatOfPoint>> mapByY = GroupingBy.approximateInArea(listSameArea,
                    p -> {
                        Rect rect = Imgproc.boundingRect(p);
                        return (int) rect.y;
                    },
                    p -> {
                        Rect rect = Imgproc.boundingRect(p);
                        return (int) rect.height;
                    }, Grid.TOLLERANCE_IN_PERCENT);

            // remove rows with less than minimum size
            List<Map.Entry<Integer, List<MatOfPoint>>> filteredMapByY = mapByY.entrySet().stream()
                    .filter(p -> p.getValue().size() >= Grid.MIN_WIDTH).collect(Collectors.toList());

            // remove entries from maped by x
            Iterator<Map.Entry<Integer, List<MatOfPoint>>> iterator = filteredMapByX.iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, List<MatOfPoint>> entry = iterator.next();
                if (!filteredMapByY.contains(entry)) {
                    iterator.remove();
                }
            }

        }

        int stop = LocalTime.now().getSecond();

        logger.info("laikas " + (stop - start));

        Mat drawing = new Mat();
        screenShot.copyTo(drawing);

        for (int op = 0; op < list.size(); op++)
            for (int i = 0; i < list.get(op).getValue().size(); i++) {
                for (MatOfPoint matOfPoint : list.get(op).getValue()) {
                    Scalar color = new Scalar(0, 255, 0);
                    Rect rect = Imgproc.boundingRect(matOfPoint);
                    Imgproc.rectangle(drawing, new Point(rect.x, rect.y),
                            new Point(rect.x + rect.width, rect.y + rect.height), color, 5);
                }
            }

        // Imgcodecs.imwrite("/Users/agnegv/Desktop/andrius/test.jpg", hierarchy);
        Imgcodecs.imwrite("c:/andrius/test.jpg", drawing);

        return 1;
    }
}
