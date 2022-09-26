package minesweeperhelper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.opencv.core.Rect;

public class GroupingByTest {

    @Test
    void approximateInAreaTest() {
        int hCount = 10;
        int vCount = 10;
        int tollerance = 35;
        List<GridCell> list = collectList(hCount, vCount, tollerance);
        Map<BigDecimal, List<GridCell>> map = GroupingBy.approximateInArea(list, p -> p.getRect().x,
                p -> p.getRect().width, BigDecimal.valueOf(tollerance));

        assertEquals(hCount, map.keySet().size());

        for (BigDecimal key : map.keySet()) {
            assertEquals(vCount, map.get(key).size());
        }

    }

    @Test
    void approximateTest() {
        int hCount = 10;
        int vCount = 10;
        int tollerance = 35;
        List<GridCell> list = collectListWithDifWidth(hCount, vCount, tollerance);
        Map<BigDecimal, List<GridCell>> mapByWidth = GroupingBy.approximate(list,
                p -> p.getRect().width,
                BigDecimal.valueOf(tollerance));

        assertEquals(hCount, mapByWidth.keySet().size());

        for (BigDecimal key : mapByWidth.keySet()) {
            assertEquals(vCount, mapByWidth.get(key).size());
        }

    }

    private List<GridCell> collectList(int hCount, int vCount, int tollerance) {
        List<GridCell> list = new ArrayList<>();
        int x = 10;
        int y = 15;

        int width = 10, height = 10;

        int gapH = (int) Math.round((double) width / 100 * tollerance - 1);
        int gapV = (int) Math.round((double) height / 100 * tollerance - 1);

        for (int i = 0; i < hCount; i++) {
            for (int k = 0; k < vCount; k++) {
                Rect rect = new Rect(x, y, width, height);
                GridCell cell = new GridCell(rect);
                list.add(cell);
                y += height + gapV;
            }
            x += width + gapH;
            y = 15;
        }
        return list;
    }

    private List<GridCell> collectListWithDifWidth(int hCount, int vCount, int tollerance) {
        List<GridCell> list = new ArrayList<>();
        int x = 10;
        int y = 15;

        int width = 10, height = 10;

        int gapH = (int) Math.round((double) width / 100 * tollerance - 1);
        int gapV = (int) Math.round((double) height / 100 * tollerance - 1);

        for (int i = 0; i < hCount; i++) {
            for (int k = 0; k < vCount; k++) {
                Rect rect = new Rect(x, y, width, height);
                GridCell cell = new GridCell(rect);
                list.add(cell);
                y += height + gapV;
            }
            width += (double) width / 100 * tollerance * 2;
            x += width + gapH;
            y = 15;
        }
        return list;
    }
}
