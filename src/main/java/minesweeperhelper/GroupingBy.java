package minesweeperhelper;

import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupingBy {

        public static <T> Map<BigDecimal, List<T>> approximateInArea(List<T> list,
                        Function<T, Integer> functionPosition,
                        Function<T, Integer> functionWidthOrHeight, BigDecimal tolleranceInPercentsBD) {

                Map<BigDecimal, List<T>> map = new HashMap<>();

                for (int i = 0; i < list.size(); i++) {

                        final int finalI = i;

                        BigDecimal closestValue = map.entrySet().stream()
                                        .map(p -> p.getKey())
                                        .min((a, b) -> {
                                                return a.subtract(BigDecimal.valueOf(functionPosition
                                                                .apply(list.get(finalI)))).abs()
                                                                .compareTo(
                                                                                b.subtract(BigDecimal.valueOf(
                                                                                                functionPosition
                                                                                                                .apply(list.get(finalI))))
                                                                                                .abs());
                                        })
                                        .orElse(BigDecimal.valueOf(-1));

                        if (closestValue.compareTo(BigDecimal.ZERO) > 0 &&
                                        BigDecimal.valueOf(functionWidthOrHeight.apply(list.get(i)))
                                                        .setScale(2, RoundingMode.HALF_EVEN)
                                                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
                                                        .multiply(tolleranceInPercentsBD)
                                                        .compareTo(
                                                                        closestValue.subtract(
                                                                                        BigDecimal.valueOf(
                                                                                                        functionPosition.apply(
                                                                                                                        list.get(i)))
                                                                                                        .setScale(2,
                                                                                                                        RoundingMode.HALF_EVEN))
                                                                                        .abs()) >= 0) {
                                map.get(closestValue).add(list.get(i));
                        } else {
                                List<T> mapValueList = new ArrayList<>();
                                mapValueList.add(list.get(i));
                                map.put(BigDecimal.valueOf(functionPosition.apply(list.get(i))).setScale(2,
                                                RoundingMode.HALF_EVEN), mapValueList);
                        }

                }

                Map<BigDecimal, List<T>> mapWithAvgKeys = new HashMap<>();
                for (Map.Entry<BigDecimal, List<T>> entry : map.entrySet()) {
                        BigDecimal key = entry.getKey();
                        BigDecimal count = BigDecimal.valueOf(entry.getValue().size());
                        BigDecimal sum = entry.getValue().stream()
                                        .map(p -> BigDecimal.valueOf(functionPosition.apply(p)))
                                        .reduce(BigDecimal.ZERO,
                                                        (subtotal, element) -> subtotal.add(element))
                                        .setScale(2,
                                                        RoundingMode.HALF_EVEN);
                        BigDecimal newKey = sum.divide(count, 2, RoundingMode.HALF_EVEN);
                        mapWithAvgKeys.put(newKey, entry.getValue());
                }

                return mapWithAvgKeys;
        }

        public static <T> Map<BigDecimal, List<T>> approximate(List<T> list, Function<T, Integer> functionLength,
                        BigDecimal tolleranceInPercentsBD) {

                Map<BigDecimal, List<T>> map = new HashMap<>();

                for (int i = 0; i < list.size(); i++) {

                        final int finalI = i;

                        BigDecimal closestValue = map.entrySet().stream()
                                        .map(p -> p.getKey())
                                        .min((a, b) -> {
                                                return a.subtract(BigDecimal.valueOf(
                                                                functionLength.apply(list.get(finalI))))
                                                                .abs()
                                                                .compareTo(
                                                                                b.subtract(BigDecimal.valueOf(
                                                                                                functionLength.apply(
                                                                                                                list.get(finalI))))
                                                                                                .abs());
                                        })
                                        .orElse(BigDecimal.valueOf(-1));

                        if (closestValue.compareTo(BigDecimal.ZERO) > 0 &&

                                        BigDecimal.valueOf(functionLength.apply(list.get(i)))
                                                        .setScale(2, RoundingMode.HALF_EVEN)
                                                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
                                                        .multiply(tolleranceInPercentsBD)
                                                        .compareTo(
                                                                        closestValue.subtract(
                                                                                        BigDecimal.valueOf(
                                                                                                        functionLength.apply(
                                                                                                                        list.get(i)))
                                                                                                        .setScale(2,
                                                                                                                        RoundingMode.HALF_EVEN))
                                                                                        .abs()) >= 0) {
                                map.get(closestValue).add(list.get(i));
                        } else {
                                List<T> mapValueList = new ArrayList<>();
                                mapValueList.add(list.get(i));
                                map.put(BigDecimal.valueOf(functionLength.apply(list.get(i))).setScale(2,
                                                RoundingMode.HALF_EVEN), mapValueList);
                        }

                }

                Map<BigDecimal, List<T>> mapWithAvgKeys = new HashMap<>();
                for (Map.Entry<BigDecimal, List<T>> entry : map.entrySet()) {
                        BigDecimal key = entry.getKey();
                        BigDecimal count = BigDecimal.valueOf(entry.getValue().size());
                        BigDecimal sum = entry.getValue().stream().map(p -> BigDecimal.valueOf(functionLength.apply(p)))
                                        .reduce(BigDecimal.ZERO,
                                                        (subtotal, element) -> subtotal.add(element))
                                        .setScale(2, RoundingMode.HALF_EVEN);
                        BigDecimal newKey = sum.divide(count, 2, RoundingMode.HALF_EVEN);
                        mapWithAvgKeys.put(newKey, entry.getValue());
                }

                return mapWithAvgKeys;
        }

        public static List<Set<RectArea>> makeGroups(List<RectArea> list,
                        Function<RectArea, BigDecimal> functionRealSize,
                        Function<RectArea, BigDecimal> functionComparedSize,
                        Function<RectArea, List<Set<RectArea>>> functionAddMeToGroup) {

                list = list.stream().sorted((a, b) -> functionRealSize.apply(a).compareTo(functionRealSize.apply(b)))
                                .collect(Collectors.toList());

                List<Set<RectArea>> listBySize = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                        Set<RectArea> set = new HashSet<>();
                        RectArea rectArea = list.get(i);
                        set.add(rectArea);
                        int k = i + 1;
                        boolean found = true;
                        while (k < list.size() && found) {
                                RectArea rectAreaToCompare = list.get(k);
                                if (functionComparedSize.apply(rectArea)
                                                .compareTo(functionRealSize.apply(rectAreaToCompare)) >= 0) {
                                        set.add(rectAreaToCompare);
                                } else {
                                        found = false;
                                }
                                k += 1;
                        }
                        for (RectArea setArea : set) {
                                functionAddMeToGroup.apply(setArea).add(set);
                        }
                        listBySize.add(set);
                }
                return listBySize;
        }

        
}
