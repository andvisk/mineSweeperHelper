package minesweeperhelper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GridUtils {

    public static Grid collectGrid(List<GridCell> cells) {

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
            List<GridCell> list = mapByX.get(x);

        }

        return null;
    }

    private GridCell getNextCellDownDirection(GridCell cell, List<GridCell> list) {
        Optional<GridCell> downCell = list.stream()
                .filter(
                        p -> cell.getId().compareTo(p.getId()) != 0 &&
                                Math.abs(cell.getRect().y + cell.getRect().height
                                        - p.getRect().y) <= (double) cell.getRect().height / 100
                                                * Grid.TOLLERANCE_IN_PERCENT)
                .findFirst();
        if (downCell.isPresent())
            return downCell.get();
        else
            return null;
    }

}
