package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
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

    public static Map<BigDecimal, Map<BigDecimal, List<Grid>>> collectGrids(Mat screenShot,
            int minGridHorizontalMembers,
            int minGridVerticalMembers, BigDecimal gridPositionAndSizeTolleranceInPercent) {

        Map<BigDecimal, Map<BigDecimal, List<Grid>>> mapGridsByWidthAndHeight = new HashMap<>();

        Mat grayMat = new Mat();
        Imgproc.cvtColor(screenShot, grayMat, Imgproc.COLOR_BGR2GRAY);

        Mat thresholdMat = new Mat();
        Imgproc.threshold(grayMat, thresholdMat, 127, 255, Imgproc.THRESH_BINARY);

        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresholdMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        Map<BigDecimal, Map<BigDecimal, List<GridCell>>> mapByWidthAndHeight = GridUtils.groupByWidthThenByHeight(
                contours,
                minGridHorizontalMembers, minGridVerticalMembers, gridPositionAndSizeTolleranceInPercent);

        for (Map.Entry<BigDecimal, Map<BigDecimal, List<GridCell>>> entry : mapByWidthAndHeight.entrySet()) {
            BigDecimal width = entry.getKey(); // cell width
            Map<BigDecimal, List<GridCell>> mapByHeight = entry.getValue();
            for (Map.Entry<BigDecimal, List<GridCell>> entryByHeight : mapByHeight.entrySet()) {
                BigDecimal height = entryByHeight.getKey(); // cell height
                List<GridCell> points = entryByHeight.getValue();

                Map<BigDecimal, List<GridCell>> mapByX = GroupingBy.approximateInArea(points,
                        p -> p.getRect().x,
                        p -> p.getRect().width, gridPositionAndSizeTolleranceInPercent);

                Map<BigDecimal, List<GridCell>> mapByY = GroupingBy.approximateInArea(points,
                        p -> p.getRect().y,
                        p -> p.getRect().height, gridPositionAndSizeTolleranceInPercent);

                mapByY = mapByY.entrySet().stream().filter(p -> p.getValue().size() >= minGridVerticalMembers)
                        .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

                List<Map<BigDecimal, List<GridCell>>> listOfxyMaps = GridUtils
                        .removeSquaresToConformMinWidthAndHeight(mapByX, mapByY, minGridHorizontalMembers,
                                minGridVerticalMembers,
                                gridPositionAndSizeTolleranceInPercent);

                mapByX = listOfxyMaps.get(0);
                mapByY = listOfxyMaps.get(1);

                List<Grid> gridList = collectGridsFromCells(mapByX, mapByY, width, height,
                        gridPositionAndSizeTolleranceInPercent);

                if (gridList.size() > 0) {
                    Map<BigDecimal, List<Grid>> returnMapByHeight = mapGridsByWidthAndHeight.get(width);
                    if (returnMapByHeight == null)
                        returnMapByHeight = new HashMap<>();

                    returnMapByHeight.put(height, gridList);

                    mapGridsByWidthAndHeight.put(width, returnMapByHeight);
                }
            }
        }

        return mapGridsByWidthAndHeight;
    }

    public static List<Grid> collectGridsFromCells(Map<BigDecimal, List<GridCell>> mapByX,
            Map<BigDecimal, List<GridCell>> mapByY, BigDecimal width, BigDecimal height,
            BigDecimal tolleranceInPercent) {
        List<Grid> gridList = new ArrayList<>();
        List<BigDecimal> xs = mapByX.keySet().stream().sorted().collect(Collectors.toList());
        List<BigDecimal> ys = mapByY.keySet().stream().sorted().collect(Collectors.toList());

        List<List<Integer>> xsIntervals = getIntervals(xs, width, tolleranceInPercent);
        List<Integer> xsStartPos = xsIntervals.get(0);
        List<Integer> xsEndPos = xsIntervals.get(1);

        List<List<Integer>> ysIntervals = getIntervals(ys, height, tolleranceInPercent);
        List<Integer> ysStartPos = ysIntervals.get(0);
        List<Integer> ysEndPos = ysIntervals.get(1);

        for (int i = 0; i < xsStartPos.size(); i++) {
            int xStart = xsStartPos.get(i);
            int xEnd = xsEndPos.get(i);
            Set<GridCell> xsSet = mapByX.entrySet().stream()
                    .filter(p -> p.getKey().compareTo(xs.get(xStart)) >= 0 && p.getKey().compareTo(xs.get(xEnd)) <= 0)
                    .flatMap(p -> p.getValue().stream()).collect(Collectors.toSet());
            for (int j = 0; j < ysStartPos.size(); j++) {
                int yStart = ysStartPos.get(j);
                int yEnd = ysEndPos.get(j);
                Set<GridCell> ysSet = mapByY.entrySet().stream()
                        .filter(p -> p.getKey().compareTo(ys.get(yStart)) >= 0
                                && p.getKey().compareTo(ys.get(yEnd)) <= 0)
                        .flatMap(p -> p.getValue().stream()).collect(Collectors.toSet());

                Set<GridCell> xGridSet = new HashSet<GridCell>(xsSet);
                xGridSet.retainAll(ysSet);

                Set<GridCell> yGridSet = new HashSet<GridCell>(ysSet);
                yGridSet.retainAll(xsSet);

                Map<BigDecimal, List<GridCell>> mapByXGrid = GroupingBy.approximateInArea(
                        xGridSet.stream().collect(Collectors.toList()),
                        p -> p.getRect().x,
                        p -> p.getRect().width, tolleranceInPercent);

                Map<BigDecimal, List<GridCell>> mapByYGrid = GroupingBy.approximateInArea(
                        yGridSet.stream().collect(Collectors.toList()),
                        p -> p.getRect().y,
                        p -> p.getRect().height, tolleranceInPercent);

                int counter = -1;
                int lastIndexX = -1;
                for (BigDecimal xValue : mapByXGrid.keySet().stream().sorted().collect(Collectors.toList())) {
                    ++counter;
                    for (GridCell gridCell : mapByXGrid.get(xValue)) {
                        gridCell.setPositionInGridX(counter);
                    }
                }
                lastIndexX = counter;

                counter = -1;
                int lastIndexY = -1;
                for (BigDecimal yValue : mapByYGrid.keySet().stream().sorted().collect(Collectors.toList())) {
                    ++counter;
                    for (GridCell gridCell : mapByYGrid.get(yValue)) {
                        gridCell.setPositionInGridY(counter);
                    }
                }
                lastIndexY = counter;

                Grid grid = new Grid(lastIndexX + 1, lastIndexY + 1);

                for (BigDecimal xValue : mapByXGrid.keySet()) {
                    for (GridCell gridCell : mapByXGrid.get(xValue)) {
                        grid.setCell(gridCell.getX(), gridCell.getY(), gridCell);
                    }
                }

                gridList.add(grid);
            }
        }

        return gridList;
    }

    /*
     * index
     * 0 - start positions
     * 1 - end positions
     */
    public static List<List<Integer>> getIntervals(List<BigDecimal> list, BigDecimal widthOrHeight,
            BigDecimal tolleranceInPercent) {
        List<Integer> startPos = new ArrayList<>();
        List<Integer> endPos = new ArrayList<>();
        List<List<Integer>> retList = Arrays.asList(startPos, endPos);

        if (list.size() > 0) {
            int startingPos = 0;
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i - 1).add(widthOrHeight).subtract(list.get(i)).abs().compareTo(
                        widthOrHeight.divide(BigDecimal.valueOf(100)).multiply(tolleranceInPercent)) > 0) {
                    startPos.add(startingPos);
                    endPos.add(i - 1);
                    startingPos = i;
                }
            }
            startPos.add(startingPos);
            endPos.add(list.size() - 1);
        }
        return retList;
    }

    @Deprecated
    public static Board collectGrid(List<MineSweeperGridCell> cells, BigDecimal tolleranceInPercent) {

        if (cells.size() > 0) {

            Map<BigDecimal, List<MineSweeperGridCell>> mapByX = GroupingBy.approximateInArea(cells,
                    p -> (int) p.getRect().x,
                    p -> (int) p.getRect().width, tolleranceInPercent);

            // remove dublicates if any
            for (BigDecimal x : mapByX.keySet()) {
                List<MineSweeperGridCell> list = mapByX.get(x);
                for (int i = 0; i < list.size() - 1; i++) {
                    MineSweeperGridCell gridCell = list.get(i);
                    Iterator<MineSweeperGridCell> iterator = list.iterator();
                    MineSweeperGridCell gridCellIter = null;
                    for (int j = 0; j < i; j++) {
                        if (iterator.hasNext())
                            iterator.next();
                    }
                    while (iterator.hasNext()) {
                        gridCellIter = iterator.next();

                        if (gridCellIter != null && gridCell.getId().compareTo(gridCellIter.getId()) != 0 &&

                                BigDecimal.valueOf(gridCell.getRect().y).setScale(2, RoundingMode.HALF_EVEN)
                                        .subtract(BigDecimal.valueOf(gridCellIter.getRect().y).setScale(2,
                                                RoundingMode.HALF_EVEN))
                                        .abs()
                                        .compareTo(
                                                BigDecimal.valueOf(gridCellIter.getRect().height)
                                                        .setScale(2, RoundingMode.HALF_EVEN)
                                                        .divide(BigDecimal.valueOf(100)).multiply(tolleranceInPercent)

                                        ) <= 0

                        ) {
                            iterator.remove();
                        }
                    }
                }
            }

            // check all columns have the same rows count
            int rows = -1;
            for (BigDecimal x : mapByX.keySet()) {
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

            List<BigDecimal> xs = new ArrayList<>(mapByX.keySet());
            xs.sort((a, b) -> a.compareTo(b));

            Board grid = new Board(xs.size(), mapByX.get(xs.get(0)).size());

            // filling grid
            int column = -1;
            for (BigDecimal x : xs) {
                ++column;
                List<MineSweeperGridCell> columnData = mapByX.get(x);
                List<BigDecimal> ys = columnData.stream()
                        .map(p -> BigDecimal.valueOf(p.getRect().y).setScale(2, RoundingMode.HALF_EVEN))
                        .collect(Collectors.toList());
                ys.sort((a, b) -> a.compareTo(b));
                int row = -1;
                for (BigDecimal y : ys) {
                    ++row;
                    grid.setCell(column, row, columnData.stream().filter(
                            p -> BigDecimal.valueOf(p.getRect().y).setScale(2, RoundingMode.HALF_EVEN)
                                    .compareTo(y) == 0)
                            .findFirst().get());
                }
            }

            return grid;
        }

        log.info("unable to get grid, r4");
        return null;
    }

    public static Mat printHelpInfo(Mat mat, MineSweeperGridCell gridCell) {
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

    public static Mat printDebugInfo(Mat mat, MineSweeperGridCell gridCell) {
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

    public static Mat drawLocations(Mat mat, Grid grid) {

        for (int i = 0; i < grid.getGrid().length; i++) {
            for (int j = 0; j < grid.getGrid()[i].length; j++) {
                mat = drawLocation(mat, grid.getGrid()[i][j]);
            }
        }
        return mat;
    }

    public static Mat drawLocation(Mat mat, GridCell gridCell) {

        if (gridCell != null) {
            Scalar color = new Scalar(0, 255, 0);
            Rect rect = gridCell.getRect();
            Imgproc.rectangle(mat, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height), color, 1);
        }
        return mat;

    }

    public static Map<BigDecimal, Map<BigDecimal, List<GridCell>>> groupByWidthThenByHeight(List<MatOfPoint> contours,
            int minimumHorizontalCount, int minimumVerticalCout, BigDecimal tolleranceInPercent) {
        Map<BigDecimal, Map<BigDecimal, List<GridCell>>> map = new HashMap<>();
        // map by width
        Map<BigDecimal, List<GridCell>> mapByWidth = convertToGridCells(GroupingBy.approximate(contours,
                p -> {
                    Rect rect = Imgproc.boundingRect(p);
                    return rect.width;
                },
                tolleranceInPercent));

        List<Map.Entry<BigDecimal, List<GridCell>>> filteredByWidth = mapByWidth.entrySet().stream()
                .filter(p -> p.getValue().size() >= minimumHorizontalCount * minimumVerticalCout)
                .collect(Collectors.toList());

        for (Map.Entry<BigDecimal, List<GridCell>> listByWidth : filteredByWidth) {
            // map by height
            Map<BigDecimal, List<GridCell>> mapByHeight = GroupingBy.approximate(listByWidth.getValue(),
                    p -> p.getRect().height,
                    tolleranceInPercent);
            Map<BigDecimal, List<GridCell>> filteredByHeight = mapByHeight.entrySet().stream()
                    .filter(p -> p.getValue().size() >= minimumHorizontalCount * minimumVerticalCout)
                    .collect(Collectors.toMap(p -> p.getKey(), v -> v.getValue()));

            map.put(listByWidth.getKey(), filteredByHeight);
        }
        return map;
    }

    private static Map<BigDecimal, List<GridCell>> convertToGridCells(Map<BigDecimal, List<MatOfPoint>> map) {
        Map<BigDecimal, List<GridCell>> mapR = map.entrySet().stream()
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

    /*
     * index
     * 0 - mapByX
     * 1 - mapByY
     */
    public static List<Map<BigDecimal, List<GridCell>>> removeSquaresToConformMinWidthAndHeight(
            Map<BigDecimal, List<GridCell>> mapByX, Map<BigDecimal, List<GridCell>> mapByY, int minWidth, int minHeight,
            BigDecimal tolleranceInPercent) {

        long beforeByXCount = mapByX.entrySet().stream().flatMap(p -> p.getValue().stream()).count();

        // romeve if absent in mapByY
        Set<UUID> mapByYIDs = mapByY.entrySet().stream().flatMap(p -> p.getValue().stream()).map(p -> p.getId())
                .collect(Collectors.toSet());

        mapByX = mapByX.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), v -> {
            List<GridCell> list = v.getValue().stream().filter(i -> mapByYIDs.contains(i.getId()))
                    .collect(Collectors.toList());
            list = removeCellsToConformSequency(list, p -> p.getRect().x, p -> p.getRect().width, minWidth,
                    tolleranceInPercent);
            return list;
        }));

        mapByX = mapByX.entrySet().stream().filter(p -> p.getValue().size() >= minWidth)
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

        long afterByXCount = mapByX.entrySet().stream().flatMap(p -> p.getValue().stream()).count();

        long beforeByYCount = mapByY.entrySet().stream().flatMap(p -> p.getValue().stream()).count();

        // romeve if absent in mapByY
        Set<UUID> mapByXIDs = mapByX.entrySet().stream().flatMap(p -> p.getValue().stream()).map(p -> p.getId())
                .collect(Collectors.toSet());

        mapByY = mapByY.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), v -> {
            List<GridCell> list = v.getValue().stream().filter(i -> mapByXIDs.contains(i.getId()))
                    .collect(Collectors.toList());
            list = removeCellsToConformSequency(list, p -> p.getRect().y, p -> p.getRect().height, minHeight,
                    tolleranceInPercent);
            return list;
        }));

        mapByY = mapByY.entrySet().stream().filter(p -> p.getValue().size() >= minHeight)
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

        long afterByYCount = mapByY.entrySet().stream().flatMap(p -> p.getValue().stream()).count();

        if (mapByX.size() > 0 && mapByY.size() > 0
                && (beforeByXCount != afterByXCount || beforeByYCount != afterByYCount)) {
            return removeSquaresToConformMinWidthAndHeight(mapByX, mapByY, minWidth, minHeight, tolleranceInPercent);
        }

        if (mapByX.size() > 0 && mapByY.size() > 0)
            return Arrays.asList(mapByX, mapByY);
        else
            return Arrays.asList(new HashMap<>(), new HashMap<>());
    }

    public static List<GridCell> removeCellsToConformSequency(List<GridCell> list,
            Function<GridCell, Integer> functionPosition, Function<GridCell, Integer> functionWidthOrHeight,
            int minWidthOrHeightCount, BigDecimal tolleranceInPercent) {

        if (list.size() > minWidthOrHeightCount) {

            List<GridCell> listToRemove = new ArrayList<>();

            list = list.stream()
                    .sorted((a, b) -> BigDecimal.valueOf(functionPosition.apply(a))
                            .compareTo(BigDecimal.valueOf(functionPosition.apply(b))))
                    .collect(Collectors.toList());

            int startingPosition = -1;
            int counter = 0;

            for (int i = 1; i < list.size(); i++) {

                if (startingPosition < 0)
                    startingPosition = i;

                BigDecimal prevPos = BigDecimal.valueOf(functionPosition.apply(list.get(i - 1))).setScale(2,
                        RoundingMode.HALF_EVEN);
                BigDecimal thisPos = BigDecimal.valueOf(functionPosition.apply(list.get(i))).setScale(2,
                        RoundingMode.HALF_EVEN);
                BigDecimal prevWidthOrHeight = BigDecimal.valueOf(functionWidthOrHeight.apply(list.get(i - 1)))
                        .setScale(2, RoundingMode.HALF_EVEN);

                if (prevWidthOrHeight.divide(BigDecimal.valueOf(100)).multiply(tolleranceInPercent)
                        .compareTo(prevPos.add(prevWidthOrHeight).subtract(thisPos).abs()) <= 0) {
                    ++counter;
                } else {
                    if (counter < minWidthOrHeightCount)
                        for (int j = startingPosition; j <= i - 1; j++) {
                            listToRemove.add(list.get(j));
                        }

                    counter = 1;
                    startingPosition = i;
                }
            }
            if (list.size() - listToRemove.size() >= minWidthOrHeightCount) {
                list.removeAll(listToRemove);
                return list;
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    private List<GridCell> getNeighbourCells(List<GridCell> list, GridCell cell) {
        return list.stream().filter(
                p -> (p.getX() - 1 == cell.getX() && p.getY() - 1 == cell.getY()) ||
                        (p.getX() == cell.getX() && p.getY() - 1 == cell.getY()) ||
                        (p.getX() + 1 == cell.getX() && p.getY() - 1 == cell.getY()) ||
                        (p.getX() + 1 == cell.getX() && p.getY() == cell.getY()) ||
                        (p.getX() + 1 == cell.getX() && p.getY() + 1 == cell.getY()) ||
                        (p.getX() == cell.getX() && p.getY() + 1 == cell.getY()) ||
                        (p.getX() - 1 == cell.getX() && p.getY() + 1 == cell.getY()) ||
                        (p.getX() - 1 == cell.getX() && p.getY() == cell.getY()))
                .collect(Collectors.toList());
    }

    /*
     * public <T,N,K,L> Map<T,List<N>> removeByCondition(Map<T,List<N>> list,
     * Function<K, L> conditionToRemove){
     * 
     * }
     */

}
