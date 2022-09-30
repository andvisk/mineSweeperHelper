package minesweeperhelper;

public class GridLocation {
    public int minX = -1;
    public int maxX = -1;
    public int minY = -1;
    public int maxY = -1;
    public int cellHeight = -1;
    public int cellWidth = -1;

    public GridLocation(RectArea[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int y = 0; y < grid[i].length; y++) {
                if (grid[i][y] != null) {
                    if (cellHeight < 0) {
                        cellHeight = grid[i][y].rectangle.height;
                        cellWidth = grid[i][y].rectangle.width;
                    }
                    if (minX < 0 || minX > grid[i][y].rectangle.x) {
                        minX = grid[i][y].rectangle.x;
                    }
                    if (minY < 0 || minY > grid[i][y].rectangle.y) {
                        minY = grid[i][y].rectangle.y;
                    }
                    if (maxX < 0 || maxX < grid[i][y].rectangle.x) {
                        maxX = grid[i][y].rectangle.x;
                    }
                    if (maxY < 0 || maxY < grid[i][y].rectangle.y) {
                        maxY = grid[i][y].rectangle.y;
                    }
                }
            }
        }
    }
}
