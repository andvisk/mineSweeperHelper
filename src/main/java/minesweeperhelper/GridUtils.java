package minesweeperhelper;

import java.io.File;
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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class GridUtils {

    private static Logger log = LogManager.getLogger(GridUtils.class);

    public static Map<BigDecimal, Map<BigDecimal, List<Grid>>> collectGrids(Mat screenShot,
            int minGridHorizontalMembers,
            int minGridVerticalMembers, BigDecimal gridPositionAndSizeTolleranceInPercent) {

        Map<BigDecimal, Map<BigDecimal, List<Grid>>> mapGridsByWidthAndHeight = new HashMap<>();

        Mat screenShotGamaCorr = ImageProcessing.gammaCorrection(screenShot, 4.5);

        Mat blueColors = ImageProcessing.detectColor(screenShotGamaCorr, new HsvBlue());
        Mat yellowColors = ImageProcessing.detectColor(screenShot, new HsvYellow());
        Mat whiteColors = ImageProcessing.detectColor(screenShot, new HsvWhite());

        /*
         * Mat grayMat = new Mat();
         * Imgproc.cvtColor(screenShot, grayMat, Imgproc.COLOR_BGR2GRAY);
         * 
         * Mat thresholdMat = new Mat();
         * Imgproc.threshold(grayMat, thresholdMat, ControllerMain.THRESH, 255,
         * Imgproc.THRESH_BINARY);
         */

        List<ContourArea> contoursAll = new ArrayList<>();

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(blueColors, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        contoursAll.addAll(contours.stream().map(p -> new ContourArea(p, ColorsEnum.BLUE)).toList());

        contours = new ArrayList<>();
        Imgproc.findContours(yellowColors, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        contoursAll.addAll(contours.stream().map(p -> new ContourArea(p, ColorsEnum.YELLOW)).toList());

        contours = new ArrayList<>();
        Imgproc.findContours(whiteColors, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        contoursAll.addAll(contours.stream().map(p -> new ContourArea(p, ColorsEnum.WHITE)).toList());

        Map<BigDecimal, Map<BigDecimal, ListReactArea>> mapByWidthAndHeight = GridUtils.groupByWidthThenByHeight(
                screenShot,
                contoursAll,
                minGridHorizontalMembers, minGridVerticalMembers, gridPositionAndSizeTolleranceInPercent);

        if (App.debug)
            printContBoundBoxs(screenShot, mapByWidthAndHeight);

        for (Map.Entry<BigDecimal, Map<BigDecimal, ListReactArea>> entry : mapByWidthAndHeight.entrySet()) {
            BigDecimal width = entry.getKey(); // cell width
            Map<BigDecimal, ListReactArea> mapByHeight = entry.getValue();
            for (Map.Entry<BigDecimal, ListReactArea> entryByHeight : mapByHeight.entrySet()) {
                BigDecimal height = entryByHeight.getKey(); // cell height
                List<RectArea> points = entryByHeight.getValue().list;

                Map<BigDecimal, ListReactArea> mapByX = groupByInCollecting(points, p -> p.x, p -> p.xDecreased);
                Map<BigDecimal, ListReactArea> mapByY = groupByInCollecting(points, p -> p.y, p -> p.yDecreased);

                List<Map<BigDecimal, ListReactArea>> listOfxyMaps = GridUtils
                        .removeSquaresToConformMinWidthAndHeight(mapByX, mapByY, minGridHorizontalMembers,
                                minGridVerticalMembers,
                                gridPositionAndSizeTolleranceInPercent);

                mapByX = listOfxyMaps.get(0);
                mapByY = listOfxyMaps.get(1);

                if (mapByX.size() > 0 && mapByY.size() > 0) {
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
        }

        return mapGridsByWidthAndHeight;

    }

    private static void printContBoundBoxs(Mat screenShot,
            Map<BigDecimal, Map<BigDecimal, ListReactArea>> mapByWidthAndHeight) {
        String dirName = "contours";
        File dir = new File(dirName);
        if (!dir.exists())
            dir.mkdirs();
        else {
            Arrays.asList(dir.listFiles()).stream().forEach(p -> p.delete());
        }

        for (Map.Entry<BigDecimal, Map<BigDecimal, ListReactArea>> entry : mapByWidthAndHeight.entrySet()) {
            BigDecimal width = entry.getKey();
            Map<BigDecimal, ListReactArea> mapByHeight = entry.getValue();
            mapByHeight.entrySet().stream().forEach(
                    h -> {
                        Mat img = screenShot.clone();
                        h.getValue().list.stream().forEach(p -> {
                            GridUtils.drawLocation(img, p, new Scalar(0, 0, 255));
                        });
                        Imgcodecs.imwrite(dirName + "/con_" + width.toString() + "_" + h.getKey().toString() + ".jpg",
                                img);
                    });
        }
    }

    private static List<Grid> collectGridsFromCells(Map<BigDecimal, ListReactArea> mapByX,
            Map<BigDecimal, ListReactArea> mapByY, BigDecimal width, BigDecimal height,
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
            Set<RectArea> xsSet = mapByX.entrySet().stream()
                    .filter(p -> p.getKey().compareTo(xs.get(xStart)) >= 0 && p.getKey().compareTo(xs.get(xEnd)) <= 0)
                    .flatMap(p -> p.getValue().list.stream()).collect(Collectors.toSet());
            for (int j = 0; j < ysStartPos.size(); j++) {
                int yStart = ysStartPos.get(j);
                int yEnd = ysEndPos.get(j);
                Set<RectArea> ysSet = mapByY.entrySet().stream()
                        .filter(p -> p.getKey().compareTo(ys.get(yStart)) >= 0
                                && p.getKey().compareTo(ys.get(yEnd)) <= 0)
                        .flatMap(p -> p.getValue().list.stream()).collect(Collectors.toSet());

                Set<RectArea> xGridSet = new HashSet<RectArea>(xsSet);
                xGridSet.retainAll(ysSet);

                Set<RectArea> yGridSet = new HashSet<RectArea>(ysSet);
                yGridSet.retainAll(xsSet);

                Map<BigDecimal, ListReactArea> mapByXGrid = groupByInCollecting(
                        xGridSet.stream().collect(Collectors.toList()), p -> p.x, p -> p.xDecreased);
                Map<BigDecimal, ListReactArea> mapByYGrid = groupByInCollecting(
                        yGridSet.stream().collect(Collectors.toList()), p -> p.y, p -> p.yDecreased);

                int counter = -1;
                int lastIndexX = -1;
                for (BigDecimal xValue : mapByXGrid.keySet().stream().sorted().collect(Collectors.toList())) {
                    ++counter;
                    for (RectArea gridCell : mapByXGrid.get(xValue).list) {
                        gridCell.positionInGridX = counter;
                    }
                }
                lastIndexX = counter;

                counter = -1;
                int lastIndexY = -1;
                for (BigDecimal yValue : mapByYGrid.keySet().stream().sorted().collect(Collectors.toList())) {
                    ++counter;
                    for (RectArea gridCell : mapByYGrid.get(yValue).list) {
                        gridCell.positionInGridY = counter;
                    }
                }
                lastIndexY = counter;

                Grid grid = new Grid(lastIndexX + 1, lastIndexY + 1);

                for (BigDecimal xValue : mapByXGrid.keySet()) {
                    for (RectArea gridCell : mapByXGrid.get(xValue).list) {
                        grid.setCell(gridCell.positionInGridX, gridCell.positionInGridY, gridCell);
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
                        widthOrHeight.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
                                .multiply(tolleranceInPercent.multiply(BigDecimal.valueOf(2)))) >= 0) { // todo mult by
                                                                                                        // 2 ???
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

    public static Mat printHelpInfo(Mat mat, MineSweeperGridCell gridCell) {
        Point position = new Point(
                gridCell.rectangle.x + (double) gridCell.rectangle.width / 100 * 20,
                gridCell.rectangle.y + (double) gridCell.rectangle.height / 100 * 20);
        Scalar color = null;

        if (gridCell.getCellTypeEnum().equals(CellTypeEnum.NEEDS_TO_BE_CHECKED)) {
            color = new Scalar(0, 255, 0);
        }

        if (gridCell.getCellTypeEnum().equals(CellTypeEnum.FLAG)) {
            color = new Scalar(0, 0, 255);
        }

        Imgproc.circle(mat, position, gridCell.rectangle.height / 5, color, -1);

        return mat;
    }

    public static Mat printDebugInfo(Mat mat, MineSweeperGridCell gridCell) {
        Point position = new Point(gridCell.rectangle.x,
                gridCell.rectangle.y + (double) gridCell.rectangle.height / 100 * 30);
        Point position2 = new Point(gridCell.rectangle.x,
                gridCell.rectangle.y + (double) gridCell.rectangle.height / 100 * 80);
        Scalar color = new Scalar(0, 0, 255);
        int font = Imgproc.FONT_HERSHEY_PLAIN;
        double scale = 1;
        int thickness = 1;

        Imgproc.putText(mat, String.valueOf(gridCell.getNumber()), position, font, scale, color, thickness);
        Imgproc.putText(mat, gridCell.getCellTypeEnum().name().substring(0, 2), position2, font, scale, color,
                thickness);

        return mat;
    }

    public static Mat drawLocations(Mat mat, Grid grid, Scalar color) {

        for (int i = 0; i < grid.getGrid().length; i++) {
            for (int j = 0; j < grid.getGrid()[i].length; j++) {
                mat = drawLocation(mat, grid.getGrid()[i][j], color);
            }
        }
        return mat;
    }

    public static Mat drawLocation(Mat mat, RectArea gridCell, Scalar color) {

        if (gridCell != null) {
            Rect rect = gridCell.rectangle;
            Imgproc.rectangle(mat, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height), color, 2);
        }
        return mat;

    }

    public static Map<BigDecimal, Map<BigDecimal, ListReactArea>> groupByWidthThenByHeight(Mat screenShot,
            List<ContourArea> contours,
            int minimumHorizontalCount, int minimumVerticalCout, BigDecimal tolleranceInPercent) {

        List<RectArea> rectAreaList = contours.stream().map(p -> new RectArea(p.contour, tolleranceInPercent, p.color))
                .collect(Collectors.toList());

        Map<BigDecimal, ListReactArea> mapByW = groupByInCollecting(rectAreaList, p -> p.width, p -> p.widthDecreased);

        Map<BigDecimal, Map<BigDecimal, ListReactArea>> mapByWH = mapByW.entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey(),
                        v -> groupByInCollecting(v.getValue().list, p -> p.height, p -> p.heightDecreased)));

        return removeInnerRectangles(mapByWH);
    }

    private static Map<BigDecimal, ListReactArea> groupByInCollecting(List<RectArea> rectAreaList,
            Function<RectArea, BigDecimal> funcGetDimensionBy,
            Function<RectArea, BigDecimal> funcGetDimensionByDecreased) {
        Map<BigDecimal, ListReactArea> mapByDimension = rectAreaList.stream()
                .collect(Collectors.groupingBy(funcGetDimensionBy))
                .entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey(), v -> {
                    ListReactArea listReactArea = new ListReactArea(v.getValue().get(0));
                    listReactArea.list = v.getValue();
                    return listReactArea;
                }));

        List<BigDecimal> listDimensions = mapByDimension.keySet().stream().sorted((a, b) -> a.compareTo(b) * -1)
                .toList();

        for (int i = 0; i < listDimensions.size() - 1; i++) {
            ListReactArea listAreas = mapByDimension.get(listDimensions.get(i));
            if (listAreas.list.size() > 0) {
                int k = i + 1;
                boolean found = true;
                while (k < listDimensions.size() && found) {
                    ListReactArea listTestAreas = mapByDimension.get(listDimensions.get(k));
                    if (funcGetDimensionByDecreased.apply(listAreas.mainMember)
                            .compareTo(funcGetDimensionBy.apply(listTestAreas.mainMember)) <= 0) {
                        listAreas.list.addAll(listTestAreas.list);
                        listTestAreas.list = new ArrayList<>();
                    }
                    k += 1;
                }
            }
        }

        mapByDimension = mapByDimension.entrySet().stream().filter(p -> p.getValue().list.size() > 0)
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));
        return mapByDimension;
    }

    private static Map<BigDecimal, Map<BigDecimal, ListReactArea>> removeInnerRectangles(
            Map<BigDecimal, Map<BigDecimal, ListReactArea>> mapByWH) {
        for (Map.Entry<BigDecimal, Map<BigDecimal, ListReactArea>> entryW : mapByWH.entrySet()) {
            for (Map.Entry<BigDecimal, ListReactArea> entryH : entryW.getValue().entrySet()) {
                Iterator<RectArea> iter = entryH.getValue().list.iterator();
                while (iter.hasNext()) {
                    RectArea rectArea = iter.next();
                    boolean remove = entryH.getValue().list.stream().filter(p -> {
                        return p.rectangle.x < rectArea.rectangle.x &&
                                p.rectangle.y < rectArea.rectangle.y &&
                                rectArea.rectangle.x < p.rectangle.x + p.rectangle.width &&
                                rectArea.rectangle.y < p.rectangle.y + p.rectangle.height &&
                                p.rectangle.width > rectArea.rectangle.width &&
                                p.rectangle.height > rectArea.rectangle.height &&
                                p.id.compareTo(rectArea.id) != 0;
                    }).findAny().isPresent();
                    if (remove)
                        iter.remove();
                }
            }
        }
        return mapByWH;
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
    public static List<Map<BigDecimal, ListReactArea>> removeSquaresToConformMinWidthAndHeight(
            Map<BigDecimal, ListReactArea> mapByX, Map<BigDecimal, ListReactArea> mapByY, int minGridHorizontalMembers,
            int minGridVerticalMembers,
            BigDecimal tolleranceInPercent) {

        long beforeByXCount = mapByX.entrySet().stream().flatMap(p -> p.getValue().list.stream()).count();

        Set<UUID> mapByYIDs = mapByY.entrySet().stream().flatMap(p -> p.getValue().list.stream()).map(p -> p.id)
                .collect(Collectors.toSet());

        // romeve if absent in mapByY
        mapByX = mapByX.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), v -> {
            List<RectArea> list = v.getValue().list.stream().filter(i -> mapByYIDs.contains(i.id))
                    .collect(Collectors.toList());
            list = removeCellsToConformSequency(list, p -> p.y, p -> p.height, minGridVerticalMembers,
                    tolleranceInPercent);
            v.getValue().list = list;
            return v.getValue();
        }));

        mapByX = mapByX.entrySet().stream().filter(p -> p.getValue().list.size() >= minGridHorizontalMembers)
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

        long afterByXCount = mapByX.entrySet().stream().flatMap(p -> p.getValue().list.stream()).count();

        long beforeByYCount = mapByY.entrySet().stream().flatMap(p -> p.getValue().list.stream()).count();

        // romove if absent in mapByY
        Set<UUID> mapByXIDs = mapByX.entrySet().stream().flatMap(p -> p.getValue().list.stream()).map(p -> p.id)
                .collect(Collectors.toSet());

        mapByY = mapByY.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), v -> {
            List<RectArea> list = v.getValue().list.stream().filter(i -> mapByXIDs.contains(i.id))
                    .collect(Collectors.toList());
            list = removeCellsToConformSequency(list, p -> p.x, p -> p.width, minGridHorizontalMembers,
                    tolleranceInPercent);
            v.getValue().list = list;
            return v.getValue();
        }));

        mapByY = mapByY.entrySet().stream().filter(p -> p.getValue().list.size() >= minGridVerticalMembers)
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

        long afterByYCount = mapByY.entrySet().stream().flatMap(p -> p.getValue().list.stream()).count();

        if (mapByX.size() > 0 && mapByY.size() > 0
                && (beforeByXCount != afterByXCount || beforeByYCount != afterByYCount)) {
            return removeSquaresToConformMinWidthAndHeight(mapByX, mapByY, minGridHorizontalMembers,
                    minGridVerticalMembers, tolleranceInPercent);
        }

        if (mapByX.size() > 0 && mapByY.size() > 0)
            return Arrays.asList(mapByX, mapByY);
        else
            return Arrays.asList(new HashMap<>(), new HashMap<>());
    }

    public static List<RectArea> removeCellsToConformSequency(List<RectArea> list,
            Function<RectArea, BigDecimal> functionPosition, Function<RectArea, BigDecimal> functionWidthOrHeight,
            int minWidthOrHeightCount, BigDecimal tolleranceInPercent) {

        if (list.size() > minWidthOrHeightCount) {

            List<RectArea> listToRemove = new ArrayList<>();

            list = list.stream()
                    .sorted((a, b) -> functionPosition.apply(a)
                            .compareTo(functionPosition.apply(b)))
                    .collect(Collectors.toList());

            int startingPosition = -1;
            int counter = 0;

            for (int i = 1; i < list.size(); i++) {

                if (startingPosition < 0)
                    startingPosition = i;

                BigDecimal prevPos = functionPosition.apply(list.get(i - 1));
                BigDecimal thisPos = functionPosition.apply(list.get(i));
                BigDecimal prevWidthOrHeight = functionWidthOrHeight.apply(list.get(i - 1));

                if (prevWidthOrHeight.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
                        .multiply(tolleranceInPercent)
                        .compareTo(prevPos.add(prevWidthOrHeight).subtract(thisPos).abs()) >= 0) {
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
