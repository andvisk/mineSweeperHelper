package minesweeperhelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GridUtilsTest {

    private int width = 20;
    private int height = 20;
    private int gap = 2;
    private int tolleranceInPercent = 25;

    @Test
    void removeCellsToConformSequency() {
        
        List listTest = getThreeSequancesWithinList(Grid.TOLLERANCE_IN_PERCENT);
        List<GridCell> listByX = (List<GridCell>) listTest.get(0);
        List<UUID> listIdsToRemove = (List<UUID>) listTest.get(1);

        List<GridCell> list = GridUtils.removeCellsToConformSequency(listByX, p->p.getX(), p->p.getRect().width, Grid.TOLLERANCE_IN_PERCENT);
    }

    @Test
    void removeSquaresToConformMinWidthAndHeight() {
        List listX = getMappedByX();
        Map<Integer, List<GridCell>> mapByX = (Map<Integer, List<GridCell>>) listX.get(0);
        List<UUID> listIdsToRemove = (List<UUID>) listX.get(1);

        List listY = getMappedByY(mapByX, listIdsToRemove);
        mapByX = (Map<Integer, List<GridCell>>) listY.get(0);
        Map<Integer, List<GridCell>> mapByY = (Map<Integer, List<GridCell>>) listY.get(1);
        listIdsToRemove = (List<UUID>) listY.get(2);

        // seting min width and height higher than input data size
        List<Map<Integer, List<GridCell>>> value = GridUtils.removeSquaresToConformMinWidthAndHeight(mapByX,
                mapByY, 11, 11);

        assertEquals(value.get(0).size(), 0);
        assertEquals(value.get(1).size(), 0);

        // expecting grid size 10x10
        value = GridUtils.removeSquaresToConformMinWidthAndHeight(mapByX,
                mapByY, 5, 5);

        assertEquals(value.get(0).size(), 10);
        assertEquals(value.get(1).size(), 10);

        Set<UUID> allCellsByX = value.get(0).entrySet().stream().flatMap(p -> p.getValue().stream()).map(p -> p.getId())
                .collect(Collectors.toSet());
        Set<UUID> allCellsByY = value.get(1).entrySet().stream().flatMap(p -> p.getValue().stream()).map(p -> p.getId())
                .collect(Collectors.toSet());
        Set<UUID> allCellsToBeRemoved = listIdsToRemove.stream().collect(Collectors.toSet());

        Set<UUID> intersectionX = new HashSet<>(allCellsByX);
        intersectionX.retainAll(allCellsToBeRemoved);
        assertEquals(intersectionX.size(), 0);

        Set<UUID> intersectionY = new HashSet<>(allCellsByY);
        intersectionY.retainAll(allCellsToBeRemoved);
        assertEquals(intersectionY.size(), 0);

    }

    /*
     * index 0 - List<GridCell> list with 50 non removing members
     * index 1 - List<UUID> 10 GridCells ids to be removed
     */
    private List<GridCell> getThreeSequancesWithinList(int tolleranceInPercent) {
        List<GridCell> listCreate = new ArrayList<>();
        List<GridCell> listCellsToRemove = new ArrayList<>();

        List listRet = Arrays.asList(listCreate, listCellsToRemove);

        int x = 20;
        int y = 20;
        for (int i = 1; i <= 50; i++) {
            Rect rect = new Rect(new Point(x, y), new Size(width, height));
            GridCell gridCell = new GridCell(rect);
            listCreate.add(gridCell);
            if (i == 13 || i == 24 || i == 36) {
                for (int j = 1; j <= 7; j++) {
                    x += width + ((double) width / 100 * tolleranceInPercent * 2) + gap;
                    rect = new Rect(new Point(x, y), new Size(width, height));
                    gridCell = new GridCell(rect);
                    listCreate.add(gridCell);
                    listCellsToRemove.add(gridCell);
                }
            }
            x += width + gap;
        }

        return listRet;
    }

    /*
     * index 0 - Map<Integer, List<GridCell>> list with 10 non removing members
     * index 1 - List<UUID> GridCells ids to be removed
     */
    private List getMappedByX() {
        Map<Integer, List<GridCell>> map = new HashMap<>();
        List<UUID> listId = new ArrayList();

        List listRet = Arrays.asList(map, listId);

        int x = 20;
        for (int i = 1; i <= 10; i++) {
            List<GridCell> list = new ArrayList<>();
            int y = 20;
            for (int j = 1; j <= 10; j++) {
                Rect rect = new Rect(new Point(x, y), new Size(width, height));
                GridCell gridCell = new GridCell(rect);
                list.add(gridCell);
                y += height + gap;
            }
            if (i == 3 || i == 6 || i == 8) {
                Rect rect = new Rect(new Point(x, y + height + gap), new Size(width, height));
                GridCell gridCell = new GridCell(rect);
                list.add(gridCell);
                listId.add(gridCell.getId());
            }
            map.put(x, list);
            x += width + gap;
        }

        return listRet;
    }

    /*
     * index 0 - Map<Integer, List<GridCell>> xs list with 10 non removing members
     * index 1 - Map<Integer, List<GridCell>> ys list with 10 non removing members
     * index 2 - List<UUID> GridCells ids to be removed
     */
    private List getMappedByY(Map<Integer, List<GridCell>> mapByX, List<UUID> listUUIDtoRemove) {

        Map<Integer, List<GridCell>> map = GroupingBy.approximateInArea(
                mapByX.entrySet().stream().flatMap(p -> p.getValue().stream())
                        .collect(Collectors.toList()),
                p -> p.getRect().y,
                p -> p.getRect().height, tolleranceInPercent);

        List<Integer> ys = map.keySet().stream().collect(Collectors.toList());
        ys = ys.stream().sorted().collect(Collectors.toList());
        for (int i = 1; i <= 7; i++) {
            if (i == 2 || i == 5 || i == 7) {

                List<GridCell> listByY = map.get(ys.get(i - 1));
                Integer maxX = listByY.stream().map(p -> p.getRect().x).max((p, l) -> Integer.compare(p, l)).get();
                Integer y = ys.get(i - 1);
                Integer x = maxX + width + gap;
                Rect rect = new Rect(new Point(x, y), new Size(width, height));
                GridCell gridCell = new GridCell(rect);
                listByY.add(gridCell);
                listUUIDtoRemove.add(gridCell.getId());

                mapByX.put(x, Arrays.asList(gridCell));
            }
        }

        return Arrays.asList(mapByX, map, listUUIDtoRemove);
    }
}