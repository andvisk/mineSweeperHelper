package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class Board {

    private MineSweeperGridCell[][] grid;
    private GridLocation gridLocation;
    public boolean cellIsEnougthSizeToOcr = true;

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

        Set<MineSweeperGridCell> usersSetFlagsSet = new HashSet<MineSweeperGridCell>(list.stream()
                .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG))
                .collect(Collectors.toList()));

        markFlagsAndEmptyCells(list);

        List<MineSweeperGridCell> cellsForHelp = uncheckedAndFlagsSet.stream()
                .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.NEEDS_TO_BE_CHECKED) ||
                        p.getCellTypeEnum().equals(CellTypeEnum.FLAG))
                .collect(Collectors.toList());

        // todo picture on these unsafe flags
        Set<MineSweeperGridCell> usersSetFalseFlagsSet = new HashSet<MineSweeperGridCell>(usersSetFlagsSet.stream()
                .filter(p -> !p.getCellTypeEnum().equals(CellTypeEnum.FLAG))
                .collect(Collectors.toList()));

        cellsForHelp.stream().forEach(p -> GridUtils.printHelpInfo(screenShot, p));

    }

    private void markFlagsAndEmptyCells(List<MineSweeperGridCell> list) {
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

        // check flags for numbers where neighbour unchecked cells count equals number
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
            markFlagsAndEmptyCells(list);
    }

    public static List<Board> collectBoards(ScreenShotArea screenShotArea,
            Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>> mapGridsByAreaWidthHeight,
            BigDecimal tolleranceInPercent) {

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

                                        // remove possibly black border
                                        double oneSideMarginMultiplier = 0.1;
                                        Rect ocrArea = new Rect(
                                                (int) (boardCell.rectangle.x
                                                        + (double) boardCell.rectangle.width * oneSideMarginMultiplier),
                                                (int) (boardCell.rectangle.y + (double) boardCell.rectangle.height
                                                        * oneSideMarginMultiplier),
                                                (int) ((double) boardCell.rectangle.width
                                                        * (1 - oneSideMarginMultiplier * 2)),
                                                (int) ((double) boardCell.rectangle.height
                                                        * (1 - oneSideMarginMultiplier * 2)));

                                        Mat imageToOcr = screenShotArea.mat().submat(ocrArea);

                                        String text = ocrScanner.getNumberFromImage(imageToOcr);

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
                            }
                        }
                    }
                }
            }
        }
        ocrScanner.destructor();
        return listBoards;
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
