package minesweeperhelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opencv.core.Mat;

public class Board {

    private MineSweeperGridCell[][] grid;
    private GridLocation gridLocation;

    public Board(int width, int height, GridLocation gridLocation) {
        this.gridLocation = gridLocation;
        grid = new MineSweeperGridCell[width][height];
    }

    public void setCell(int x, int y, MineSweeperGridCell gridCell) {
        grid[x][y] = gridCell;
        gridCell.setPositionInGridX(x);
        gridCell.setPositionInGridY(y);
    }

    public MineSweeperGridCell[][] getGrid() {
        return grid;
    }

    public GridLocation getGridLocation(){
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
            Set<MineSweeperGridCell> numberFlags = new HashSet<MineSweeperGridCell>(getNeighbourCells(list, number).stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG)).toList());
            if (number.getNumber() > numberFlags.size()) {
                List<MineSweeperGridCell> neighbours = getNeighbourCells(list, number);
                List<MineSweeperGridCell> neighboursNumbers = neighbours.stream()
                        .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.NUMBER)).toList();

                Set<MineSweeperGridCell> numberUnchecked = new HashSet<MineSweeperGridCell>(getNeighbourCells(list, number).stream()
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
                            Set<MineSweeperGridCell> neighboursOnlyUnchecked = new HashSet<MineSweeperGridCell>(neighboursUnchecked);
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

    private List<MineSweeperGridCell> getNeighbourCells(List<MineSweeperGridCell> list, MineSweeperGridCell cell) {
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
