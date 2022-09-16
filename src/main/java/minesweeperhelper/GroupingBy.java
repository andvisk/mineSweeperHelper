package minesweeperhelper;

import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.function.Function;

public class GroupingBy {

    public static <T> Map<Integer, List<T>> approximate(List<T> list, Function<T, Integer> functionPosition, Function<T, Integer> functionWidth, int tolleranceInPercents) {

        Map<Integer, List<T>> map = new HashMap<>();

        for (int i = 0; i < list.size(); i++) {

            final int finalI = i;

            Integer closestValue = map.entrySet().stream()
                    .map(p -> p.getKey())
                    .min(Comparator.comparingInt(p -> Math.abs(p - functionPosition.apply(list.get(finalI)))))
                    .orElse(-1);

            if (closestValue > 0 && (double) functionWidth.apply(list.get(i)) / 100 * tolleranceInPercents >= Math.abs(closestValue - functionPosition.apply(list.get(i)))) {
                map.get(closestValue).add(list.get(i));
            } else {
                List<T> mapValueList = new ArrayList<>();
                mapValueList.add(list.get(i));
                map.put(functionPosition.apply(list.get(i)), mapValueList);
            }

        }
        return map;
    }

}
