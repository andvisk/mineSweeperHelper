package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Board {

    private MineSweeperGridCell[][] grid;
    private GridLocation gridLocation;
    public boolean cellIsEnougthSizeToOcr = false;

    public static final String increaseBoardSizeMsg = "Increase board size to increase accuracy";

    public Board(int width, int height, GridLocation gridLocation) {
        this.gridLocation = gridLocation;
        grid = new MineSweeperGridCell[width][height];
    }

    public void setCell(int x, int y, MineSweeperGridCell gridCell) {
        grid[x][y] = gridCell;
        gridCell.positionInGridX = x;
        gridCell.positionInGridY = y;
    }

    public MineSweeperGridCell[][] getGrid() {
        return grid;
    }

    public GridLocation getGridLocation() {
        return gridLocation;
    }

    public void processGrid(Mat screenShot) {
        // set all flags as unchecked
        for (int i = 0; i < grid.length; i++) {
            for (int y = 0; y < grid[i].length; y++) {
                if (grid[i][y].getCellTypeEnum().equals(CellTypeEnum.FLAG))
                    grid[i][y].setCellTypeEnum(CellTypeEnum.UNCHECKED);
            }
        }
        List<MineSweeperGridCell> list = Stream.of(grid).flatMap(p -> Stream.of(p)).collect(Collectors.toList());

        Set<MineSweeperGridCell> uncheckedAndFlagsSet = new HashSet<MineSweeperGridCell>(list.stream()
                .filter(
                        p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED) ||
                                p.getCellTypeEnum().equals(CellTypeEnum.FLAG))
                .collect(Collectors.toList()));

        solveBoard2(list);

        List<MineSweeperGridCell> cellsForHelp = uncheckedAndFlagsSet.stream()
                .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.NEEDS_TO_BE_CHECKED) ||
                        p.getCellTypeEnum().equals(CellTypeEnum.FLAG))
                .collect(Collectors.toList());

        cellsForHelp.stream().forEach(p -> GridUtils.printHelpInfo(screenShot, p));

    }

    private void solveBoard(List<MineSweeperGridCell> list) {
        boolean anyChanges = false;
        List<MineSweeperGridCell> numbers = list.stream().filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.NUMBER))
                .collect(Collectors.toList());

        // check empty slots
        for (MineSweeperGridCell number : numbers) {
            List<MineSweeperGridCell> neighbours = getNeighbourCells(list, number);
            List<MineSweeperGridCell> neighboursUnchecked = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED)).toList();
            List<MineSweeperGridCell> neighboursFlags = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG)).toList();
            if (neighboursFlags.size() == number.getNumber() && neighboursUnchecked.size() > 0) {
                neighboursUnchecked.stream().forEach(p -> {
                    p.setCellTypeEnum(CellTypeEnum.NEEDS_TO_BE_CHECKED);
                    p.setNumber(-1);
                });
                anyChanges = true;
            }
        }

        // check flags for numbers where unchecked around cells count equals number
        // minus flags
        // count
        for (MineSweeperGridCell number : numbers) {
            List<MineSweeperGridCell> neighbours = getNeighbourCells(list, number);
            List<MineSweeperGridCell> neighboursUnchecked = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED)).toList();
            List<MineSweeperGridCell> neighboursFlags = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG)).toList();
            if (neighboursUnchecked.size() > 0
                    && neighboursUnchecked.size() + neighboursFlags.size() == number.getNumber()) {
                neighboursUnchecked.stream().forEach(p -> p.setCellTypeEnum(CellTypeEnum.FLAG));
                anyChanges = true;
            }
        }

        // two neighbour numbers and their unchecked cells intersection can have only
        // one flag
        for (MineSweeperGridCell number : numbers) {
            Set<MineSweeperGridCell> numberFlags = new HashSet<MineSweeperGridCell>(
                    getNeighbourCells(list, number).stream()
                            .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG)).toList());
            if (number.getNumber() > numberFlags.size()) {
                List<MineSweeperGridCell> neighbours = getNeighbourCells(list, number);
                List<MineSweeperGridCell> neighboursNumbers = neighbours.stream()
                        .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.NUMBER)).toList();

                Set<MineSweeperGridCell> numberUnchecked = new HashSet<MineSweeperGridCell>(
                        getNeighbourCells(list, number).stream()
                                .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED)).toList());

                for (MineSweeperGridCell neighbourNumber : neighboursNumbers) {
                    Set<MineSweeperGridCell> neighboursFlags = new HashSet<MineSweeperGridCell>(
                            getNeighbourCells(list, neighbourNumber).stream()
                                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG)).toList());
                    Set<MineSweeperGridCell> neighboursUnchecked = new HashSet<MineSweeperGridCell>(
                            getNeighbourCells(list, neighbourNumber).stream()
                                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED)).toList());
                    if (neighbourNumber.getNumber() - neighboursFlags.size() == 1) {
                        Set<MineSweeperGridCell> uncheckedInCommon = new HashSet<MineSweeperGridCell>(numberUnchecked);
                        uncheckedInCommon.retainAll(neighboursUnchecked);
                        if (numberUnchecked.size() - uncheckedInCommon.size() == number.getNumber() - numberFlags.size()
                                - 1) {
                            Set<MineSweeperGridCell> numberCellsToFlag = new HashSet<>(numberUnchecked);
                            numberCellsToFlag.removeAll(uncheckedInCommon);
                            if (numberCellsToFlag.size() > 0) {
                                numberCellsToFlag.stream().forEach(p -> p.setCellTypeEnum(CellTypeEnum.FLAG));
                                anyChanges = true;
                            }
                            Set<MineSweeperGridCell> neighboursOnlyUnchecked = new HashSet<MineSweeperGridCell>(
                                    neighboursUnchecked);
                            neighboursOnlyUnchecked.removeAll(uncheckedInCommon);
                            if (neighboursOnlyUnchecked.size() > 0) {
                                neighboursOnlyUnchecked.stream()
                                        .forEach(p -> p.setCellTypeEnum(CellTypeEnum.NEEDS_TO_BE_CHECKED));
                                anyChanges = true;
                            }

                        }
                    }

                }
            }
        }

        if (anyChanges)
            solveBoard(list);
    }

    private void solveBoard2(List<MineSweeperGridCell> list) {

        AtomicBoolean anyChanges = new AtomicBoolean(false);

        Map<Integer, Set<Set<MineSweeperGridCell>>> mapByToFlagCellsCountOnSetsOfUncheckedCells = getMapByToFlagCellsCountOnSetsOfUncheckedCells(
                list);

        while (anyChanges.get()) {

            anyChanges.set(false);

            // to mark count == unchecked count

            mapByToFlagCellsCountOnSetsOfUncheckedCells.entrySet().stream()
                    .forEach(p -> {

                        int toFlagCount = p.getKey();

                        Set<Set<MineSweeperGridCell>> set = p.getValue();

                        List<Set<MineSweeperGridCell>> listToFlag = set.stream().filter(l -> toFlagCount == l.size())
                                .toList();

                        listToFlag.stream().flatMap(o -> o.stream()).forEach(k -> {
                            k.setCellTypeEnum(CellTypeEnum.FLAG);
                        });

                        if (listToFlag.size() > 0) {
                            anyChanges.set(true);
                        }

                    });

            // src/test/resources/src3.png number 6 and number 1 on the right side
        }

    }

    private Map<Integer, Set<Set<MineSweeperGridCell>>> getMapByToFlagCellsCountOnSetsOfUncheckedCells(
            List<MineSweeperGridCell> list) {
        List<MineSweeperGridCell> numbers = list.stream().filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.NUMBER))
                .collect(Collectors.toList());

        Map<Integer, Set<Set<MineSweeperGridCell>>> mapByToFlagCellsCountOnSetsOfUncheckedCells = new HashMap<>();

        for (MineSweeperGridCell number : numbers) {
            List<MineSweeperGridCell> neighbours = getNeighbourCells(list, number);
            List<MineSweeperGridCell> neighboursUnchecked = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED)).toList();
            List<MineSweeperGridCell> neighboursFlags = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG)).toList();

            if (neighboursFlags.size() < number.getNumber() && neighboursUnchecked.size() > 0) {

                Set<Set<MineSweeperGridCell>> setFromMap = mapByToFlagCellsCountOnSetsOfUncheckedCells
                        .get(number.getNumber());

                if (setFromMap == null)
                    setFromMap = new HashSet<>();

                setFromMap.add(neighboursUnchecked.stream().collect(Collectors.toSet()));

                mapByToFlagCellsCountOnSetsOfUncheckedCells.put(number.getNumber() - neighboursFlags.size(),
                        setFromMap);

            }
        }

        return mapByToFlagCellsCountOnSetsOfUncheckedCells;
    }

    public static List<Board> collectBoards(ScreenShotArea screenShotArea,
            Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>> mapGridsByAreaWidthHeight,
            BigDecimal tolleranceInPercent, Consumer<String> updateMessageConsumer) {

        List<Board> listBoards = new ArrayList<>();

        OcrScanner ocrScanner = new OcrScanner(System.getProperty("tesseractDataDir"));

        for (BigDecimal area : mapGridsByAreaWidthHeight.keySet()) {
            for (BigDecimal width : mapGridsByAreaWidthHeight.get(area).keySet()) {
                for (BigDecimal height : mapGridsByAreaWidthHeight.get(area).get(width).keySet()) {
                    for (Grid grid : mapGridsByAreaWidthHeight.get(area).get(width).get(height)) {

                        GridLocation gridLocation = new GridLocation(grid.getGrid(), screenShotArea.area().x,
                                screenShotArea.area().y);

                        // skip if grid area has margins bigger than half cell size
                        if (gridLocation.minX - screenShotArea.area().x <= (double) gridLocation.cellWidth / 2 ||
                                gridLocation.minY - screenShotArea.area().y <= (double) gridLocation.cellHeight / 2) {
                            List<MineSweeperGridCell> boardCells = new ArrayList<>();

                            for (RectArea rectArea : grid.getGridCells()) {

                                CellTypeEnum cellTypeEnum = null;

                                switch (rectArea.color) {
                                    case BLUE:
                                        cellTypeEnum = CellTypeEnum.UNCHECKED;
                                        break;
                                    case YELLOW:
                                        cellTypeEnum = CellTypeEnum.FLAG;
                                        break;
                                    case WHITE:
                                        cellTypeEnum = CellTypeEnum.NUMBER;
                                        break;

                                }

                                MineSweeperGridCell gridCell = new MineSweeperGridCell(
                                        cellTypeEnum, rectArea.rectangle, 0, rectArea.color);

                                boardCells.add(gridCell);
                            }

                            List<RectArea> gridCells = grid.getGridCells();

                            if (boardCells.size() == gridCells.size()) {
                                for (MineSweeperGridCell boardCell : boardCells) {

                                    RectArea gridCell = gridCells.stream().filter(
                                            p -> {
                                                BigDecimal bdX = BigDecimal
                                                        .valueOf(p.rectangle.x)
                                                        .setScale(2,
                                                                RoundingMode.HALF_EVEN);
                                                BigDecimal bdY = BigDecimal
                                                        .valueOf(p.rectangle.y)
                                                        .setScale(2,
                                                                RoundingMode.HALF_EVEN);
                                                BigDecimal bdWidth = BigDecimal.valueOf(
                                                        p.rectangle.width)
                                                        .setScale(2,
                                                                RoundingMode.HALF_EVEN);
                                                BigDecimal bdHeight = BigDecimal
                                                        .valueOf(p.rectangle.height)
                                                        .setScale(2,
                                                                RoundingMode.HALF_EVEN);

                                                BigDecimal bdBX = BigDecimal.valueOf(
                                                        boardCell.rectangle.x)
                                                        .setScale(2,
                                                                RoundingMode.HALF_EVEN);
                                                BigDecimal bdBY = BigDecimal.valueOf(
                                                        boardCell.rectangle.y)
                                                        .setScale(2,
                                                                RoundingMode.HALF_EVEN);

                                                return bdX.subtract(bdBX).abs()
                                                        .compareTo(
                                                                bdWidth.divide(BigDecimal
                                                                        .valueOf(100),
                                                                        2,
                                                                        RoundingMode.HALF_EVEN)
                                                                        .multiply(tolleranceInPercent)) <= 0
                                                        &&
                                                        bdY.subtract(bdBY).abs()
                                                                .compareTo(
                                                                        bdHeight.divide(BigDecimal
                                                                                .valueOf(100),
                                                                                2,
                                                                                RoundingMode.HALF_EVEN)
                                                                                .multiply(tolleranceInPercent)) <= 0;

                                            }).findAny().orElse(null);

                                    if (gridCell != null) {
                                        boardCell.positionInGridX = gridCell.positionInGridX;
                                        boardCell.positionInGridY = gridCell.positionInGridY;
                                    }
                                }
                                int maxXpos = boardCells.stream()
                                        .max((a, b) -> Integer.compare(a.positionInGridX,
                                                b.positionInGridX))
                                        .get().positionInGridX;
                                int maxYpos = boardCells.stream()
                                        .max((a, b) -> Integer.compare(a.positionInGridY,
                                                b.positionInGridY))
                                        .get().positionInGridY;
                                Board board = new Board(maxXpos + 1, maxYpos + 1, gridLocation);

                                for (MineSweeperGridCell boardCell : boardCells) {

                                    if (boardCell.color.equals(ColorsEnum.WHITE)) {

                                        if (boardCell.rectangle.width < 32 && updateMessageConsumer != null) { // updateMessageConsumer
                                                                                                               // null
                                                                                                               // in
                                                                                                               // case
                                                                                                               // running
                                                                                                               // tests
                                            updateMessageConsumer.accept(increaseBoardSizeMsg);
                                        }

                                        Mat imageToOcr = screenShotArea.mat().submat(boardCell.rectangle);

                                        Mat screenShotGamaCorr = ImageUtils
                                                .gammaCorrection(imageToOcr, 1.5);

                                        Mat screenShotBightnessCorr = ImageUtils.contrastAndBrightnessCorrection(
                                                screenShotGamaCorr, 1.0, 30);

                                        Mat whiteColors = ImageUtils.detectColor(screenShotBightnessCorr,
                                                new HsvGray());

                                        double scaleWidthFasctor = (double) 50 / whiteColors.width();

                                        Imgproc.resize(whiteColors, whiteColors, new Size(),
                                                scaleWidthFasctor,
                                                scaleWidthFasctor,
                                                Imgproc.INTER_AREA);

                                        List<MatOfPoint> contours = new ArrayList<>();
                                        Imgproc.findContours(whiteColors, contours, new Mat(), Imgproc.RETR_LIST,
                                                Imgproc.CHAIN_APPROX_SIMPLE);

                                        List<RectArea> listRectArea = contours.stream().map(p -> {
                                            return new RectArea(Imgproc.boundingRect(p), BigDecimal.valueOf(10));
                                        }).collect(Collectors.toCollection(ArrayList::new));

                                        String text = null;

                                        // all cells has borders, removing contour of border, the second one is
                                        // contour for number
                                        listRectArea.sort((a, b) -> a.areaSize.compareTo(b.areaSize));

                                        // some cells has contours from neighbour cells (finding cell accuracy)
                                        // find bigges contour and leave just all inner contours
                                        final RectArea biggesRect = listRectArea.get(listRectArea.size() - 1);

                                        listRectArea = listRectArea.stream().filter(p -> {
                                            Rect rectToTest = p.rectangle;

                                            if (biggesRect.id.compareTo(p.id) != 0) {

                                                if (biggesRect.rectangle.x < rectToTest.x &&
                                                        biggesRect.rectangle.y < rectToTest.y &&
                                                        biggesRect.rectangle.x
                                                                + biggesRect.rectangle.width > rectToTest.x
                                                                        + rectToTest.width
                                                        &&
                                                        biggesRect.rectangle.y
                                                                + biggesRect.rectangle.height > rectToTest.y
                                                                        + rectToTest.height) {
                                                    return true;
                                                }

                                                return false;
                                            } else {
                                                return true; // leave biggest rect
                                            }

                                        }).collect(Collectors.toCollection(ArrayList::new));

                                        // if not empty cell (white), white cells has only one contour of border
                                        if (listRectArea.size() > 1) {

                                            Rect rectForNumber = listRectArea
                                                    .get(listRectArea.size() - 2).rectangle;
                                            Rect rectForNumberIncreased = new Rect(
                                                    new double[] {
                                                            (double) rectForNumber.x
                                                                    - (double) rectForNumber.width * 0.05,
                                                            (double) rectForNumber.y
                                                                    - (double) rectForNumber.height * 0.05,
                                                            (double) rectForNumber.width
                                                                    + (double) rectForNumber.width * 0.1,
                                                            (double) rectForNumber.height
                                                                    + (double) rectForNumber.height * 0.1,
                                                    }

                                            );

                                            if (boardCell.positionInGridX == 17 && boardCell.positionInGridY == 14) {
                                                int ooopa = 0;
                                            }

                                            text = ocrScanner.getNumberFromImage(
                                                    whiteColors.submat(rectForNumberIncreased));

                                            int ooop = 0;
                                        } else {
                                            text = null;
                                        }

                                        // if (text.compareTo("41") == 0) {}

                                        if (text != null && text.length() > 0) {
                                            boardCell.setNumber(Integer.parseInt(text));
                                            boardCell.setCellTypeEnum(CellTypeEnum.NUMBER);
                                        } else {
                                            boardCell.setNumber(0);
                                            boardCell.setCellTypeEnum(CellTypeEnum.EMPTY);
                                        }
                                    }

                                    board.setCell(boardCell.positionInGridX,
                                            boardCell.positionInGridY, boardCell);
                                }
                                listBoards.add(board);
                                printBoard(board);
                            }
                        }
                    }
                }
            }
        }
        ocrScanner.destructor();
        return listBoards;
    }

    private static void printBoard(Board board) {
        MineSweeperGridCell[][] grid = board.getGrid();
        for (int k = 0; k < grid[0].length; k++) {
            String line = "new int[] { ";
            for (int i = 0; i < grid.length; i++) {
                line = line + ((i > 0) ? "," : "") + grid[i][k].getNumber();
            }
            line = line + " },";
            System.out.println(line);
        }
    }

    private List<MineSweeperGridCell> getNeighbourCells(List<MineSweeperGridCell> list, MineSweeperGridCell cell) {
        return list.stream().filter(
                p -> (p.positionInGridX - 1 == cell.positionInGridX && p.positionInGridY - 1 == cell.positionInGridY) ||
                        (p.positionInGridX == cell.positionInGridX && p.positionInGridY - 1 == cell.positionInGridY) ||
                        (p.positionInGridX + 1 == cell.positionInGridX && p.positionInGridY - 1 == cell.positionInGridY)
                        ||
                        (p.positionInGridX + 1 == cell.positionInGridX && p.positionInGridY == cell.positionInGridY) ||
                        (p.positionInGridX + 1 == cell.positionInGridX && p.positionInGridY + 1 == cell.positionInGridY)
                        ||
                        (p.positionInGridX == cell.positionInGridX && p.positionInGridY + 1 == cell.positionInGridY) ||
                        (p.positionInGridX - 1 == cell.positionInGridX && p.positionInGridY + 1 == cell.positionInGridY)
                        ||
                        (p.positionInGridX - 1 == cell.positionInGridX && p.positionInGridY == cell.positionInGridY))
                .collect(Collectors.toList());
    }

    public Mat printDebugInfo(Mat mat) {
        for (int i = 0; i < grid.length; i++) {
            for (int y = 0; y < grid[i].length; y++) {
                mat = GridUtils.printDebugInfo(mat, grid[i][y]);
            }

        }
        return mat;
    }

    public Mat printNumberValuesOnBoardCells(Mat mat) {
        for (int i = 0; i < grid.length; i++) {
            for (int y = 0; y < grid[i].length; y++) {
                mat = GridUtils.printOnlyNumberValues(mat, grid[i][y]);
            }

        }
        return mat;
    }

    public List<MineSweeperGridCell> getGridCells() {
        List<MineSweeperGridCell> list = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            for (int k = 0; k < grid[i].length; k++) {
                if (grid[i][k] != null) {
                    list.add(grid[i][k]);
                }
            }
        }
        return list;
    }
}
