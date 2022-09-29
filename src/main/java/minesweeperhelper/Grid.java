package minesweeperhelper;

import java.util.ArrayList;
import java.util.List;

public class Grid {

    private RectArea[][] grid;

    public Grid(int width, int height) {
        grid = new RectArea[width][height];
    }

    public void setCell(int x, int y, RectArea gridCell) {
        grid[x][y] = gridCell;
        gridCell.positionInGridX = x;
        gridCell.positionInGridY = y;
    }

    public RectArea[][] getGrid() {
        return grid;
    }

    public List<RectArea> getGridCells() {
        List<RectArea> list = new ArrayList<>();
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
