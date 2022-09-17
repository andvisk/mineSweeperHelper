package minesweeperhelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GridUtils {

    public static Grid collectGrid(List<GridCell> cells) {

        if (cells.size() > 0) {
            int cellWidth = cells.get(0).getRect().width;
            int cellHeight = cells.get(0).getRect().height;

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

            for (Integer x : mapByX.keySet()) {
                mapByX.get(x).sort((a, b) -> Integer.compare(a.getRect().y, b.getRect().y));
            }

            List<Integer> xs = new ArrayList<>(mapByX.keySet());
            xs.sort((a, b) -> Integer.compare(a, b));

            // check x's and y's for sequency
            Integer prevX = null;
            for (Integer x : xs) {
                if (prevX != null
                        && Math.abs(x - (prevX + cellWidth)) > (double) cellWidth / 100 * Grid.TOLLERANCE_IN_PERCENT) {
                    return null;
                }
                prevX = x;

                //checking y's sequency
                Integer prevY = null;
                for(Iteger y:map.get(x).){

                }
            }

            return new Grid(xs.size(), mapByX.get(xs.get(0)).size());
        }

        return null;
    }

}
