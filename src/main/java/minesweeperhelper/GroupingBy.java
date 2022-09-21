package minesweeperhelper;

import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.function.Function;

public class GroupingBy {

    public static <T> Map<Integer, List<T>> approximateInArea(List<T> list, Function<T, Integer> functionPosition,
            Function<T, Integer> functionWidthOrHeight, int tolleranceInPercents) {

        Map<Integer, List<T>> map = new HashMap<>();

        for (int i = 0; i < list.size(); i++) {

            final int finalI = i;

            Integer closestValue = map.entrySet().stream()
                    .map(p -> p.getKey())
                    .min(Comparator.comparingInt(p -> Math.abs(p - functionPosition.apply(list.get(finalI)))))
                    .orElse(-1);

            if (closestValue > 0 && (double) functionWidthOrHeight.apply(list.get(i)) / 100
                    * tolleranceInPercents >= Math.abs(closestValue - functionPosition.apply(list.get(i)))) {
                map.get(closestValue).add(list.get(i));
            } else {
                List<T> mapValueList = new ArrayList<>();
                mapValueList.add(list.get(i));
                map.put(functionPosition.apply(list.get(i)), mapValueList);
            }

        }
        return map;
    }

    public static <T> Map<Integer, List<T>> approximate(List<T> list, Function<T, Integer> functionLength,
            int tolleranceInPercents) {

        Map<Integer, List<T>> map = new HashMap<>();

        for (int i = 0; i < list.size(); i++) {

            final int finalI = i;

            Integer closestValue = map.entrySet().stream()
                    .map(p -> p.getKey())
                    .min(Comparator
                            .comparingInt(p -> Math.abs(p - (int) Math.round(functionLength.apply(list.get(finalI))))))
                    .orElse(-1);

            if (closestValue > 0 && (double) Math.round(functionLength.apply(list.get(finalI))) / 100
                    * tolleranceInPercents >= Math.abs(closestValue - functionLength.apply(list.get(i)))) {
                map.get(closestValue).add(list.get(i));
            } else {
                List<T> mapValueList = new ArrayList<>();
                mapValueList.add(list.get(i));
                map.put((int) Math.round(functionLength.apply(list.get(finalI))), mapValueList);
            }

        }
        return map;
    }
}
