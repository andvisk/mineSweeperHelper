package minesweeperhelper;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.List;
import java.util.Map;

public class Grid {

    public static final int MIN_WIDTH = 9;
    public static final int MIN_HEIGHT = 9;
    public static final int TOLLERANCE_IN_PERCENT = 15;;

    private GridCell[][] grid;

    public Grid(Mat srcImage, List<List<Rect>> allLines, Map<Integer, List<Rect>> numbersLocations) {
        grid = new GridCell[allLines.size()][allLines.get(0).size()];

        int y = -1;

        for (List<Rect> line : allLines) {
            ++y;
            int x = -1;
            for (Rect cell : line) {
                ++x;
                grid[y][x] = new GridCell(srcImage, cell, numbersLocations);
            }
        }
    }

    public void setCell(int x, int y, GridCell gridCell) {
        grid[x][y] = gridCell;
    }

    public GridCell[][] getGrid() {
        return grid;
    }
}
