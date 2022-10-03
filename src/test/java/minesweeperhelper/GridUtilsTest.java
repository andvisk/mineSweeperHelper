package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private int minGridMembersHor = 9;
    private int minGridMembersVer = 9;
    private BigDecimal tolleranceInPercent = BigDecimal.valueOf(65).setScale(2, RoundingMode.HALF_EVEN);
    private int tolleranceInPercentInt = tolleranceInPercent.intValue();

    @Test
    void getIntervals() {
        BigDecimal width = BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_EVEN);
        List<BigDecimal> list = createFourIntervals(width, tolleranceInPercent);
        List<List<Integer>> intervals = GridUtils.getIntervals(list, width, tolleranceInPercent);
        assertEquals(4, intervals.get(0).size());
        assertEquals(4, intervals.get(1).size());

        list = createOneIntervals(width, tolleranceInPercent);
        intervals = GridUtils.getIntervals(list, width, tolleranceInPercent);
        assertEquals(1, intervals.get(0).size());
        assertEquals(1, intervals.get(1).size());

        list = Arrays.asList(BigDecimal.valueOf(5));
        intervals = GridUtils.getIntervals(list, width, tolleranceInPercent);
        assertEquals(1, intervals.get(0).size());
        assertEquals(1, intervals.get(1).size());

        list = Arrays.asList(BigDecimal.valueOf(5), BigDecimal.valueOf(5).add(width));
        intervals = GridUtils.getIntervals(list, width, tolleranceInPercent);
        assertEquals(1, intervals.get(0).size());
        assertEquals(1, intervals.get(1).size());

    }

    @Test
    void removeCellsToConformSequency() {

        List listTest = getThreeSequancesWithinList(tolleranceInPercent);
        List<RectArea> listByX = (List<RectArea>) listTest.get(0);
        List<UUID> listIdsToRemove = (List<UUID>) listTest.get(1);

        List<RectArea> list = GridUtils.removeCellsToConformSequency(listByX, p -> p.y,
                p -> p.height, minGridMembersHor, tolleranceInPercent);

        Set<UUID> allCellsByX = list.stream().map(p -> p.id)
                .collect(Collectors.toSet());

        Set<UUID> allCellsToBeRemoved = listIdsToRemove.stream().collect(Collectors.toSet());

        Set<UUID> intersectionX = new HashSet<>(allCellsByX);
        intersectionX.retainAll(allCellsToBeRemoved);
        assertEquals(0, intersectionX.size());
    }

    @Test
    void removeSquaresToConformMinWidthAndHeight_emptyRes() {
        List listX = getMappedByX();
        Map<BigDecimal, ListReactArea> mapByX = (Map<BigDecimal, ListReactArea>) listX.get(0);
        List<UUID> listIdsToRemove = (List<UUID>) listX.get(1);

        List listY = getMappedByY(mapByX, listIdsToRemove);
        mapByX = (Map<BigDecimal, ListReactArea>) listY.get(0);
        Map<BigDecimal, ListReactArea> mapByY = (Map<BigDecimal, ListReactArea>) listY.get(1);
        listIdsToRemove = (List<UUID>) listY.get(2);

        // seting min width and height higher than input data size
        List<Map<BigDecimal, ListReactArea>> value = GridUtils.removeSquaresToConformMinWidthAndHeight(mapByX,
                mapByY, 11, 11, tolleranceInPercent);

        assertEquals(0, value.get(0).size());
        assertEquals(0, value.get(1).size());

    }

    @Test
    void removeSquaresToConformMinWidthAndHeight_fullRes() {
        List listX = getMappedByX();
        Map<BigDecimal, ListReactArea> mapByX = (Map<BigDecimal, ListReactArea>) listX.get(0);
        List<UUID> listIdsToRemove = (List<UUID>) listX.get(1);

        List listY = getMappedByY(mapByX, listIdsToRemove);
        mapByX = (Map<BigDecimal, ListReactArea>) listY.get(0);
        Map<BigDecimal, ListReactArea> mapByY = (Map<BigDecimal, ListReactArea>) listY.get(1);
        listIdsToRemove = (List<UUID>) listY.get(2);

        // expecting grid size 10x10
        List<Map<BigDecimal, ListReactArea>> value = GridUtils.removeSquaresToConformMinWidthAndHeight(mapByX,
                mapByY, 10, 10, tolleranceInPercent);

        assertEquals(10, value.get(0).size());
        assertEquals(10, value.get(1).size());

        Set<UUID> allCellsByX = value.get(0).entrySet().stream().flatMap(p -> p.getValue().list.stream()).map(p -> p.id)
                .collect(Collectors.toSet());
        Set<UUID> allCellsByY = value.get(1).entrySet().stream().flatMap(p -> p.getValue().list.stream()).map(p -> p.id)
                .collect(Collectors.toSet());
        Set<UUID> allCellsToBeRemoved = listIdsToRemove.stream().collect(Collectors.toSet());

        Set<UUID> intersectionX = new HashSet<>(allCellsByX);
        intersectionX.retainAll(allCellsToBeRemoved);
        assertEquals(0, intersectionX.size());

        Set<UUID> intersectionY = new HashSet<>(allCellsByY);
        intersectionY.retainAll(allCellsToBeRemoved);
        assertEquals(0, intersectionY.size());

    }

    @Test
    void groupByInCollectingTest() {
        int hCount = 10;
        int vCount = 10;
        List<RectArea> list = collectList(hCount, vCount);
        Map<BigDecimal, ListReactArea> map = GridUtils.groupByInCollecting(list, p -> p.x,
                p -> p.xDecreased);

        assertEquals(hCount, map.keySet().size());

        for (BigDecimal key : map.keySet()) {
            assertEquals(vCount, map.get(key).list.size());
        }

    }

    private List<RectArea> collectList(int hCount, int vCount) {
        List<RectArea> list = new ArrayList<>();
        int x = 10;
        int y = 15;

        int width = 10, height = 10;

        int gapH = (int) Math.round((double) width / 100 * tolleranceInPercentInt - 1);
        int gapV = (int) Math.round((double) height / 100 * tolleranceInPercentInt - 1);

        for (int i = 0; i < hCount; i++) {
            for (int k = 0; k < vCount; k++) {
                Rect rect = new Rect(x, y, width, height);
                RectArea cell = new RectArea(rect,tolleranceInPercent);
                list.add(cell);
                y += height + gapV;
            }
            x += width + gapH;
            y = 15;
        }
        return list;
    }

    /*
     * index 0 - List<GridCell> list with 4 intervals
     */
    private List<BigDecimal> createFourIntervals(BigDecimal width, BigDecimal tolleranceInPercent) {
        List<BigDecimal> list = new ArrayList();
        BigDecimal x = BigDecimal.valueOf(10).setScale(2, RoundingMode.HALF_EVEN);
        for (int i = 1; i <= 20; i++) {

            x = x
                    .add(width)
                    .add(width
                            .divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_EVEN)
                            .multiply(tolleranceInPercent)
                            .divide(BigDecimal.valueOf(2),2, RoundingMode.HALF_EVEN)
                            .subtract(
                                    width.divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(10))));

            list.add(x);
            if (i == 3 || i == 10 || i == 12) {

                x = x.add(width)
                        .add(width.divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_EVEN)
                                .multiply(tolleranceInPercent.multiply(BigDecimal.valueOf(2))));

                list.add(x);
            }
        }
        return list;
    }

    /*
     * index 0 - List<GridCell> list with 1 interval
     */
    private List<BigDecimal> createOneIntervals(BigDecimal width, BigDecimal tolleranceInPercent) {
        List<BigDecimal> list = new ArrayList();
        BigDecimal x = BigDecimal.valueOf(10).setScale(2, RoundingMode.HALF_EVEN);
        for (int i = 1; i <= 20; i++) {
            x = x
                    .add(width)
                    .add(
                            width
                                    .divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_EVEN)
                                    .multiply(tolleranceInPercent)
                                    .divide(BigDecimal.valueOf(2),2, RoundingMode.HALF_EVEN)
                                            .subtract(
                                                    width
                                                            .divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_EVEN)
                                                            .multiply(BigDecimal.valueOf(10))));
            list.add(x);
        }
        return list;
    }

    /*
     * index 0 - List<GridCell> list with 50 non removing members
     * index 1 - List<UUID> 21 GridCells ids to be removed
     */
    private List<RectArea> getThreeSequancesWithinList(BigDecimal tolleranceInPercent) {
        List<RectArea> listCreate = new ArrayList<>();
        List<RectArea> listCellsToRemove = new ArrayList<>();

        List listRet = Arrays.asList(listCreate, listCellsToRemove);

        BigDecimal x = BigDecimal.valueOf(20);
        BigDecimal y = BigDecimal.valueOf(20);
        for (int i = 1; i <= 50; i++) {
            Rect rect = new Rect(new Point(x.intValue(), y.intValue()), new Size(width, height));
            RectArea gridCell = new RectArea(rect, tolleranceInPercent);
            listCreate.add(gridCell);
            if (i == 13 || i == 24 || i == 36) {
                for (int j = 1; j <= 7; j++) {

                    x = x.add(BigDecimal.valueOf(width)).add(
                            BigDecimal.valueOf(width).setScale(2, RoundingMode.HALF_EVEN)
                                    .divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_EVEN)
                                    .multiply(tolleranceInPercent)
                                    .multiply(BigDecimal.valueOf(2)))
                            .add(BigDecimal.valueOf(gap));

                    rect = new Rect(new Point(x.intValue(), y.intValue()), new Size(width, height));
                    gridCell = new RectArea(rect, tolleranceInPercent);
                    listCreate.add(gridCell);
                    listCellsToRemove.add(gridCell);
                }
            }
            x = x.add(BigDecimal.valueOf(width)).add(BigDecimal.valueOf(gap));
        }

        return listRet;
    }

    /*
     * index 0 - Map<BigDecimal, ListReactArea> list with 10 non removing members
     * index 1 - List<UUID> GridCells ids to be removed
     */
    private List getMappedByX() {
        Map<BigDecimal, ListReactArea> map = new HashMap<>();
        List<UUID> listId = new ArrayList();

        List listRet = Arrays.asList(map, listId);

        int x = 20;
        for (int i = 1; i <= 10; i++) {
            List<RectArea> list = new ArrayList<>();
            int y = 20;
            for (int j = 1; j <= 10; j++) {
                Rect rect = new Rect(new Point(x, y), new Size(width, height));
                RectArea gridCell = new RectArea(rect, tolleranceInPercent);
                list.add(gridCell);
                y += height + gap;
            }
            /* if (i == 3 || i == 6 || i == 8) {
                Rect rect = new Rect(new Point(x, y + height + gap), new Size(width, height));
                RectArea gridCell = new RectArea(rect, tolleranceInPercent);
                list.add(gridCell);
                listId.add(gridCell.id);
            } */
            map.put(BigDecimal.valueOf(x), new ListReactArea(list.get(0), list));
            x += width + gap;
        }

        return listRet;
    }

    /*
     * index 0 - Map<BigDecimal, ListReactArea> xs list with 10 non removing
     * members
     * index 1 - Map<BigDecimal, ListReactArea> ys list with 10 non removing
     * members
     * index 2 - List<UUID> GridCells ids to be removed
     */
    private List getMappedByY(Map<BigDecimal, ListReactArea> mapByX, List<UUID> listUUIDtoRemove) {

        Map<BigDecimal, ListReactArea> map = GridUtils.groupByInCollecting(
                mapByX.entrySet().stream().flatMap(p -> p.getValue().list.stream())
                        .collect(Collectors.toList()),
                p -> p.y,
                p -> p.yDecreased);

        List<BigDecimal> ys = map.keySet().stream().collect(Collectors.toList());
        ys = ys.stream().sorted().collect(Collectors.toList());
        /* for (int i = 1; i <= 7; i++) {
            if (i == 2 || i == 5 || i == 7) {

                List<RectArea> listByY = map.get(ys.get(i - 1)).list;
                Integer maxX = listByY.stream().map(p -> p.rectangle.x).max((p, l) -> Integer.compare(p, l)).get();
                BigDecimal y = ys.get(i - 1);
                Integer x = maxX + width + gap;
                Rect rect = new Rect(new Point(x, y.intValue()), new Size(width, height));
                RectArea gridCell = new RectArea(rect, tolleranceInPercent);
                listByY.add(gridCell);
                listUUIDtoRemove.add(gridCell.id);

                mapByX.put(BigDecimal.valueOf(x), new ListReactArea(gridCell, Arrays.asList(gridCell)));
            }
        } */

        return Arrays.asList(mapByX, map, listUUIDtoRemove);
    }
}
