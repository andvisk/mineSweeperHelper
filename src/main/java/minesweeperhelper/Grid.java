package minesweeperhelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opencv.core.Mat;

public class Grid {

    public static final int MIN_WIDTH = 9;
    public static final int MIN_HEIGHT = 9;
    public static final int TOLLERANCE_IN_PERCENT = 50; // 25% to one direction

    private GridCell[][] grid;

    public Grid(int width, int height) {
        grid = new GridCell[width][height];
    }

    public void setCell(int x, int y, GridCell gridCell) {
        grid[x][y] = gridCell;
        gridCell.setPositionInGridX(x);
        gridCell.setPositionInGridY(y);
    }

    public GridCell[][] getGrid() {
        return grid;
    }

    public void processGrid(Mat screenShot) {
        // set all flags as unchecked
        for (int i = 0; i < grid.length; i++) {
            for (int y = 0; y < grid[i].length; y++) {
                if (grid[i][y].getCellTypeEnum().equals(CellTypeEnum.FLAG))
                    grid[i][y].setCellTypeEnum(CellTypeEnum.UNCHECKED);
            }
        }
        List<GridCell> list = Stream.of(grid).flatMap(p -> Stream.of(p)).collect(Collectors.toList());

        Set<GridCell> uncheckedAndFlagsSet = new HashSet<GridCell>(list.stream()
                .filter(
                        p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED) ||
                                p.getCellTypeEnum().equals(CellTypeEnum.FLAG))
                .collect(Collectors.toList()));

        Set<GridCell> usersSetFlagsSet = new HashSet<GridCell>(list.stream()
                .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG))
                .collect(Collectors.toList()));

        markFlagsAndEmptyCells(list);

        List<GridCell> cellsForHelp = uncheckedAndFlagsSet.stream()
                .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.NEEDS_TO_BE_CHECKED) ||
                        p.getCellTypeEnum().equals(CellTypeEnum.FLAG))
                .collect(Collectors.toList());

        // todo picture on these unsafe flags
        Set<GridCell> usersSetFalseFlagsSet = new HashSet<GridCell>(usersSetFlagsSet.stream()
                .filter(p -> !p.getCellTypeEnum().equals(CellTypeEnum.FLAG))
                .collect(Collectors.toList()));

        cellsForHelp.stream().forEach(p -> GridUtils.printHelpInfo(screenShot, p));

    }

    private void markFlagsAndEmptyCells(List<GridCell> list) {
        boolean anyChanges = false;
        List<GridCell> numbers = list.stream().filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.NUMBER))
                .collect(Collectors.toList());

        // check empty slots
        for (GridCell number : numbers) {
            List<GridCell> neighbours = getNeighbourCells(list, number);
            List<GridCell> neighboursUnchecked = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED)).toList();
            List<GridCell> neighboursFlags = neighbours.stream()
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
        for (GridCell number : numbers) {
            List<GridCell> neighbours = getNeighbourCells(list, number);
            List<GridCell> neighboursUnchecked = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED)).toList();
            List<GridCell> neighboursFlags = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG)).toList();
            if (neighboursUnchecked.size() > 0
                    && neighboursUnchecked.size() + neighboursFlags.size() == number.getNumber()) {
                neighboursUnchecked.stream().forEach(p -> p.setCellTypeEnum(CellTypeEnum.FLAG));
                anyChanges = true;
            }
        }

        // two neighbour numbers and their unchecked cells intersection can have only
        // one flag
        for (GridCell number : numbers) {
            Set<GridCell> numberFlags = new HashSet<GridCell>(getNeighbourCells(list, number).stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG)).toList());
            if (number.getNumber() > numberFlags.size()) {
                List<GridCell> neighbours = getNeighbourCells(list, number);
                List<GridCell> neighboursNumbers = neighbours.stream()
                        .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.NUMBER)).toList();

                Set<GridCell> numberUnchecked = new HashSet<GridCell>(getNeighbourCells(list, number).stream()
                        .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED)).toList());

                for (GridCell neighbourNumber : neighboursNumbers) {
                    Set<GridCell> neighboursFlags = new HashSet<GridCell>(
                            getNeighbourCells(list, neighbourNumber).stream()
                                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG)).toList());
                    Set<GridCell> neighboursUnchecked = new HashSet<GridCell>(
                            getNeighbourCells(list, neighbourNumber).stream()
                                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED)).toList());
                    if (neighbourNumber.getNumber() - neighboursFlags.size() == 1) {
                        Set<GridCell> uncheckedInCommon = new HashSet<GridCell>(numberUnchecked);
                        uncheckedInCommon.retainAll(neighboursUnchecked);
                        if (numberUnchecked.size() - uncheckedInCommon.size() == number.getNumber() - numberFlags.size()
                                - 1) {
                            Set<GridCell> numberCellsToFlag = new HashSet<>(numberUnchecked);
                            numberCellsToFlag.removeAll(uncheckedInCommon);
                            if (numberCellsToFlag.size() > 0) {
                                numberCellsToFlag.stream().forEach(p -> p.setCellTypeEnum(CellTypeEnum.FLAG));
                                anyChanges = true;
                            }
                            Set<GridCell> neighboursOnlyUnchecked = new HashSet<GridCell>(neighboursUnchecked);
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

    public Mat printDebugInfo(Mat mat) {
        for (int i = 0; i < grid.length; i++) {
            for (int y = 0; y < grid[i].length; y++) {
                mat = GridUtils.printDebugInfo(mat, grid[i][y]);
            }

        }
        return mat;
    }
}
