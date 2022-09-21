package minesweeperhelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class GridUtils {

    private static Logger log = LogManager.getLogger(GridUtils.class);

    public static Grid collectGrid(List<GridCell> cells) {

        if (cells.size() > 0) {

            Map<Integer, List<GridCell>> mapByX = GroupingBy.approximateInArea(cells, p -> (int) p.getRect().x,
                    p -> (int) p.getRect().width, Grid.TOLLERANCE_IN_PERCENT);

            // remove dublicates if any
            for (Integer x : mapByX.keySet()) {
                List<GridCell> list = mapByX.get(x);
                for (int i = 0; i < list.size() - 1; i++) {
                    GridCell gridCell = list.get(i);
                    Iterator<GridCell> iterator = list.iterator();
                    GridCell gridCellIter = null;
                    for (int j = 0; j < i; j++) {
                        if (iterator.hasNext())
                            iterator.next();
                    }
                    while (iterator.hasNext()) {
                        gridCellIter = iterator.next();

                        if (gridCellIter != null && gridCell.getId().compareTo(gridCellIter.getId()) != 0 &&
                                Math.abs(gridCell.getRect().y
                                        - gridCellIter.getRect().y) <= (double) gridCellIter.getRect().height / 100
                                                * Grid.TOLLERANCE_IN_PERCENT) {
                            iterator.remove();
                        }
                    }
                }
            }

            // check all columns have the same rows count
            int rows = -1;
            for (Integer x : mapByX.keySet()) {
                mapByX.get(x).sort((a, b) -> Integer.compare(a.getRect().y, b.getRect().y));
                if (rows < 0) {
                    rows = mapByX.get(x).size();
                } else {
                    if (mapByX.get(x).size() != rows) {
                        log.info("unable to get grid, r1");
                        return null;
                    }
                }
            }

            List<Integer> xs = new ArrayList<>(mapByX.keySet());
            xs.sort((a, b) -> Integer.compare(a, b));

            Grid grid = new Grid(xs.size(), mapByX.get(xs.get(0)).size());

            // filling grid
            int column = -1;
            for (Integer x : xs) {
                ++column;
                List<GridCell> columnData = mapByX.get(x);
                List<Integer> ys = columnData.stream().map(p -> p.getRect().y).collect(Collectors.toList());
                ys.sort((a, b) -> Integer.compare(a, b));
                int row = -1;
                for (Integer y : ys) {
                    ++row;
                    grid.setCell(column, row, columnData.stream().filter(p -> p.getRect().y == y).findFirst().get());
                }
            }

            return grid;
        }

        log.info("unable to get grid, r4");
        return null;
    }

    public static Mat printHelpInfo(Mat mat, GridCell gridCell) {
        Point position = new Point(
                gridCell.getRect().x + (double) gridCell.getRect().width / 100 * 20,
                gridCell.getRect().y + (double) gridCell.getRect().height / 100 * 20);
        Scalar color = null;

        if (gridCell.getCellTypeEnum().equals(CellTypeEnum.NEEDS_TO_BE_CHECKED)) {
            color = new Scalar(0, 255, 0);
        }

        if (gridCell.getCellTypeEnum().equals(CellTypeEnum.FLAG)) {
            color = new Scalar(0, 0, 255);
        }

        Imgproc.circle(mat, position, gridCell.getRect().height / 5, color, -1);

        return mat;
    }

    public static Mat printDebugInfo(Mat mat, GridCell gridCell) {
        Point position = new Point(gridCell.getRect().x,
                gridCell.getRect().y + (double) gridCell.getRect().height / 100 * 30);
        Point position2 = new Point(gridCell.getRect().x,
                gridCell.getRect().y + (double) gridCell.getRect().height / 100 * 80);
        Scalar color = new Scalar(0, 0, 255);
        int font = Imgproc.FONT_HERSHEY_PLAIN;
        double scale = 1;
        int thickness = 1;

        Imgproc.putText(mat, String.valueOf(gridCell.getNumber()), position, font, scale, color, thickness);
        Imgproc.putText(mat, gridCell.getCellTypeEnum().name().substring(0, 2), position2, font, scale, color,
                thickness);

        return mat;
    }

    public static Map<Integer, Map<Integer, List<GridCell>>> groupByWidthThenByHeight(List<MatOfPoint> contours,
            int minimumHorizontalCount, int minimumVerticalCout, int tolleranceInPercent) {
        Map<Integer, Map<Integer, List<GridCell>>> map = new HashMap<>();
        // map by width
        Map<Integer, List<GridCell>> mapByWidth = convertToGridCells(GroupingBy.approximate(contours,
                p -> {
                    Rect rect = Imgproc.boundingRect(p);
                    return rect.width;
                },
                tolleranceInPercent));

        List<Map.Entry<Integer, List<GridCell>>> filteredByWidth = mapByWidth.entrySet().stream()
                .filter(p -> p.getValue().size() >= minimumHorizontalCount * minimumVerticalCout)
                .collect(Collectors.toList());

        for (Map.Entry<Integer, List<GridCell>> listByWidth : filteredByWidth) {
            // map by height
            Map<Integer, List<GridCell>> mapByHeight = GroupingBy.approximate(listByWidth.getValue(),
                    p -> p.getRect().height,
                    tolleranceInPercent);
            Map<Integer, List<GridCell>> filteredByHeight = mapByHeight.entrySet().stream()
                    .filter(p -> p.getValue().size() >= minimumHorizontalCount * minimumVerticalCout)
                    .collect(Collectors.toMap(p -> p.getKey(), v -> v.getValue()));

            map.put(listByWidth.getKey(), filteredByHeight);
        }
        return map;
    }

    private static Map<Integer, List<GridCell>> convertToGridCells(Map<Integer, List<MatOfPoint>> map) {
        Map<Integer, List<GridCell>> mapR = map.entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey(),
                        v -> {
                            List<GridCell> list = v.getValue().stream().map(p -> {
                                Rect rect = Imgproc.boundingRect(p);
                                return new GridCell(rect);
                            }).collect(Collectors.toList());
                            return list;
                        }));
        return mapR;
    }

    public static List<Map<Integer, List<GridCell>>> removePointsToConformMinWidthAndHeight(
            Map<Integer, List<GridCell>> mapByX, Map<Integer, List<GridCell>> mapByY) {

        long beforeByXCount = mapByX.entrySet().stream().flatMap(p -> p.getValue().stream()).count();

        // romeve if absent in mapByY
        Set<UUID> mapByYIDs = mapByY.entrySet().stream().flatMap(p -> p.getValue().stream()).map(p -> p.getId())
                .collect(Collectors.toSet());

        mapByX = mapByX.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), v -> {
            List<GridCell> list = v.getValue().stream().filter(i -> mapByYIDs.contains(i.getId()))
                    .collect(Collectors.toList());
            return list;
        }));

        mapByX = mapByX.entrySet().stream().filter(p -> p.getValue().size() >= Grid.MIN_WIDTH)
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

        long afterByXCount = mapByX.entrySet().stream().flatMap(p -> p.getValue().stream()).count();

        long beforeByYCount = mapByY.entrySet().stream().flatMap(p -> p.getValue().stream()).count();

        // romeve if absent in mapByY
        Set<UUID> mapByXIDs = mapByX.entrySet().stream().flatMap(p -> p.getValue().stream()).map(p -> p.getId())
                .collect(Collectors.toSet());

        mapByY = mapByY.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), v -> {
            List<GridCell> list = v.getValue().stream().filter(i -> mapByXIDs.contains(i.getId()))
                    .collect(Collectors.toList());
            return list;
        }));

        mapByY = mapByY.entrySet().stream().filter(p -> p.getValue().size() >= Grid.MIN_HEIGHT)
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

        long afterByYCount = mapByY.entrySet().stream().flatMap(p -> p.getValue().stream()).count();

        if (mapByX.size() > 0 && mapByY.size() > 0
                && (beforeByXCount != afterByXCount || beforeByYCount != afterByYCount)) {
            return removePointsToConformMinWidthAndHeight(mapByX, mapByY);
        }

        if (mapByX.size() > 0 && mapByY.size() > 0)
            return Arrays.asList(mapByX, mapByY);
        else
            return Arrays.asList(new HashMap<>(), new HashMap<>());
    }

    /*
     * public <T,N,K,L> Map<T,List<N>> removeByCondition(Map<T,List<N>> list,
     * Function<K, L> conditionToRemove){
     * 
     * }
     */

}
