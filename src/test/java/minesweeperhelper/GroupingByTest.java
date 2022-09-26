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
        List<GridCell> list = collectList(hCount, vCount);
        Map<BigDecimal, List<GridCell>> map = GroupingBy.approximateInArea(list, p -> p.getRect().x,
                p -> p.getRect().width, BigDecimal.valueOf(50));

        assertEquals(hCount, map.keySet().size());

        for (BigDecimal key : map.keySet()) {
            assertEquals(vCount, map.get(key).size());
        }

    }

    @Test
    void approximate() {

    }

    private List<GridCell> collectList(int hCount, int vCount) {
        List<GridCell> list = new ArrayList<>();
        int x = 10;
        int y = 15;
        int gap = 2;
        int width = 10, height = 10;
        for (int i = 0; i < hCount; i++) {
            for (int k = 0; k < vCount; k++) {
                Rect rect = new Rect(x, y, width, height);
                GridCell cell = new GridCell(rect);
                list.add(cell);
                y += height + gap;
            }
            x += width + gap;
            y = 15;
        }
        return list;
    }
}
