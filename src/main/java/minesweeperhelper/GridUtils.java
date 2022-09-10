package minesweeperhelper;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class GridUtils {

    public static Grid getGrid(Mat srcImage, List<MatOfPoint> contours, Map<Integer, List<Rect>> numbersLocations) {

        List<Rect> rectList = contours.stream().map(p -> Imgproc.boundingRect(p)).collect(Collectors.toList());

        rectList = rectList.stream().filter(p -> p.area() > 20).collect(Collectors.toList());

        Map<Integer, List<Rect>> mapByAreaSize = GroupingBy.approximate(rectList, p -> (int) p.area(), Grid.TOLLERANCE_IN_PERCENT);

        int minGridCellsCount = Grid.MIN_WIDTH * Grid.MIN_HEIGHT;

        mapByAreaSize = mapByAreaSize.entrySet().stream().filter(i -> i.getValue().size() >= minGridCellsCount).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));

        rectList = mapByAreaSize.entrySet().stream().flatMap(i -> i.getValue().stream()).collect(Collectors.toList());

        Map<Integer, List<Rect>> mapByWidth = GroupingBy.approximate(rectList, p -> p.width, Grid.TOLLERANCE_IN_PERCENT);

        Map<Integer, Map<Integer, List<Rect>>> mapByWidthAndHeight = new HashMap<>();

        for (Integer key : mapByWidth.keySet()) {
            Map<Integer, List<Rect>> mapByHeight = GroupingBy.approximate(mapByWidth.get(key), p -> p.height, Grid.TOLLERANCE_IN_PERCENT);
            mapByHeight = mapByHeight.entrySet().stream().filter(i -> i.getValue().size() >= minGridCellsCount).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
            mapByWidthAndHeight.put(key, mapByHeight);
        }

        for (Integer widthKey : mapByWidthAndHeight.keySet()) {
            for (Integer heightKey : mapByWidthAndHeight.get(widthKey).keySet()) {

                return collectGrid(srcImage, mapByWidthAndHeight.get(widthKey).get(heightKey), numbersLocations);

            }
        }

        return null;

    }

    private static Grid collectGrid(Mat srcImage, List<Rect> rects, Map<Integer, List<Rect>> numbersLocations) {

        Rect top = rects.stream().min(Comparator.comparingInt(p -> p.y)).orElse(null);

        if (top != null) {
            List<Rect> topLine = new ArrayList();

            while (topLine != null && topLine.size() < Grid.MIN_WIDTH) {

                final Rect topFinal = top;

                topLine = rects.stream().filter(p -> Math.abs(p.y - topFinal.y) <= (double) topFinal.height / 100 * Grid.TOLLERANCE_IN_PERCENT)
                        .collect(Collectors.toList());

                if (topLine.size() == 0)
                    topLine = null;
                else if (topLine.size() < Grid.MIN_WIDTH) {
                    rects.remove(top);
                    top = rects.stream().min(Comparator.comparingInt(p -> p.y)).orElse(null);
                }

            }

            if (topLine != null) {

                topLine.sort(Comparator.comparingInt(p -> p.x));

                if (checkLineIntegrity(topLine)) {

                    rects.removeAll(topLine);
                    List<List<Rect>> allLines = getAllLines(rects, topLine, Grid.TOLLERANCE_IN_PERCENT);

                    return new Grid(srcImage, allLines, numbersLocations);

                }

            }
        }

        return null;
    }

    private static List<List<Rect>> getAllLines(List<Rect> rects, List<Rect> topLine, int tolleranceInPercent) {

        List<List<Rect>> restLines = new ArrayList<>();
        restLines.add(topLine);

        while (true) {
            List<Rect> topLineFinal = topLine;
            List<Rect> newLine = rects.stream()
                    .filter(
                            p -> p.y - (topLineFinal.get(0).y + topLineFinal.get(0).height) >= 0
                                    && p.y - (topLineFinal.get(0).y + topLineFinal.get(0).height) <= (double) topLineFinal.get(0).height / 100 * tolleranceInPercent
                                    && p.x >= topLineFinal.get(0).x - (double) topLineFinal.get(0).width / 100 * tolleranceInPercent
                                    && p.x + p.width <= topLineFinal.get(topLineFinal.size() - 1).x + topLineFinal.get(topLineFinal.size() - 1).width + (double) topLineFinal.get(0).width / 100 * tolleranceInPercent
                    )
                    .collect(Collectors.toList());
            newLine.sort(Comparator.comparingInt(p -> p.x));
            if (topLine.size() == newLine.size() && checkLineIntegrity(newLine)) {
                restLines.add(newLine);
                topLine = newLine;
            } else {
                return restLines;
            }
        }
    }

    private static boolean checkLineIntegrity(List<Rect> topLine) {

        for (int i = 1; i < topLine.size(); i++) {
            if (topLine.get(i).x < topLine.get(i - 1).x + topLine.get(i - 1).width) {
                return false;
            }
        }

        return true;
    }

}
