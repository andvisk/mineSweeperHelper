package minesweeperhelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class GridUtils {

    private static Logger log = LogManager.getLogger(GridUtils.class);
    
    public static Grid collectGrid(List<GridCell> cells) {

        if (cells.size() > 0) {

            Map<Integer, List<GridCell>> mapByX = GroupingBy.approximate(cells, p -> (int) p.getRect().x,
                    p -> (int) p.getRect().width, Grid.TOLLERANCE_IN_PERCENT);

            // remove dublicates if any
            for (Integer x : mapByX.keySet()) {
                List<GridCell> list = mapByX.get(x);
                for (int i = 0; i < list.size() - 1; i++) {
                    GridCell gridCell = list.get(i);
                    Iterator<GridCell> iterator = list.iterator();
                    GridCell gridCellIter = null;
                    for (int j = 0; j < i; j++) {
                        if (iterator.hasNext())
                            iterator.next();
                    }
                    while (iterator.hasNext()) {
                        gridCellIter = iterator.next();

                        if (gridCellIter != null && gridCell.getId().compareTo(gridCellIter.getId()) != 0 &&
                                Math.abs(gridCell.getRect().y
                                        - gridCellIter.getRect().y) <= (double) gridCellIter.getRect().height / 100
                                                * Grid.TOLLERANCE_IN_PERCENT) {
                            iterator.remove();
                        }
                    }
                }
            }

            //check all columns have the same rows count
            int rows = -1;
            for (Integer x : mapByX.keySet()) {
                mapByX.get(x).sort((a, b) -> Integer.compare(a.getRect().y, b.getRect().y));
                if (rows < 0) {
                    rows = mapByX.get(x).size();
                } else {
                    if (mapByX.get(x).size() != rows) {
                        log.info("unable to get grid, r1");
                    }
                }
            }

            List<Integer> xs = new ArrayList<>(mapByX.keySet());
            xs.sort((a, b) -> Integer.compare(a, b));

            Grid grid = new Grid(xs.size(), mapByX.get(xs.get(0)).size());

            //filling grid
            int column = -1;
            for (Integer x : xs) {
                ++column;
                List<GridCell> columnData = mapByX.get(x);
                List<Integer> ys = columnData.stream().map(p -> p.getRect().y).collect(Collectors.toList());
                ys.sort((a, b) -> Integer.compare(a, b));
                int row = -1;
                for (Integer y : ys) {
                    ++row;
                    grid.setCell(column, row, columnData.stream().filter(p -> p.getRect().y == y).findFirst().get());
                }
            }

            return grid;
        }

        log.info("unable to get grid, r4");
        return null;
    }

    public static Mat printDebugInfo(Mat mat, GridCell gridCell) {
        Point position = new Point(gridCell.getRect().x,
                gridCell.getRect().y + (double) gridCell.getRect().height / 100 * 30);
        Point position2 = new Point(gridCell.getRect().x,
                gridCell.getRect().y + (double) gridCell.getRect().height / 100 * 80);
        Scalar color = new Scalar(0, 0, 255);
        int font = Imgproc.FONT_HERSHEY_PLAIN;
        double scale = 1;
        int thickness = 1;

        Imgproc.putText(mat, String.valueOf(gridCell.getNumber()), position, font, scale, color, thickness);
        Imgproc.putText(mat, gridCell.getCellTypeEnum().name().substring(0, 2), position2, font, scale, color,
                thickness);

        return mat;
    }

}
