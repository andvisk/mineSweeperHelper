package minesweeperhelper;

import java.util.List;
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

    public void processGrid() {
        // set all flags as unchecked
        for (int i = 0; i < grid.length; i++) {
            for (int y = 0; y < grid[i].length; y++) {
                if (grid[i][y].getCellTypeEnum().equals(CellTypeEnum.FLAG))
                    grid[i][y].setCellTypeEnum(CellTypeEnum.UNCHECKED);
            }
        }
        List<GridCell> list = Stream.of(grid).flatMap(p -> Stream.of(p)).collect(Collectors.toList());

        markFlagsAndEmptyCells(list);

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
                    p.setCellTypeEnum(CellTypeEnum.EMPTY);
                    p.setNumber(0);
                });
                anyChanges = true;
            }
        }

        // check flags for nombers where neighbour unchecked cells equals number
        for (GridCell number : numbers) {
            List<GridCell> neighbours = getNeighbourCells(list, number);
            List<GridCell> neighboursUnchecked = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.UNCHECKED)).toList();
            List<GridCell> neighboursFlags = neighbours.stream()
                    .filter(p -> p.getCellTypeEnum().equals(CellTypeEnum.FLAG)).toList();
            if (neighboursUnchecked.size() > 0 && neighboursUnchecked.size() + neighboursFlags.size() == number.getNumber()) {
                neighboursUnchecked.stream().forEach(p -> p.setCellTypeEnum(CellTypeEnum.FLAG));
                anyChanges = true;
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
