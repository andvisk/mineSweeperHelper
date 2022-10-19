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
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.awt.geom.Line2D;

public class GridUtils {

    private static Logger log = LogManager.getLogger(GridUtils.class);

    // map by area, width and height
    public static Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>> collectGrids(
            ScreenShotArea screenShotArea,
            int minGridHorizontalMembers,
            int minGridVerticalMembers, BigDecimal gridPositionAndSizeTolleranceInPercent) {

        String dir = ProcessingService.debugDir + File.separatorChar + screenShotArea.id();

        if (App.debug)
            FileUtils.checkDirExists(dir, true);

        Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>> mapGridsByAreaWidthHeight = new HashMap<>();

        Mat screenShotContrastAndBrightnessCorr = ImageUtils.contrastAndBrightnessCorrection(screenShotArea.mat(), 1.0,
                30);
        Mat screenShotGamaCorr = ImageUtils.gammaCorrection(screenShotContrastAndBrightnessCorr, 1.5);

        Mat blueColors = ImageUtils.detectColor(screenShotGamaCorr, new HsvBlue());

        if (App.debug) {
            Imgcodecs.imwrite(dir + File.separatorChar + "debug_blue_colors.jpg", blueColors);
        }

        Mat yellowColors = ImageUtils.detectColor(screenShotArea.mat(), new HsvYellow()); // including green question
                                                                                          // marks

        if (App.debug) {
            Imgcodecs.imwrite(dir + File.separatorChar + "debug_yellow_colors.jpg",
                    yellowColors);
        }

        Mat grayColors = ImageUtils.detectColor(screenShotArea.mat(), new HsvGray());

        if (App.debug) {
            Imgcodecs.imwrite(dir + File.separatorChar + "debug_gray_and_white_colors.jpg",
                    grayColors);
        }

        List<ContourArea> contoursAll = new ArrayList<>();

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(blueColors, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        contoursAll.addAll(contours.stream().map(p -> new ContourArea(p, ColorsEnum.BLUE)).toList());

        if (App.debug) {
            List<RectArea> rectAreaList = contours.stream()
                    .map(p -> new RectArea(p, BigDecimal.valueOf(10), ColorsEnum.BLUE))
                    .collect(Collectors.toList());
            Mat copyMat = screenShotArea.mat().clone();
            rectAreaList.stream().forEach(p -> GridUtils.drawLocation(copyMat, p, new Scalar(0, 0, 255)));
            Imgcodecs.imwrite(dir + File.separatorChar + "debug_blue_contours.jpg", copyMat);
        }

        contours = new ArrayList<>();
        Imgproc.findContours(yellowColors, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        contoursAll.addAll(contours.stream().map(p -> new ContourArea(p, ColorsEnum.YELLOW)).toList());

        if (App.debug) {
            List<RectArea> rectAreaList = contours.stream()
                    .map(p -> new RectArea(p, BigDecimal.valueOf(10), ColorsEnum.YELLOW))
                    .collect(Collectors.toList());
            Mat copyMat = screenShotArea.mat().clone();
            rectAreaList.stream().forEach(p -> GridUtils.drawLocation(copyMat, p, new Scalar(0, 0, 255)));
            Imgcodecs.imwrite(dir + File.separatorChar + "debug_yellow_contours.jpg", copyMat);
        }

        contours = new ArrayList<>();
        Imgproc.findContours(grayColors, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        contoursAll.addAll(contours.stream().map(p -> new ContourArea(p, ColorsEnum.WHITE)).toList());

        if (App.debug) {
            List<RectArea> rectAreaList = contours.stream()
                    .map(p -> new RectArea(p, BigDecimal.valueOf(10), ColorsEnum.WHITE))
                    .collect(Collectors.toList());
            Mat copyMat = screenShotArea.mat().clone();
            rectAreaList.stream().forEach(p -> GridUtils.drawLocation(copyMat, p, new Scalar(0, 0, 255)));
            Imgcodecs.imwrite(dir + File.separatorChar + "debug_gray_white_contours.jpg",
                    copyMat);
        }

        Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>>> mapByAreaWidthHeight = GridUtils
                .groupByAreaWidthHeight(
                        contoursAll,
                        minGridHorizontalMembers, minGridVerticalMembers, gridPositionAndSizeTolleranceInPercent);

        if (App.debug) {
            printContBoundBoxs(screenShotArea, mapByAreaWidthHeight);
            Imgcodecs.imwrite(dir + File.separatorChar + "debug_screenshot.png",
                    screenShotArea.mat());
        }

        for (Map.Entry<BigDecimal, Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>>> entryArea : mapByAreaWidthHeight
                .entrySet()) {
            BigDecimal area = entryArea.getKey(); // cell area
            for (Map.Entry<BigDecimal, Map<BigDecimal, ListArea<RectArea>>> entryWidth : entryArea.getValue()
                    .entrySet()) {
                BigDecimal width = entryWidth.getKey(); // cell width
                Map<BigDecimal, ListArea<RectArea>> mapByHeight = entryWidth.getValue();
                for (Map.Entry<BigDecimal, ListArea<RectArea>> entryByHeight : mapByHeight.entrySet()) {
                    BigDecimal height = entryByHeight.getKey(); // cell height
                    List<RectArea> points = entryByHeight.getValue().list;

                    Map<BigDecimal, ListArea<RectArea>> mapByX = groupByInCollecting(points, p -> p.x,
                            p -> p.xDecreased);
                    Map<BigDecimal, ListArea<RectArea>> mapByY = groupByInCollecting(points, p -> p.y,
                            p -> p.yDecreased);

                    List<Map<BigDecimal, ListArea<RectArea>>> listOfxyMaps = GridUtils
                            .removeSquaresToConformMinWidthAndHeight(screenShotArea.mat(), mapByX, mapByY,
                                    minGridHorizontalMembers,
                                    minGridVerticalMembers,
                                    gridPositionAndSizeTolleranceInPercent);

                    mapByX = listOfxyMaps.get(0);
                    mapByY = listOfxyMaps.get(1);

                    if (mapByX.size() > 0 && mapByY.size() > 0) {
                        List<Grid> gridList = collectGridsFromCells(mapByX, mapByY, width, height,
                                gridPositionAndSizeTolleranceInPercent);

                        if (gridList.size() > 0) {
                            Map<BigDecimal, Map<BigDecimal, List<Grid>>> returnMapByWidth = mapGridsByAreaWidthHeight
                                    .get(area);

                            if (returnMapByWidth == null) {
                                returnMapByWidth = new HashMap<>();
                                mapGridsByAreaWidthHeight.put(area, returnMapByWidth);

                            }

                            Map<BigDecimal, List<Grid>> returnMapByHeight = mapGridsByAreaWidthHeight.get(area)
                                    .get(width);

                            if (returnMapByHeight == null)
                                returnMapByHeight = new HashMap<>();

                            returnMapByHeight.put(height, gridList);

                            mapGridsByAreaWidthHeight.get(area).put(width, returnMapByHeight);
                        }
                    }
                }
            }
        }

        return mapGridsByAreaWidthHeight;

    }

    private static void printContBoundBoxs(ScreenShotArea screenShotArea,
            Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>>> mapByAreaWidthHeight) {
        final String dirNameFinal = ProcessingService.debugContoursDir + File.separatorChar + screenShotArea.id();
        FileUtils.checkDirExists(dirNameFinal, true);

        for (Map.Entry<BigDecimal, Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>>> entryA : mapByAreaWidthHeight
                .entrySet()) {
            BigDecimal area = entryA.getKey();
            Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>> mapByAW = entryA.getValue();
            for (Map.Entry<BigDecimal, Map<BigDecimal, ListArea<RectArea>>> entryW : mapByAW.entrySet()) {
                BigDecimal width = entryW.getKey();
                entryW.getValue().entrySet().stream().forEach(
                        h -> {
                            Mat img = screenShotArea.mat().clone();
                            h.getValue().list.stream().forEach(p -> {
                                GridUtils.drawLocation(img, p, new Scalar(0, 0, 255));
                            });
                            Imgcodecs.imwrite(
                                    dirNameFinal + "/con_A" + area.toString() + "_" + "_W" + width.toString() + "_H"
                                            + h.getKey().toString() + ".jpg",
                                    img);
                        });
            }
        }
    }

    private static List<Grid> collectGridsFromCells(Map<BigDecimal, ListArea<RectArea>> mapByX,
            Map<BigDecimal, ListArea<RectArea>> mapByY, BigDecimal width, BigDecimal height,
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

                Map<BigDecimal, ListArea<RectArea>> mapByXGrid = groupByInCollecting(
                        xGridSet.stream().collect(Collectors.toList()), p -> p.x, p -> p.xDecreased);
                Map<BigDecimal, ListArea<RectArea>> mapByYGrid = groupByInCollecting(
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
        List<List<Integer>> retList = new ArrayList<>(Arrays.asList(startPos, endPos));

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
                gridCell.rectangle.y + (double) gridCell.rectangle.height / 100 * 40);
        Point position2 = new Point(gridCell.rectangle.x,
                gridCell.rectangle.y + (double) gridCell.rectangle.height / 100 * 90);
        Scalar color = new Scalar(0, 0, 255);
        int font = Imgproc.FONT_HERSHEY_PLAIN;
        double scale = 0.7;
        int thickness = 1;

        Imgproc.putText(mat, String.valueOf(gridCell.getNumber()), position, font, 1, color, thickness);
        Imgproc.putText(mat, gridCell.positionInGridX + ";" + gridCell.positionInGridY, position2, font, scale, color,
                thickness);

        return mat;
    }

    public static Mat printOnlyNumberValues(Mat mat, MineSweeperGridCell gridCell) {
        Point position = new Point(gridCell.rectangle.x,
                gridCell.rectangle.y + (double) gridCell.rectangle.height / 100 * 50);
        Scalar color = new Scalar(0, 0, 255);
        int font = Imgproc.FONT_HERSHEY_PLAIN;
        double scale = 1;
        int thickness = 1;

        if (gridCell.getNumber() > 0)
            Imgproc.putText(mat, String.valueOf(gridCell.getNumber()), position, font, scale, color, thickness);

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

    public static Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>>> groupByAreaWidthHeight(
            List<ContourArea> contours,
            int minimumHorizontalCount, int minimumVerticalCout, BigDecimal tolleranceInPercent) {

        List<RectArea> rectAreaList = contours.stream().map(p -> new RectArea(p.contour, tolleranceInPercent, p.color))
                .collect(Collectors.toList());

        Map<BigDecimal, ListArea<RectArea>> mapByA = groupByInCollecting(rectAreaList, p -> p.areaSize,
                p -> p.decreasedAreaSize);

        Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>>> mapByAWH = mapByA.entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey(),
                        v -> {
                            Map<BigDecimal, ListArea<RectArea>> mapAW = groupByInCollecting(v.getValue().list,
                                    p -> p.width,
                                    p -> p.widthDecreased);
                            Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>> mapAWH = mapAW.entrySet().stream()
                                    .collect(Collectors.toMap(hk -> hk.getKey(),
                                            hv -> groupByInCollecting(hv.getValue().list, hp -> hp.height,
                                                    hp -> hp.heightDecreased)));
                            return mapAWH;
                        }));

        return removeInnerRectangles(mapByAWH);
    }

    public static <T> Map<BigDecimal, ListArea<T>> groupByInCollecting(List<T> rectAreaList,
            Function<T, BigDecimal> funcGetDimensionBy,
            Function<T, BigDecimal> funcGetDimensionByDecreased) {
        Map<BigDecimal, ListArea<T>> mapByDimension = rectAreaList.stream()
                .collect(Collectors.groupingBy(funcGetDimensionBy))
                .entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey(), v -> {
                    ListArea<T> listReactArea = new ListArea(v.getValue().get(0));
                    listReactArea.list = v.getValue();
                    return listReactArea;
                }));

        List<BigDecimal> listDimensions = mapByDimension.keySet().stream().sorted((a, b) -> a.compareTo(b) * -1)
                .toList();

        for (int i = 0; i < listDimensions.size() - 1; i++) {
            ListArea<T> listAreas = mapByDimension.get(listDimensions.get(i));
            if (listAreas.list.size() > 0) {
                int k = i + 1;
                boolean found = true;
                while (k < listDimensions.size() && found) {
                    ListArea<T> listTestAreas = mapByDimension.get(listDimensions.get(k));
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

    private static Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>>> removeInnerRectangles(
            Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>>> mapByAWH) {
        for (Map.Entry<BigDecimal, Map<BigDecimal, Map<BigDecimal, ListArea<RectArea>>>> entryA : mapByAWH.entrySet()) {
            for (Map.Entry<BigDecimal, Map<BigDecimal, ListArea<RectArea>>> entryW : entryA.getValue().entrySet()) {
                for (Map.Entry<BigDecimal, ListArea<RectArea>> entryH : entryW.getValue().entrySet()) {
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
        }
        return mapByAWH;
    }

    /*
     * index
     * 0 - mapByX
     * 1 - mapByY
     */
    public static List<Map<BigDecimal, ListArea<RectArea>>> removeSquaresToConformMinWidthAndHeight(Mat screenShot,
            Map<BigDecimal, ListArea<RectArea>> mapByX, Map<BigDecimal, ListArea<RectArea>> mapByY,
            int minGridHorizontalMembers,
            int minGridVerticalMembers,
            BigDecimal tolleranceInPercent) {

        long beforeByXCount = mapByX.entrySet().stream().flatMap(p -> p.getValue().list.stream()).count();

        Set<UUID> mapByYIDs = mapByY.entrySet().stream().flatMap(p -> p.getValue().list.stream()).map(p -> p.id)
                .collect(Collectors.toSet());

        // romove if absent in mapByY
        mapByX = mapByX.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), v -> {
            List<RectArea> list = v.getValue().list.stream().filter(i -> mapByYIDs.contains(i.id))
                    .collect(Collectors.toList());
            list = removeCellsToConformSequency(screenShot, list, p -> p.y, p -> p.height, minGridVerticalMembers,
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
            list = removeCellsToConformSequency(screenShot, list, p -> p.x, p -> p.width, minGridHorizontalMembers,
                    tolleranceInPercent);
            v.getValue().list = list;
            return v.getValue();
        }));

        mapByY = mapByY.entrySet().stream().filter(p -> p.getValue().list.size() >= minGridVerticalMembers)
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

        long afterByYCount = mapByY.entrySet().stream().flatMap(p -> p.getValue().list.stream()).count();

        if (mapByX.size() > 0 && mapByY.size() > 0
                && (beforeByXCount != afterByXCount || beforeByYCount != afterByYCount)) {
            return removeSquaresToConformMinWidthAndHeight(screenShot, mapByX, mapByY, minGridHorizontalMembers,
                    minGridVerticalMembers, tolleranceInPercent);
        }

        if (mapByX.size() > 0 && mapByY.size() > 0)
            return new ArrayList<>(Arrays.asList(mapByX, mapByY));
        else
            return new ArrayList<>(Arrays.asList(new HashMap<>(), new HashMap<>()));
    }

    public static List<RectArea> removeCellsToConformSequency(Mat screenShot, List<RectArea> list,
            Function<RectArea, BigDecimal> functionPosition, Function<RectArea, BigDecimal> functionWidthOrHeight,
            int minWidthOrHeightCount, BigDecimal tolleranceInPercent) {

        if (list.size() >= minWidthOrHeightCount) {

            List<RectArea> listToRemove = new ArrayList<>();

            list = list.stream()
                    .sorted((a, b) -> functionPosition.apply(a)
                            .compareTo(functionPosition.apply(b)))
                    .collect(Collectors.toList());

            int startingPosition = 0;
            int counter = 1;

            for (int i = 1; i < list.size(); i++) {

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

                if (i == list.size() - 1 && counter < minWidthOrHeightCount) {
                    for (int j = startingPosition; j < list.size(); j++) {
                        listToRemove.add(list.get(j));
                    }
                }
            }
            if (list.size() - listToRemove.size() >= minWidthOrHeightCount) {
                if (App.debug && listToRemove.size() > 0) {

                    String uuid = UUID.randomUUID().toString();

                    final Mat debugMat = screenShot.clone();
                    listToRemove.stream().forEach(p -> GridUtils.drawLocation(debugMat, p, new Scalar(0, 0, 255)));
                    Imgcodecs
                            .imwrite(ProcessingService.debugRemoveConformSeqDir + File.separatorChar
                                    + uuid + "_LIST_TO_REMOVE.jpg", debugMat);

                    final Mat debugMatLst = screenShot.clone();
                    list.stream().forEach(p -> GridUtils.drawLocation(debugMatLst, p, new Scalar(0, 0, 255)));
                    Imgcodecs
                            .imwrite(ProcessingService.debugRemoveConformSeqDir + File.separatorChar
                                    + uuid + "_SOURCE_LIST.jpg", debugMatLst);
                }
                list.removeAll(listToRemove);
                return list;
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    public static Point getLinesItersection(Point line1_1, Point line1_2, Point line2_1,
            Point line2_2) {
        double x1 = line1_1.x;
        double x2 = line1_2.x;
        double x3 = line2_1.x;
        double x4 = line2_2.x;
        double y1 = line1_1.y;
        double y2 = line1_2.y;
        double y3 = line2_1.y;
        double y4 = line2_2.y;

        Line2D line1 = new Line2D.Float(1, 1, 1, 1);
        line1.setLine(x1, y1, x2, y2);

        Line2D line2 = new Line2D.Float(1, 1, 1, 1);
        line2.setLine(x3, y3, x4, y4);

        boolean intersectsSegments = line2.intersectsLine(line1);

        if (intersectsSegments) {

            double denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

            if (BigDecimal.valueOf(Math.abs(denominator)).setScale(2, RoundingMode.HALF_EVEN)
                    .equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)))
                return null;

            double x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4))
                    / denominator;
            double y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4))
                    / denominator;

            return new Point(x, y);
        }

        return null;
    }

    public static Map<UUID, Set<Intersection>> getMapGroupedByIntersections(Mat screenShot) {
        Mat gray = new Mat();
        Imgproc.cvtColor(screenShot, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(gray, gray, 50, 200, 3, false);

        Mat cdst = new Mat();
        Imgproc.cvtColor(gray, cdst, Imgproc.COLOR_GRAY2BGR);

        Mat screenShotBW_BGR = cdst.clone();

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
                        new HashSet<>(new ArrayList<>(Arrays.asList(intersection))));
            }

        }

        if (App.debug) {

            mapGroupedIntersections.entrySet().stream().forEach(p -> {
                Scalar color = new Scalar(0, 0, 255);
                Mat groupedIntersectionsMat = screenShotBW_BGR.clone();
                p.getValue().forEach(i -> {
                    Imgproc.line(groupedIntersectionsMat, i.lineArea1.point1, i.lineArea1.point2,
                            color, 1,
                            Imgproc.LINE_AA, 0);
                    Imgproc.line(groupedIntersectionsMat, i.lineArea2.point1, i.lineArea2.point2,
                            color, 1,
                            Imgproc.LINE_AA, 0);
                });
                Imgcodecs.imwrite(
                        ProcessingService.debugDir + File.separatorChar + "debug_lines_intersections_grouped_"
                                + p.getKey().toString() + ".png",
                        groupedIntersectionsMat);
            });
        }

        if (App.debug) {
            Imgcodecs.imwrite(ProcessingService.debugDir + File.separatorChar + "debug_lines.png", cdst);
            Imgcodecs.imwrite(ProcessingService.debugDir + File.separatorChar + "debug_lines_intersections.png",
                    intersectionsMat);
        }

        return mapGroupedIntersections;
    }

    public static Rect getAreaByIntersections(Set<Intersection> mapByIntersections) {

        double minX = mapByIntersections.stream().mapToDouble(p -> {
            return Arrays.asList(p.lineArea1.point1.x, p.lineArea1.point2.x, p.lineArea2.point1.x, p.lineArea2.point2.x)
                    .stream().min((a, b) -> Double.compare(a, b)).get();
        }).min().getAsDouble();

        double minY = mapByIntersections.stream().mapToDouble(p -> {
            return Arrays.asList(p.lineArea1.point1.y, p.lineArea1.point2.y, p.lineArea2.point1.y, p.lineArea2.point2.y)
                    .stream().min((a, b) -> Double.compare(a, b)).get();
        }).min().getAsDouble();

        double maxX = mapByIntersections.stream().mapToDouble(p -> {
            return Arrays.asList(p.lineArea1.point1.x, p.lineArea1.point2.x, p.lineArea2.point1.x, p.lineArea2.point2.x)
                    .stream().max((a, b) -> Double.compare(a, b)).get();
        }).max().getAsDouble();

        double maxY = mapByIntersections.stream().mapToDouble(p -> {
            return Arrays.asList(p.lineArea1.point1.y, p.lineArea1.point2.y, p.lineArea2.point1.y, p.lineArea2.point2.y)
                    .stream().max((a, b) -> Double.compare(a, b)).get();
        }).max().getAsDouble();

        int width = (int) (maxX - minX);
        int height = (int) (maxY - minY);

        return new Rect((int) minX, (int) minY, (int) width,
                (int) height);
    }

    public static int howManyContours(Mat mat, HsvColor hsvColor) {
        Mat colorsMat = ImageUtils.detectColor(mat, hsvColor);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(colorsMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        return contours.stream().flatMap(p -> p.toList().stream()).toList().size();
    }

}
