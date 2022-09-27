package minesweeperhelper;

public class GridLocation {
    public int minX = -1;
    public int maxX = -1;
    public int minY = -1;
    public int maxY = -1;
    public int cellHeight = -1;
    public int cellWidth = -1;

    public GridLocation(GridCell[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int y = 0; y < grid[i].length; y++) {
                if (grid[i][y] != null) {
                    if (cellHeight < 0) {
                        cellHeight = grid[i][y].getRect().height;
                        cellWidth = grid[i][y].getRect().width;
                    }
                    if (minX < 0 || minX > grid[i][y].getRect().x) {
                        minX = grid[i][y].getRect().x;
                    }
                    if (minY < 0 || minY > grid[i][y].getRect().y) {
                        minY = grid[i][y].getRect().y;
                    }
                    if (maxX < 0 || maxX < grid[i][y].getRect().x) {
                        maxX = grid[i][y].getRect().x;
                    }
                    if (maxY < 0 || maxY < grid[i][y].getRect().y) {
                        maxY = grid[i][y].getRect().y;
                    }
                }
            }
        }
    }
}
