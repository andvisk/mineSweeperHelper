package minesweeperhelper;

public class Grid {

    public static final int MIN_WIDTH = 9;
    public static final int MIN_HEIGHT = 9;
    public static final int TOLLERANCE_IN_PERCENT = 30; // 15% to one direction

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

}
