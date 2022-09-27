package minesweeperhelper;

import java.util.ArrayList;
import java.util.List;

public class Grid {

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

    public List<GridCell> getGridCells() {
        List<GridCell> list = new ArrayList<>();
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
